package com.quashbugs.quash.service;

import com.quashbugs.quash.model.Organisation;
import com.quashbugs.quash.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${token.signing.key}")
    private String jwtSigningKey;

    @Value("${token.accessToken.expiration}")
    private long expiration;

    @Value("${token.refreshToken.expiration}")
    private long refreshExpiration;

    public String extractWorkEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String generateToken(User user) {
        return generateToken(new HashMap<>(), user);
    }

    private String generateToken(Map<String, Object> extraClaims, User user) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(user.getWorkEmail())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(User user) {
        return generateRefreshToken(new HashMap<>(), user);
    }

    private String generateRefreshToken(Map<String, Object> extraClaims, User user) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(user.getWorkEmail())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, User user) {
        final String workEmail = extractWorkEmail(token);
        return (workEmail.equals(user.getWorkEmail())) && !isTokenExpired(token);
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public boolean isTokenValid(String token, Organisation organisation) {
        final String orgId = extractOrgIdFromToken(token);
        if (orgId == null || isTokenExpired(token)) {
            return false;
        }
        String organisationIdString = String.valueOf(organisation.getId());
        return orgId.equals(organisationIdString);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolvers) {
        final Claims claims = extractAllClaims(token);
        return claimsResolvers.apply(claims);
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody();
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSigningKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateReportingToken(Organisation org) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("orgId", org.getId());
        claims.put("orgName", org.getName());
        return generateReportingToken(claims, org);
    }

    private String generateReportingToken(Map<String, Object> extraClaims, Organisation organisation) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setId(organisation.getOrgUniqueKey())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(Long.MAX_VALUE))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims decodeToken(String token) {
        return Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody();
    }

    public String extractOrgIdFromToken(String token) {
        Claims claims = decodeToken(token);
        Long orgId = claims.get("orgId", Long.class);
        return orgId != null ? orgId.toString() : null;
    }
}