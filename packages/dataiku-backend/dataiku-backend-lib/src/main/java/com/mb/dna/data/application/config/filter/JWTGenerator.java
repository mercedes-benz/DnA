package com.mb.dna.data.application.config.filter;

import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.inject.Singleton;

@Singleton
public class JWTGenerator {

	private static Logger log = LoggerFactory.getLogger(JWTGenerator.class);
	
	public static Claims decodeJWT(String jwt,String secretKey) {
		try {
			Claims claims = Jwts.parser().setSigningKey(secretKey.getBytes())
					.parseClaimsJws(jwt).getBody();
			return claims;
		} catch (ExpiredJwtException e) {
            log.error("Expired JWT. Error parsing JWT:{}", e.getMessage());
            return e.getClaims();
        } catch (MalformedJwtException | SignatureException | UnsupportedJwtException
                 | IllegalArgumentException e) {
            log.error("Error parsing JWT:{}", e.getMessage());
            return null;
        }
	}
	
}
