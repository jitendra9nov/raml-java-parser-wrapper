package com.bhadouriya.raml.oauth;

import com.bhadouriya.raml.validation.RamlException;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (null != auth) {
            //Log below
            String a = auth.getName() + request.getRequestURI();
        }
        handleException(request, response, new IllegalArgumentException(HttpStatus.FORBIDDEN.toString()));

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
