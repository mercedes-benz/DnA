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
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Value;

@Bean
public class JWTGenerator {

	private static Logger log = LoggerFactory.getLogger(JWTGenerator.class);
	
	@Value("${jwt.secret.key}")
	private static String secret_key;

	public static Claims decodeJWT(String jwt) {
		try {
			// This line will throw an exception if it is not a signed JWS (as expected)
			secret_key = "oeRaYY7Wo24sDqKSX3IM9ASGmdGPmkTd9jo1QTy4b7P9Ze5_9hKolVX8xNrQDcNRfVEdTZNOuOyqEGhXEbdJI-ZQ19k_o9MI0y3eZN2lp9jow55FfXMiINEdt1XR85VipRLSOkT6kSpzs2x-jbLDiz9iFVzkd81YKxMgPA7VfZeQUm4n-mOmnWMaVX30zGFU4L3oPBctYKkl4dYfqYWqRNfrgPJVi5DGFjywgxx0ASEiJHtV72paI3fDR2XwlSkyhhmY-ICjCRmsJN4fX1pdoL8a18-aQrvyu4j0Os6dVPYIoPvvY0SAZtWYKHfM15g7A3HD4cVREf9cUsprCRK93w";
			Claims claims = Jwts.parser().setSigningKey(DatatypeConverter.parseBase64Binary(secret_key))
					.parseClaimsJws(jwt).getBody();
			return claims;
		} catch (ExpiredJwtException | MalformedJwtException | SignatureException | UnsupportedJwtException
				| IllegalArgumentException e) {
			log.error("Error parsing JWT:{}", e.getMessage());
			return null;
		}
	}
	
}
