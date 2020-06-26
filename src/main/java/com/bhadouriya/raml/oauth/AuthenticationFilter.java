package com.bhadouriya.raml.oauth;

import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.springframework.util.StringUtils.hasText;

public class AuthenticationFilter extends OncePerRequestFilter {

    SecurityUtil securityUtil;
    public AuthenticationFilter(SecurityUtil securityUtil) {
        this.securityUtil=securityUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        
        String path=request.getRequestURI().substring(request.getContextPath().length()).replaceAll("[/]+$","");
        
        UserInfo userInfo=new UserInfo();
        
        String token=getUseDetails(request,response,userInfo);
        
        if(hasText(token)){
            request=addJwtTokenHeader(request,token);
        }
        
        request=new UserHttpServletRequest(request,userInfo);
        
        filterChain.doFilter(request,response);
    }

    private HttpServletRequest addJwtTokenHeader(HttpServletRequest request, String token) {
        AddHeaderHttpServletRequest customHeader=new AddHeaderHttpServletRequest(request);

        customHeader.addHeader("authorization","Bearer "+token);

        return customHeader;

    }

    private String getUseDetails(HttpServletRequest request, HttpServletResponse response, UserInfo userInfo) {
    String token=null;
        try{
        //Get Details and from SSO or DB
        userInfo.setRole("USER");
        token=securityUtil.produceJwtToken(userInfo);

    }
    catch (Exception e){
        handleException(request,response,new IllegalArgumentException(HttpStatus.UNAUTHORIZED.toString()));
    }
    return token;

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

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        //return super.shouldNotFilter(request);
        return (hasText(request.getHeader("authourization"))) && new AntPathRequestMatcher("/index/**").matches(request);
    }

    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return false;
    }
}
