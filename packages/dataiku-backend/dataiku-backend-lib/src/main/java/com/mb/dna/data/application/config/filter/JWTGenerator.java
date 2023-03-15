package com.mb.dna.data.application.config.filter;

import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mb.dna.data.application.adapter.dna.DnaClientConfig;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class JWTGenerator {

	private static Logger log = LoggerFactory.getLogger(JWTGenerator.class);
	
	@Inject
	static
	DnaClientConfig dnaClientConfig;

	public static Claims decodeJWT(String jwt) {
		try {
			log.info("Parsing jwt {} with secret {} ",jwt,dnaClientConfig.getJwt());
			Claims claims = Jwts.parser().setSigningKey(DatatypeConverter.parseBase64Binary(dnaClientConfig.getJwt()))
					.parseClaimsJws(jwt).getBody();
			return claims;
		} catch (Exception e) {
			log.error("Error parsing JWT:{}", e.getMessage());
			return null;
		}
	}
	
}
