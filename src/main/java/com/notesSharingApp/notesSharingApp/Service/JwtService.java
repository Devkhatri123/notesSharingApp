package com.notesSharingApp.notesSharingApp.Service;

import com.notesSharingApp.notesSharingApp.model.user;
import com.notesSharingApp.notesSharingApp.model.userdetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {
    @Value("${spring.jwt.secret.key}")
    private String SECRET_KEY;

    private SecretKey secretKey;

    public String generateToken(user user){
        Map<String,String> claim = new HashMap<>();
        claim.put("email",user.getUniversityEmail());
        claim.put("role",user.getRole());
        return Jwts.builder()
                .subject(user.getUniversityEmail())
                .issuedAt(new Date())
                .claims(claim)
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(generateKey(), Jwts.SIG.HS256)
                .compact();
    }
    private SecretKey generateKey(){
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private <T> T extractClaim(String token, Function<Claims,T> claimResolver){
        Claims claims = extractClaims(token);
        return claimResolver.apply(claims);
    }
    public String extractUsername(String token){
        return extractClaim(token,Claims::getSubject);
    }
    private Date getExpiration(String token){
        return extractClaim(token,Claims::getExpiration);
    }
    private boolean isTokenExpired(String token){
        return getExpiration(token).before(new Date());
    }
    public boolean isTokenValid(String token, userdetails user){
        String username = extractUsername(token);
        return (username.equals(user.getUser().getUniversityEmail()) && !isTokenExpired(token));
    }
    private Claims extractClaims(String token){
       return Jwts.parser()
               .verifyWith(generateKey())
               .build()
               .parseSignedClaims(token)
               .getPayload();
    }
}
