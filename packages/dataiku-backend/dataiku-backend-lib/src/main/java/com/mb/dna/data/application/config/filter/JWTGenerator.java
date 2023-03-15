package com.mb.dna.data.application.config.filter;

import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.inject.Singleton;

@Singleton
public class JWTGenerator {

	private static Logger log = LoggerFactory.getLogger(JWTGenerator.class);
	
	public static Claims decodeJWT(String jwt,String secretKey) {
		try {
			log.info("Parsing jwt {} with secret {} ",jwt,secretKey);
			Claims claims = Jwts.parser().setSigningKey(DatatypeConverter.parseBase64Binary(secretKey))
					.parseClaimsJws(jwt).getBody();
			return claims;
		} catch (Exception e) {
			log.error("Error parsing JWT:{}", e.getMessage());
			return null;
		}
	}
	
}
