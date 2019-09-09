package org.hspconsortium.sandboxmanagerapi.services.impl;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.crypto.RsaProvider;
import org.hspconsortium.sandboxmanagerapi.services.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

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

//    @Override
//    public String createSignedHookJwt(String subject) {
//        KeyPair kp = RsaProvider.generateKeyPair();
//        PrivateKey privateKey = kp.getPrivate();
//        return Jwts.builder()
//                .setSubject(subject)
//                .signWith(SignatureAlgorithm.RS256, privateKey)
//                .compact();
//    }

}