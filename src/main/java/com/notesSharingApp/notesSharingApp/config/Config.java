package com.notesSharingApp.notesSharingApp.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.notesSharingApp.notesSharingApp.Service.UserDetailsService;
import com.notesSharingApp.notesSharingApp.Enum.Role;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
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
import java.util.concurrent.Executor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableAsync
public class Config {

    @Autowired
    UserDetailsService userdetailsService;

    @Value("${cloudinary.cloud_name}")
    private String cloudName;

    @Value("${cloudinary.api_key}")
    private String apiKey;

    @Value("${cloudinary.api_secret}")
    private String apiSecret;
    @Value("${cloud.aws.credentials.secret-key}")
    private String aws_Secret_key;
    @Value("${cloud.aws.credentials.access-key}")
    private String aws_access_key;
    @Value("${cloud.aws.static.region}")
    private String aws_s3_region;
    @Bean
    public jwtFilter jwtFilter() {
        return new jwtFilter();
    }


    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception{
        return http.csrf(csrf->csrf.disable())

                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                //.exceptionHandling(exception -> exception.authenticationEntryPoint(new AuthEntryPoint()))
                .authorizeHttpRequests(auth->{ auth
                .requestMatchers(HttpMethod.POST,"/v1/admin/**").hasAnyRole(Role.ADMIN.name(),Role.MANAGER.name())
                .requestMatchers("/v1/auth/signUp").permitAll()
                .requestMatchers("/v1/auth/login").permitAll()
                .requestMatchers("/v1/auth/resendVerificationCode").permitAll()
                .requestMatchers("/v1/auth/resetPasswordToken/{email}").permitAll()
                .requestMatchers("/v1/auth/resetPassword").permitAll()
                .requestMatchers("/v1/auth/verify").permitAll()
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
    public CorsConfigurationSource corsConfigurationSource() {
        final CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("https://study-share-eta.vercel.app","http://localhost:5173"));
        configuration.setExposedHeaders(List.of(
                HttpHeaders.AUTHORIZATION,
                HttpHeaders.CONTENT_TYPE,
                HttpHeaders.ACCEPT,
                HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
                "user-TimeZone"
        ));
        configuration.setAllowedHeaders(List.of(
                HttpHeaders.AUTHORIZATION,
                HttpHeaders.CONTENT_TYPE,
                HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
                HttpHeaders.ACCEPT,
                "Origin",
                "X-Requested-With",
                "Cookie",
                "ngrok-skip-browser-warning",
                "user-TimeZone"
        ));
        configuration.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
        configuration.setAllowCredentials(true);

        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public ModelMapper modelMapper(){
        return new ModelMapper();
    }

    @Bean("asyncTask")
    public Executor asyncTaskExecutor(){
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(50);
        threadPoolTaskExecutor.setMaxPoolSize(100);
        threadPoolTaskExecutor.setQueueCapacity(25);
        threadPoolTaskExecutor.setThreadNamePrefix("asyncTaskThread-");
        threadPoolTaskExecutor.initialize();
        return threadPoolTaskExecutor;

    }

    // Cloud Config
    @Bean
    public Cloudinary getCloudinary(){
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret));
    }

    // S3 Configuration
    @Bean
    public AmazonS3 generateAmazonS3(){
        AWSCredentials awsCredentials = new BasicAWSCredentials(aws_access_key,aws_Secret_key);
        return AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .withRegion(aws_s3_region).build();
    }
}
