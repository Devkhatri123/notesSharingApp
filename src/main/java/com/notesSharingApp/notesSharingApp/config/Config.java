package com.notesSharingApp.notesSharingApp.config;

import com.notesSharingApp.notesSharingApp.JWT.AuthEntryPoint;
import com.notesSharingApp.notesSharingApp.Service.UserDetailsService;
import com.notesSharingApp.notesSharingApp.Enum.Role;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class Config {

    @Autowired
    UserDetailsService userdetailsService;

    @Autowired
    private AuthEntryPoint authEntryPoint;
    @Bean
    public jwtFilter jwtFilter() {
        return new jwtFilter();
    }


    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception{
        return http.csrf(csrf->csrf.disable())

                .cors(Customizer.withDefaults())

               //.exceptionHandling(exception -> exception.authenticationEntryPoint(new AuthEntryPoint()))
                .authorizeHttpRequests(auth->{ auth
                .requestMatchers(HttpMethod.POST,"/v1/admin/**").hasAnyRole(Role.ADMIN.name(),Role.MANAGER.name())
                .requestMatchers("v1/auth/signUp").permitAll()
                .requestMatchers("v1/auth/login").permitAll()
                .requestMatchers("v1/auth/resendVerificationCode").permitAll()
                .requestMatchers("v1/auth/resetPasswordToken/{email}").permitAll()
                .requestMatchers("v1/auth/resetPassword").permitAll()
                .requestMatchers("v1/auth/verify").permitAll()
                .requestMatchers("/v1/notes").permitAll()
                .requestMatchers("/v1/notes/note/{noteID}").permitAll()
                .requestMatchers("/v1/subject/all").permitAll()
                .requestMatchers("v1/manager/**").hasRole(Role.MANAGER.name())
                .anyRequest().authenticated();
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
    public CorsConfigurationSource corsConfigurationSource(){
        final CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("https://study-share-delta.vercel.app"));
        configuration.setAllowedHeaders(List.of(
                HttpHeaders.AUTHORIZATION,
                HttpHeaders.CONTENT_TYPE,
                HttpHeaders.ACCEPT));
        configuration.setAllowedMethods(List.of("GET","POST","PUT","DELETE"));
        configuration.setAllowCredentials(true);
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    @Bean
    public ModelMapper modelMapper(){
        return new ModelMapper();
    }
}
