package com.quashbugs.quash.config;

import com.quashbugs.quash.exceptions.TokenExpiredAuthenticationException;
import com.quashbugs.quash.model.Organisation;
import com.quashbugs.quash.model.User;
import com.quashbugs.quash.repo.OrganisationRepository;
import com.quashbugs.quash.service.JwtService;
import com.quashbugs.quash.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    @Autowired
    private final UserService userService;

    @Autowired
    private final OrganisationRepository organisationRepository;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
            final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String workEmail;
        final String orgId;

        if (ObjectUtils.isEmpty(authHeader) || !StringUtils.startsWithIgnoreCase(authHeader, "Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);
        if (jwtService.isTokenExpired(jwt)) {
            throw new TokenExpiredAuthenticationException("Token has expired");
        }
        workEmail = jwtService.extractWorkEmail(jwt);
        orgId = jwtService.extractOrgIdFromToken(jwt);

        if (StringUtils.hasText(workEmail) && SecurityContextHolder.getContext().getAuthentication() == null) {
            User user = userService.getUserByWorkEmail(workEmail);
            if (user!=null && jwtService.isTokenValid(jwt, user)) {
                SecurityContext context = SecurityContextHolder.createEmptyContext();
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        user, null, user.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                context.setAuthentication(authToken);
                SecurityContextHolder.setContext(context);
            }
        } else if (StringUtils.hasText(orgId) && SecurityContextHolder.getContext().getAuthentication() == null) {
            Optional<Organisation> organisation= organisationRepository.findOptionalById(Long.parseLong(orgId));
            if(organisation.isPresent() && jwtService.isTokenValid(jwt, organisation.get())){
                SecurityContext context = SecurityContextHolder.createEmptyContext();
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        organisation, null, null);
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                context.setAuthentication(authToken);
                SecurityContextHolder.setContext(context);
            }
        }
        filterChain.doFilter(request, response);
    }
}