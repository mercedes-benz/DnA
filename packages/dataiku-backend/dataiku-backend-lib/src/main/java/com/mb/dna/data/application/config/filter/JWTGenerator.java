package com.mb.dna.data.application.config.filter;

import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;

@Singleton
public class JWTGenerator {

	private static Logger log = LoggerFactory.getLogger(JWTGenerator.class);
	
	@Value("${jwt.secret.key}")
	private static String secret_key;

	public static Claims decodeJWT(String jwt) {
		try {
			Claims claims = Jwts.parser().setSigningKey(DatatypeConverter.parseBase64Binary(secret_key))
					.parseClaimsJws(jwt).getBody();
			return claims;
		} catch (Exception e) {
			log.error("Error parsing JWT:{}", e.getMessage());
			return null;
		}
	}
	
}
