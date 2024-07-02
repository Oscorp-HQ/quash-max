package com.quashbugs.quash.service;

import com.quashbugs.quash.dto.request.ResetPasswordRequestDTO;
import com.quashbugs.quash.dto.request.SignInRequestDTO;
import com.quashbugs.quash.dto.request.SignUpRequestDTO;
import com.quashbugs.quash.dto.response.JwtAuthenticationResponseDTO;
import com.quashbugs.quash.model.Organisation;
import com.quashbugs.quash.model.TeamMember;
import com.quashbugs.quash.model.User;
import com.quashbugs.quash.repo.OrganisationRepository;
import com.quashbugs.quash.repo.TeamMemberRepository;
import com.quashbugs.quash.repo.UserRepository;
import com.quashbugs.quash.util.VerificationTokenGenerator;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.*;

import static com.quashbugs.quash.constants.Constants.GOOGLE;
import static com.quashbugs.quash.constants.Constants.MEMBER;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    private final EmailService emailService;

    private final ClientRegistrationRepository clientRegistrationRepository;

    private final OrganisationRepository organisationRepository;

    private final TeamMemberRepository teamMemberRepository;

    @Autowired
    public AuthenticationService(UserRepository userRepository,
                                 PasswordEncoder passwordEncoder,
                                 JwtService jwtService,
                                 EmailService emailService,
                                 ClientRegistrationRepository clientRegistrationRepository,
                                 OrganisationRepository organisationRepository,
                                 TeamMemberRepository teamMemberRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.emailService = emailService;
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.organisationRepository = organisationRepository;
        this.teamMemberRepository = teamMemberRepository;
    }

    @Value("${spring.url}")
    private String baseUrl;

    public Map<String, Object> signup(SignUpRequestDTO request) {
        try {
            if (userRepository.findByWorkEmail(request.getWorkEmail()).isPresent()) {
                throw new IllegalArgumentException("User with this email already exists");
            }
            Map<String, Object> map = new HashMap<>();

            var user = User.builder()
                    .workEmail(request.getWorkEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .shouldNavigateToDashboard(false)
                    .createdAt(new Date())
                    .signUpType(request.getSignUpType())
                    .build();
            userRepository.save(user);

            var jwt = jwtService.generateToken(user);
            var refreshToken = jwtService.generateRefreshToken(user);
            map.put("token", JwtAuthenticationResponseDTO.builder().token(jwt).build().getToken());
            map.put("refreshToken", JwtAuthenticationResponseDTO.builder().refreshToken(refreshToken).build().getRefreshToken());
            map.put("should_navigate_to_dashboard", user.isShouldNavigateToDashboard());
            if (checkIfOrganisationExists(request.getWorkEmail(), user)) {
                map.put("organisation_present", true);
            } else {
                map.put("organisation_present", false);
            }
            return map;
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    public Map<String, Object> signIn(SignInRequestDTO request) {
        try {
            var user = userRepository.findByWorkEmail(request.getWorkEmail())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));

            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                throw new IllegalArgumentException("Invalid email or password.");
            }
            var jwt = jwtService.generateToken(user);
            var refreshToken = jwtService.generateRefreshToken(user);
            Map<String, Object> map = new HashMap<>();
            if (checkIfOrganisationExists(request.getWorkEmail(), user)) {
                map.put("organisation_present", true);
            } else {
                map.put("organisation_present", false);
            }
            map.put("token", JwtAuthenticationResponseDTO.builder().token(jwt).build().getToken());
            map.put("refreshToken", JwtAuthenticationResponseDTO.builder().refreshToken(refreshToken).build().getRefreshToken());
            map.put("should_navigate_to_dashboard", user.isShouldNavigateToDashboard());
            return map;
        } catch (AuthenticationException e) {
            throw new IllegalArgumentException(e.getMessage());
        }

    }

    public boolean verifyEmail(String verificationToken) {
        try {
            var user = userRepository.findByVerificationToken(verificationToken)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid verification token."));

            if (user.getTokenExpiration().isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("Verification token has expired.");
            }
            user.setEmailVerified(true);
            user.setVerificationToken(null); // Remove the token
            user.setTokenExpiration(null);   // Remove the token expiration
            userRepository.save(user);

            return true;
        } catch (Exception e) {
            throw new RuntimeException("An error occurred during email verification.", e);
        }
    }

    public void sendInviteEmail(String email, User owner) {
        try {
            var user = userRepository.findByWorkEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid email."));

            String resetPasswordUrl = baseUrl + "/api/auth/reset_password?authToken=" + jwtService.generateToken(user);
            var org = organisationRepository.findOptionalByCreatedBy(owner);
            if (org.isPresent()) {
                emailService.sendInviteEmail(user, resetPasswordUrl, owner.getFullName(), org.get().getName());
            } else {
                throw new IllegalArgumentException("No Organisation Found");
            }
        } catch (Exception e) {
            System.out.println("MAIL FAILURE: " + e.getMessage());
            throw new RuntimeException("An error occurred during sending email", e);
        }
    }

    public void sendEmail(String action, String payload) {
        if (action.equals("VERIFY")) {
            try {
                var user = userRepository.findByWorkEmail(jwtService.extractWorkEmail(payload))
                        .orElseThrow(() -> new IllegalArgumentException("Invalid verification token."));
                String verificationToken = VerificationTokenGenerator.generateToken();
                user.setVerificationToken(verificationToken);
                user.setTokenExpiration(LocalDateTime.now().plusHours(24));
                userRepository.save(user);
                String verificationUrl = baseUrl + "/api/auth/verify?token=" + verificationToken;
                emailService.sendVerificationEmail(user, verificationUrl);
            } catch (Exception e) {
                System.out.println("MAIL FAILURE: " + e.getMessage());
                throw new RuntimeException("An error occurred during sending email", e);
            }
        } else if (action.equals("FORGOT_PASSWORD")) {
            try {
                var user = userRepository.findByWorkEmail(payload)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid email."));
                String resetPasswordUrl = baseUrl + "/api/auth/reset_password?authToken=" + jwtService.generateToken(user);
                emailService.sendResetPasswordEmail(user, resetPasswordUrl);
            } catch (Exception e) {
                System.out.println("MAIL FAILURE: " + e.getMessage());
                throw new RuntimeException("An error occurred during sending email", e);
            }
        }
    }

    public Map<String, Object> getGoogleOAuth2User(String accessToken) {
        String userInfoEndpoint = "https://www.googleapis.com/oauth2/v3/userinfo";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                userInfoEndpoint, HttpMethod.GET, entity, new ParameterizedTypeReference<Map<String, Object>>() {
                });

        if (response.getStatusCode() == HttpStatus.OK) {
            Map<String, Object> userInfo = response.getBody();
            assert userInfo != null;
            return processUser(userInfo);
        }
        return null;
    }

    public Map<String, Object> processUser(Map<String, Object> userInfo) {
        String email = (String) userInfo.get("email");
        Optional<User> userOptional = userRepository.findByWorkEmail(email);

        return userOptional.map(this::createUserResponse)
                .orElseGet(() -> createAndSaveNewUser(email));
    }

    private Map<String, Object> createUserResponse(User user) {
        Map<String, Object> map = createResponseMap(user);
        map.put("organisation_present", checkIfOrganisationExists(user.getWorkEmail(), user));

        return map;
    }

    private Map<String, Object> createResponseMap(User user) {
        String jwt = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        Map<String, Object> map = new HashMap<>();
        map.put("token", JwtAuthenticationResponseDTO.builder().token(jwt).build().getToken());
        map.put("refreshToken", JwtAuthenticationResponseDTO.builder().refreshToken(refreshToken).build().getRefreshToken());
        map.put("should_navigate_to_dashboard", user.isShouldNavigateToDashboard());
        return map;
    }

    private Map<String, Object> createAndSaveNewUser(String email) {
        String hash = VerificationTokenGenerator.generateToken();
        SignUpRequestDTO signUpRequestDTO = new SignUpRequestDTO(email, hash);
        signup(signUpRequestDTO);
        Optional<User> googleUser = userRepository.findByWorkEmail(email);
        return googleUser.map(user -> {
            user.setEmailVerified(true);
            user.setSignUpType(GOOGLE);
            userRepository.save(user);
            return createUserResponse(user);
        }).orElse(null);
    }

    private boolean checkIfOrganisationExists(String workEmail, User userToAdd) {
        String domainToFind = extractDomainFromEmail(workEmail);

        if (hasGmailSuffix(workEmail)) {
            return checkGmailDomainOrganisation(userToAdd);
        }

        return checkNonGmailDomainOrganisation(domainToFind, userToAdd);
    }

    private boolean checkGmailDomainOrganisation(User userToAdd) {
        TeamMember teamMember = teamMemberRepository.findByUser(userToAdd);
        return teamMember != null && teamMember.getOrganisation() != null;
    }

    private boolean checkNonGmailDomainOrganisation(String domainToFind, User userToAdd) {
        List<User> usersWithGivenDomain = userRepository.findByWorkEmailEndingWith(domainToFind);

        for (User userWithGivenDomain : usersWithGivenDomain) {
            TeamMember teamMember = teamMemberRepository.findByUser(userWithGivenDomain);

            if (teamMember != null) {
                updateTeamMemberForUser(userToAdd, teamMember.getOrganisation());
                return true;
            }
        }
        return false;
    }

    private void updateTeamMemberForUser(User userToAdd, Organisation organisation) {
        TeamMember existingTeamMember = teamMemberRepository.findByUser(userToAdd);
        if (existingTeamMember == null) {
            TeamMember teamMemberToSave = addTeamMemberToGoogleSignedUpUser(userToAdd, organisation, MEMBER);
            teamMemberRepository.save(teamMemberToSave);
        }
    }

    public TeamMember addTeamMemberToGoogleSignedUpUser(User user, Organisation organisation, String role) {
        return TeamMember.builder()
                .organisation(organisation)
                .user(user)
                .role(role)
                .joinedAt(new Date())
                .build();
    }

    public static String extractDomainFromEmail(String email) {
        return email.substring(email.indexOf('@'));
    }

    public boolean hasGmailSuffix(String email) {
        return email.endsWith("@gmail.com");
    }


    public ResponseEntity<Void> signInWithGoogle() {
        ClientRegistration clientRegistration = clientRegistrationRepository.findByRegistrationId("google");

        String redirectUri = baseUrl + "/api/auth/google/callback"; // Change this to your callback URL

        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString(clientRegistration.getProviderDetails().getAuthorizationUri())
                .queryParam("response_type", "code")
                .queryParam("client_id", clientRegistration.getClientId())
                .queryParam("redirect_uri", redirectUri)
                .queryParam("scope", "openid profile email");

        var uri = uriBuilder.toUriString();
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, uriBuilder.toUriString())
                .build();
    }

    public User getUserByToken(String authToken) {
        try {
            // Authenticate using workEmail and password
            return userRepository.findByWorkEmail(jwtService.extractWorkEmail(authToken))
                    .orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));
        } catch (AuthenticationException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    public void changePassword(User user, ResetPasswordRequestDTO resetPasswordRequestDTO) throws Exception {
        try {
            user.setPassword(passwordEncoder.encode(resetPasswordRequestDTO.getNewPassword()));
            userRepository.save(user);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    public Map<String, Object> getTokens(Claims claims) {
        HashMap<String, Object> map = new HashMap<>();
        String email = claims.get("sub", String.class);

        if (email != null) {
            var user = userRepository.findByWorkEmail(email);
            if (user.isPresent()) {
                var accessToken = jwtService.generateToken(user.get());
                var refreshToken = jwtService.generateRefreshToken(user.get());
                map.put("token", JwtAuthenticationResponseDTO.builder().token(accessToken).build().getToken());
                map.put("refreshToken", JwtAuthenticationResponseDTO.builder().refreshToken(refreshToken).build().getRefreshToken());
                return map;
            }
        }
        return null;
    }
}