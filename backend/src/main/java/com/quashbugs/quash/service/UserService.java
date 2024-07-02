package com.quashbugs.quash.service;

import com.quashbugs.quash.dto.request.OrganisationSignUpRequestDTO;
import com.quashbugs.quash.model.User;
import com.quashbugs.quash.repo.TeamMemberRepository;
import com.quashbugs.quash.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    private PasswordEncoder passwordEncoder;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    public boolean checkUsersExist(List<String> emails) {
        List<String> existingUsers = findByEmails(emails);
        return !existingUsers.isEmpty();
    }

    private List<String> findByEmails(List<String> emails) {
        List<User> existingUsers = userRepository.findByWorkEmailIn(emails);
        return existingUsers.stream().map(User::getWorkEmail).collect(Collectors.toList());
    }

    public User getUserByWorkEmail(String workEmail) {
        Optional<User> userOptional = userRepository.findByWorkEmail(workEmail);
        return userOptional.orElse(null);
    }

    public UserDetailsService userDetailsService() {
        return new UserDetailsService() {
            @Override
            public User loadUserByUsername(String username) throws UsernameNotFoundException {
                User user = userRepository.findByUsername(username); // Implement this method in your UserRepository
                if (user == null) {
                    throw new UsernameNotFoundException("User not found for username");
                }
                return user;
            }
        };
    }

    public void updateUser(User user, User updatedUser) {
        if (user != null) {
            user.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
            user.setUsername(updatedUser.getUsername());
            userRepository.save(user);
        }
    }

    public boolean deleteUser(User user) {
        try {
            userRepository.delete(user);
            var teamMember = teamMemberRepository.findByUser(user);
            teamMemberRepository.delete(teamMember);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void saveUser(User user) {
        if (user != null) {
            userRepository.save(user);
        }
    }

    public void updateUsersWithOrganisationRole(User user, OrganisationSignUpRequestDTO organisationSignUpRequestDTO) {
        if (user != null) {
            user.setShouldNavigateToDashboard(true);
            user.setFullName(organisationSignUpRequestDTO.getFullName());
            user.setUserOrganisationRole(organisationSignUpRequestDTO.getOrganisationRole());
            userRepository.save(user);

        }
    }

    public Optional<User> findUserById(String userId) {
        return userRepository.findById(userId);
    }
}