package com.bhadouriya.raml.oauth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {


    private static final List<RequestMatcher> publicUrls = new ArrayList<>();
    private static final List<RequestMatcher> protectedUrls = new ArrayList<>();
    private static final List<RequestMatcher> privateUrls = new ArrayList<>();
    private static final OrRequestMatcher PUBLIC = new OrRequestMatcher(publicUrls);
    private static final OrRequestMatcher PROTECTED = new OrRequestMatcher(protectedUrls);
    private static final OrRequestMatcher PRIVATE = new OrRequestMatcher(privateUrls);
    private static final OrRequestMatcher INCLUDE = new OrRequestMatcher(PUBLIC, PROTECTED, PRIVATE);
    private static final NegatedRequestMatcher EXCLUDE = new NegatedRequestMatcher(INCLUDE);

    static {
        publicUrls.add(new AntPathRequestMatcher("/api/dome", HttpMethod.POST.name()));
        publicUrls.add(new AntPathRequestMatcher("/api/dome/{id:(?s).*}/do", HttpMethod.GET.name()));
    }

    static {
        protectedUrls.add(new AntPathRequestMatcher("/api/dome", HttpMethod.POST.name()));
        protectedUrls.add(new AntPathRequestMatcher("/api/dome/{id:(?s).*}/do", HttpMethod.GET.name()));
    }

    static {
        privateUrls.add(new AntPathRequestMatcher("/api/dome", HttpMethod.POST.name()));
        privateUrls.add(new AntPathRequestMatcher("/api/dome/{id:(?s).*}/do", HttpMethod.GET.name()));
    }

    private final AuthenticationProvider provider;

    public SecurityConfig(final @Autowired AuthenticationProvider authenticationProvider) {
        super();
        this.provider = authenticationProvider;
    }


    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(provider);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).
                and().exceptionHandling().accessDeniedHandler(accessDeniedHandler()).and()
                .authenticationProvider(provider)
                .addFilterBefore(authorizationFilter().getFilter(), AnonymousAuthenticationFilter.class)
                .authorizeRequests()
                .requestMatchers(PRIVATE).hasAuthority("ADMIN")
                .requestMatchers(PROTECTED).hasAnyAuthority("ADMIN", "USER")
                .requestMatchers(PUBLIC).hasAnyAuthority("ADMIN", "USER", "VIEWER")
                .requestMatchers(EXCLUDE).permitAll()
                .and().csrf().disable().formLogin().disable().httpBasic().disable().logout().disable();
    }

    @Bean
    public FilterRegistrationBean authorizationFilter() throws Exception {
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();

        AuthorizationFilter authorizationFilter = new AuthorizationFilter(INCLUDE);
        authorizationFilter.setAuthenticationManager(authenticationManager());
        registrationBean.setName("authorizationFilter");
        registrationBean.setFilter(authorizationFilter);
        registrationBean.setOrder(2);
        return registrationBean;
    }

    @Bean
    AuthenticationEntryPoint forbiddenEntryPoint() {
        return new HttpStatusEntryPoint(HttpStatus.FORBIDDEN);
    }

    @Bean
    AccessDeniedHandler accessDeniedHandler() {
        return new CustomAccessDeniedHandler();
    }
}
