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
import io.micronaut.context.annotation.Value;

public class JWTGenerator {

	private static Logger log = LoggerFactory.getLogger(JWTGenerator.class);
	
	private static String SECRET_KEY;
	
	@Value("${jwt.secret.key}")
	public void setSecretKey(String secretKey) {
		SECRET_KEY = secretKey;
	}

	public static Claims decodeJWT(String jwt) {
		try {
			// This line will throw an exception if it is not a signed JWS (as expected)
			Claims claims = Jwts.parser().setSigningKey(DatatypeConverter.parseBase64Binary(SECRET_KEY))
					.parseClaimsJws(jwt).getBody();
			return claims;
		} catch (ExpiredJwtException | MalformedJwtException | SignatureException | UnsupportedJwtException
				| IllegalArgumentException e) {
			log.error("Error parsing JWT:{}", e.getMessage());
			return null;
		}
	}
	
}
