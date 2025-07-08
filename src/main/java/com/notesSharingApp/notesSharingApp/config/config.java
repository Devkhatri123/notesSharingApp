package com.notesSharingApp.notesSharingApp.config;

import com.notesSharingApp.notesSharingApp.JWT.AuthEntryPoint;
import com.notesSharingApp.notesSharingApp.Service.userdetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import com.notesSharingApp.notesSharingApp.JWT.jwtFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class config {

    @Autowired
    userdetailsService userdetailsService;

//    @Autowired
//    jwtFilter jwtFilter;

    @Autowired
    private AuthEntryPoint authEntryPoint;
    @Bean
    public jwtFilter jwtFilter() {
        return new jwtFilter();
    }


    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception{
        return http.csrf(csrf->csrf.disable())
                .exceptionHandling(exception -> exception.authenticationEntryPoint(authEntryPoint))
                .authorizeHttpRequests(auth->{ auth
                .requestMatchers(HttpMethod.POST,"/v1/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET,"/v1/admin/**").hasRole("ADMIN")
                .requestMatchers("/v1/notes/admin/ApprovalPendingNotes").hasRole("ADMIN")
                .requestMatchers("/v1/notes/uploadNote").hasRole("STUDENT")
                .requestMatchers("/v1/auth/resendVerificationCode").hasRole("STUDENT")
                .anyRequest().permitAll();
        })
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter(), UsernamePasswordAuthenticationFilter.class)
                .build();
    }
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder(13);
    }
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception{
        return authenticationConfiguration.getAuthenticationManager();
    }
    @Bean
    AuthenticationProvider daoAuthenticationProvider(){
        DaoAuthenticationProvider auth = new DaoAuthenticationProvider();
        auth.setUserDetailsService(userdetailsService);
        auth.setPasswordEncoder(passwordEncoder());
        return auth;
    }
    @Bean
    public WebMvcConfigurer corsConfigurer(){
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
               registry.addMapping("/v1/subject/**")
                        .allowedOrigins("http://localhost:5173/")
                        .allowedMethods("GET");
            }
        };
    }
}
