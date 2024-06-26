package com.example.EBook_Management_BE.components;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.EBook_Management_BE.entity.Token;
import com.example.EBook_Management_BE.entity.User;
import com.example.EBook_Management_BE.repositories.TokenRepository;
import com.example.EBook_Management_BE.services.token.ITokenRedisService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtTokenUtil {
	@Value("${jwt.expiration}")
	private int expiration;
	@Value("${jwt.secretKey}")
	private String secretKey;
	
	private final TokenRepository tokenRepository;
	private final ITokenRedisService tokenRedisService;
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenUtil.class);

	public String generateToken(User user) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("phoneNumber", user.getPhoneNumber());

		try {
			String token = Jwts.builder()
					.setClaims(claims)
					.setSubject(user.getPhoneNumber())
					.setExpiration(new Date(System.currentTimeMillis() + expiration * 1000L))
					.signWith(getSignKey(), SignatureAlgorithm.HS256)
					.compact();
			
			return token;
		} catch (Exception e) {
			System.err.println("Cannot create jwt token, erorr " + e.getMessage());
			return null;
		}
	}

	private Key getSignKey() {
		byte[] bytes = Decoders.BASE64.decode(secretKey);
		return Keys.hmacShaKeyFor(bytes);
	}
	
	private Claims extractAllClaims(String token) {
		return Jwts.parserBuilder()
				.setSigningKey(getSignKey())
				.build()
				.parseClaimsJws(token)
				.getBody();
	}
	
	public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = this.extractAllClaims(token);
		return claimsResolver.apply(claims);
	}
	
	public boolean isTokenExpired(String token) {
		Date expirationDate = this.extractClaim(token, Claims::getExpiration);
		
		return expirationDate.before(new Date());
	}
	
	public String extractPhoneNumber(String token) {
        return extractClaim(token, Claims::getSubject);
    }
	
	public boolean validateToken(String token, User userDetails) throws Exception {
        try {
            String phoneNumber = extractPhoneNumber(token);
            Token existingToken = tokenRedisService.getToken(token);
            if (existingToken == null) {
            	existingToken = tokenRepository.findByToken(token);
            	
            	tokenRedisService.saveToken(token, existingToken);
            }
            		
            if(existingToken == null ||
                    existingToken.isRevoked() == true ||
                    !(userDetails.getIsActive() == 1)
            ) {
                return false;
            }
            return (phoneNumber.equals(userDetails.getUsername()))
                    && !isTokenExpired(token);
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }

        return false;
    }
}
