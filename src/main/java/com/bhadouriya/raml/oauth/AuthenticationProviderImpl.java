package com.bhadouriya.raml.oauth;

import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.springframework.util.StringUtils.*;

@Component
public class AuthenticationProviderImpl extends AbstractUserDetailsAuthenticationProvider {

    private static final Logger LOGGER = Logger.getLogger(AuthenticationProviderImpl.class.getName());

    @Autowired
    SecurityUtil securityUtil;

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        //Do something with below details
        Optional<? extends GrantedAuthority> role =userDetails.getAuthorities().stream().findFirst();
        UserInfo userInfo=new UserInfo(userDetails.getUsername(),userDetails.getPassword(),(
                role.isPresent()?role.get().getAuthority():"VIEWER"
                ))

        ;

        authentication.setDetails("pass any object from above details in chain filter.");
    }

    @Override
    protected UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {

        User user = null;

        String token = !isEmpty(authentication.getCredentials()) ? String.valueOf(authentication.getCredentials()) : null;

        String otherInfo = !isEmpty(authentication.getPrincipal()) ? String.valueOf(authentication.getPrincipal()) : null;

        try {
            if (hasText(token)) {
                JwtClaims jwtClaims = securityUtil.consumeJwtToken(token);

                LOGGER.log(Level.INFO, "JWT validation succeeded so continue the request..");

                UserInfo userInfo = securityUtil.parseJson(jwtClaims.getStringClaimValue("UserInfo"), UserInfo.class);
                user = new User(userInfo.getUserName(), userInfo.getPassword(), true, true, true, true, AuthorityUtils.createAuthorityList(userInfo.getRole()));
            } else {
                LOGGER.log(Level.INFO, "JWT validation failed! Missing authorization");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "JWT validation failed! Missing authorization",e);
        }
        if(null==user){
            throw new UsernameNotFoundException("Cannot find use with authentication token="+token);
        }
        return user;
    }
}
