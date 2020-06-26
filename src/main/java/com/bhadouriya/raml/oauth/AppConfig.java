package com.bhadouriya.raml.oauth;

import com.bhadouriya.raml.efficacies.Efficacy;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.lang.JoseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.security.interfaces.RSAPublicKey;

import static com.bhadouriya.raml.efficacies.Efficacy.loadFileFromClasspath;

@Configuration
public class AppConfig {

    @Value("${private.key.location}")
    private String privateKey;

    @Bean
    public FilterRegistrationBean authenticationFilter(@Autowired SecurityUtil securityUtil){
        FilterRegistrationBean registrationBean=new FilterRegistrationBean();

        AuthenticationFilter authenticationFilter=new AuthenticationFilter(securityUtil);

        registrationBean.setName("authenticationFilter");
        registrationBean.setFilter(authenticationFilter);
        registrationBean.addUrlPatterns("/asNeeded");
        registrationBean.setOrder(1);
        return registrationBean;
    }

    @Bean
    public RsaJsonWebKey rsaJsonWebKey() throws IOException ,JoseException{

        RsaJsonWebKey rsaJsonWebKey=null;
        try {
            File privateKeyFile= loadFileFromClasspath(privateKey);

            Object keyObject=new ObjectMapper().readValue(privateKeyFile,Object.class);

            PublicJsonWebKey jsonWebKey=PublicJsonWebKey.Factory.newPublicJwk(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(keyObject));

            rsaJsonWebKey=new RsaJsonWebKey((RSAPublicKey) jsonWebKey.getPublicKey());

            rsaJsonWebKey.setPrivateKey(jsonWebKey.getPrivateKey());
            rsaJsonWebKey.setKeyId(jsonWebKey.getKeyId());

        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
        return rsaJsonWebKey;
    }
}
