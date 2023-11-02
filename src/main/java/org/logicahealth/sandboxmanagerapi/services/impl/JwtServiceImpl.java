package org.logicahealth.sandboxmanagerapi.services.impl;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.logicahealth.sandboxmanagerapi.services.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class JwtServiceImpl implements JwtService {
    private static Logger LOGGER = LoggerFactory.getLogger(JwtServiceImpl.class.getName());

    @Value("${hspc.platform.jwt.key}")
    private String jwtKey;

    @Value("${hspc.platform.jwt.signatureAlgorithm}")
    private String signatureAlgorithm;

    public JwtServiceImpl() {
    }

    @Override
    public String createSignedJwt(String subject) {
        
        LOGGER.info("Inside JwtServiceImpl - createSignedJwt");

        String retVal = Jwts.builder()
                .setSubject(subject)
                .signWith(SignatureAlgorithm.forName(signatureAlgorithm), jwtKey)
                .compact();

        LOGGER.debug("Inside JwtServiceImpl - createSignedJwt: "
        +"Parameters: subject = "+subject+"; Return value = "+retVal);

        return retVal;
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