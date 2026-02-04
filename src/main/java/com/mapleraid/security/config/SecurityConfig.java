package com.mapleraid.security.config;

import com.mapleraid.core.constant.Constants;
import com.mapleraid.core.utility.JsonWebTokenUtil;
import com.mapleraid.security.application.service.CustomOauth2UserDetailService;
import com.mapleraid.security.filter.ExceptionFilter;
import com.mapleraid.security.filter.GlobalLoggerFilter;
import com.mapleraid.security.filter.JsonWebTokenAuthenticationFilter;
import com.mapleraid.security.handler.common.DefaultAccessDeniedHandler;
import com.mapleraid.security.handler.common.DefaultAuthenticationEntryPoint;
import com.mapleraid.security.handler.login.Oauth2FailureHandler;
import com.mapleraid.security.handler.login.Oauth2SuccessHandler;
import com.mapleraid.user.application.port.out.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final Oauth2SuccessHandler oauth2SuccessHandler;
    private final Oauth2FailureHandler oauth2FailureHandler;
    private final CustomOauth2UserDetailService customOauth2UserDetailService;

    private final DefaultAccessDeniedHandler defaultAccessDeniedHandler;
    private final DefaultAuthenticationEntryPoint defaultAuthenticationEntryPoint;

    private final UserRepository userRepository;
    private final JsonWebTokenUtil jsonWebTokenUtil;

    @Value("${cookie.domain:localhost}")
    private String cookieDomain;

    @Value("${cookie.access-token-name:access_token}")
    private String accessTokenCookieName;

    @Value("${cookie.refresh-token-name:refresh_token}")
    private String refreshTokenCookieName;

    @Value("${jwt.refresh-token-validity-ms}")
    private Long cookieMaxAge;

    @Bean
    @Profile("!local")
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(configurer -> configurer
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(configurer -> configurer
                        .requestMatchers(Constants.NO_NEED_AUTH_URLS.toArray(String[]::new)).permitAll()
                        .requestMatchers(HttpMethod.GET, Constants.NO_NEED_AUTH_GET_URLS.toArray(String[]::new)).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oauth2SuccessHandler)
                        .failureHandler(oauth2FailureHandler)
                        .userInfoEndpoint(it -> it.userService(customOauth2UserDetailService))
                )
                .exceptionHandling(configurer -> configurer
                        .accessDeniedHandler(defaultAccessDeniedHandler)
                        .authenticationEntryPoint(defaultAuthenticationEntryPoint)
                )
                .addFilterBefore(
                        new JsonWebTokenAuthenticationFilter(
                                userRepository,
                                jsonWebTokenUtil,
                                cookieDomain,
                                accessTokenCookieName,
                                refreshTokenCookieName,
                                cookieMaxAge
                        ),
                        LogoutFilter.class
                )

                .addFilterBefore(
                        new ExceptionFilter(),
                        JsonWebTokenAuthenticationFilter.class
                )

                .addFilterBefore(
                        new GlobalLoggerFilter(),
                        ExceptionFilter.class
                )

                .getOrBuild();
    }

    @Bean
    @Profile("local")
    public SecurityFilterChain localSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> {
                })
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(configurer -> configurer
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(configurer -> configurer
                        .requestMatchers(Constants.NO_NEED_AUTH_URLS.toArray(String[]::new)).permitAll()
                        .requestMatchers(HttpMethod.GET, Constants.NO_NEED_AUTH_GET_URLS.toArray(String[]::new)).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oauth2SuccessHandler)
                        .failureHandler(oauth2FailureHandler)
                        .userInfoEndpoint(it -> it.userService(customOauth2UserDetailService))
                )
                .exceptionHandling(configurer -> configurer
                        .accessDeniedHandler(defaultAccessDeniedHandler)
                        .authenticationEntryPoint(defaultAuthenticationEntryPoint)
                )
                .addFilterBefore(
                        new JsonWebTokenAuthenticationFilter(
                                userRepository,
                                jsonWebTokenUtil,
                                cookieDomain,
                                accessTokenCookieName,
                                refreshTokenCookieName,
                                cookieMaxAge
                        ),
                        LogoutFilter.class
                )

                .addFilterBefore(
                        new ExceptionFilter(),
                        JsonWebTokenAuthenticationFilter.class
                )

                .addFilterBefore(
                        new GlobalLoggerFilter(),
                        ExceptionFilter.class
                )

                .getOrBuild();
    }
}
