package com.notesSharingApp.notesSharingApp.JWT;

import com.notesSharingApp.notesSharingApp.Service.AuthenticationService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import com.notesSharingApp.notesSharingApp.model.userdetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.notesSharingApp.notesSharingApp.Service.JwtService;
import com.notesSharingApp.notesSharingApp.Service.UserDetailsService;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Component
public class jwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserDetailsService userdetailsService;
    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    @Qualifier("handlerExceptionResolver")
    private HandlerExceptionResolver resolver;

    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException{
        String header = request.getHeader("Authorization");
        String username = null;
        String token = null;
        if(header != null && header.startsWith("Bearer")){
            token = header.substring(7);
        }
        if(token == null){
            Cookie[] cookies = request.getCookies();
            if(cookies != null){
                for (Cookie cookie : cookies){
                    if("jwt".equals(cookie.getName())){
                        token = cookie.getValue();
                        break;
                    }
                }
            }
        }
        if (token != null) {
                try {
                    username = jwtService.extractUsername(token);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (ExpiredJwtException e) {
                    resolver.resolveException(request, response, null, e);
                    return;
                } catch (MalformedJwtException e) {
                    resolver.resolveException(request, response, null, e);
                    return;
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    userdetails user = null;
                    try {
                        user = userdetailsService.loadUserByUsername(username);
                    } catch (UsernameNotFoundException e) {
                        authenticationService.logout(response);
                        //return;
                    }
                    boolean validateToken = jwtService.isTokenValid(token, user);
                    if (validateToken) {
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            }
      filterChain.doFilter(request,response);
  }
}
