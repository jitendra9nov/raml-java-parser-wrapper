package com.bhadouriya.raml.oauth;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.apache.commons.lang.StringUtils.*;
import static org.apache.commons.lang.StringUtils.trim;

public class AuthorizationFilter  extends AbstractAuthenticationProcessingFilter {


    public AuthorizationFilter(RequestMatcher include) {
        super(include);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        String path=request.getRequestURI().substring(request.getContextPath().length()).replaceAll("[/]+$","");

        Authentication authentication=null;
        String auth=request.getHeader("authorization");

        String token= trim(replace(auth,"Bearer ",""));

        Authentication requestAuth=new UsernamePasswordAuthenticationToken(token,token);

        try{
            authentication=getAuthenticationManager().authenticate(requestAuth);
        }
        catch (Exception e){
            handleException(request,response,new IllegalArgumentException(HttpStatus.FORBIDDEN.toString()));
        }

        return authentication;
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        String path=request.getRequestURI().substring(request.getContextPath().length()).replaceAll("[/]+$","");
        SecurityContextHolder.getContext().setAuthentication(authResult);

        UserInfo userInfo=UserInfo.class.cast(authResult.getDetails());

        chain.doFilter(new UserHttpServletRequest(request,userInfo),response);
    }

    private void handleException(HttpServletRequest request, HttpServletResponse response, Exception ex) {
        ApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(request.getServletContext());
        ModelAndView modelAndView=new ModelAndView(ex.getMessage());

        try{
            if(null!=modelAndView){
                modelAndView.getView().render(modelAndView.getModel(),request,response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }

    }
}
