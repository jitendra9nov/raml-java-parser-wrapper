package com.bhadouriya.raml.oauth;

import com.bhadouriya.raml.efficacies.Efficacy;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwk.RsaJwkGenerator;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.lang.JoseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

import static com.bhadouriya.raml.efficacies.Efficacy.toJSON;

@Component
public class SecurityUtil {

    @Autowired
    private RsaJsonWebKey rsaJsonWebKey;


    public String produceJwtToken(Object payload) throws IOException, JoseException {

        String jwt=null;

        JwtClaims claims=new JwtClaims();
        claims.setIssuer("Issuer");
        claims.setAudience("User");
        claims.setExpirationTimeMinutesInTheFuture(480);
        claims.setGeneratedJwtId();
        claims.setIssuedAtToNow();
        claims.setNotBeforeMinutesInThePast(2);
        claims.setSubject("App Name");
        claims.setStringClaim("UserInfo", toJSON(payload));


        JsonWebSignature signature=new JsonWebSignature();

        signature.setPayload(claims.toJson());

        signature.setKey(rsaJsonWebKey.getPrivateKey());

        signature.setKeyIdHeaderValue(rsaJsonWebKey.getKeyId());
        signature.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256);

        jwt=signature.getCompactSerialization();

        return jwt;
    }


    public JwtClaims consumeJwtToken(String token) throws InvalidJwtException {

        JwtConsumer jwtConsumer=new JwtConsumerBuilder().setRequireExpirationTime()
                .setAllowedClockSkewInSeconds(30)
                .setRequireSubject()
                .setExpectedIssuer("Issuser")
                .setExpectedAudience("User")
                .setVerificationKey(rsaJsonWebKey.getKey())
                .setJweAlgorithmConstraints(new AlgorithmConstraints(AlgorithmConstraints.ConstraintType.WHITELIST,AlgorithmIdentifiers.RSA_USING_SHA256))
                .build();

        JwtClaims claims=jwtConsumer.processToClaims(token);

        return claims;
    }

    public void generateJwkFiles(String privateKeyLoc, String publicKeyLoc){
        try(FileWriter privateKeyWriter =new FileWriter(privateKeyLoc);
            FileWriter publicKeyWriter =new FileWriter(publicKeyLoc);

        ) {

            RsaJsonWebKey rsaJsonWebKeyLoc= RsaJwkGenerator.generateJwk(2048);

            rsaJsonWebKeyLoc.setKeyId(UUID.randomUUID().toString());

            privateKeyWriter.write(rsaJsonWebKeyLoc.toJson(JsonWebKey.OutputControlLevel.INCLUDE_PRIVATE));

            publicKeyWriter.write(rsaJsonWebKeyLoc.toJson(JsonWebKey.OutputControlLevel.PUBLIC_ONLY));

            privateKeyWriter.flush();
            publicKeyWriter.flush();
        } catch (IOException | JoseException e) {
            e.printStackTrace();
        }
    }
}
