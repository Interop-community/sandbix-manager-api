package org.hspconsortium.sandboxmanagerapi.services.impl;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.hspconsortium.sandboxmanagerapi.services.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtServiceImpl implements JwtService {

    @Value("${hspc.platform.jwt.key}")
    private String jwtKey;

    @Value("${hspc.platform.jwt.signatureAlgorithm}")
    private String signatureAlgorithm;

    public JwtServiceImpl() {
    }

    @Override
    public String createSignedJwt(String subject) {
        return Jwts.builder()
                .setSubject(subject)
                .signWith(SignatureAlgorithm.forName(signatureAlgorithm), jwtKey)
                .compact();
    }
}