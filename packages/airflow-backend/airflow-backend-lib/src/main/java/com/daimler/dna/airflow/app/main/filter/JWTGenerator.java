/* LICENSE START
 * 
 * MIT License
 * 
 * Copyright (c) 2019 Daimler TSS GmbH
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * LICENSE END 
 */

package com.daimler.dna.airflow.app.main.filter;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.daimler.dna.airflow.controller.LoginController;

import javax.xml.bind.DatatypeConverter;

import java.security.SignatureException;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class JWTGenerator {

	private static Logger log = LoggerFactory.getLogger(JWTGenerator.class);

	private static String SECRET_KEY;

	@Value("${jwt.secret.key}")
	public void setSecretKey(String secretKey) {
		SECRET_KEY = secretKey;
	}

	public static String createJWT(Map<String, Object> tokenData) {

		final JwtBuilder jwtBuilder = Jwts.builder();
		jwtBuilder.setClaims(tokenData);
		final String token = jwtBuilder.signWith(SignatureAlgorithm.HS512, SECRET_KEY).compact();
		return token;
	}

	public static String createJWT(LoginController.UserInfo userInfo) {
		log.debug("creating jwt token..");
		final Map<String, Object> tokenData = new HashMap<>();
		tokenData.put("id", userInfo.getId());
		tokenData.put("firstName", userInfo.getFirstName());
		tokenData.put("lastName", userInfo.getLastName());
		tokenData.put("email", userInfo.getEmail());
		tokenData.put("nounce", Math.random());
		tokenData.put("mobileNumber", userInfo.getMobileNumber());
		tokenData.put("department", userInfo.getDepartment());
		// tokenData.put("digiRole", userInfo.getDigiRole());

		final JwtBuilder jwtBuilder = Jwts.builder();
		jwtBuilder.setClaims(tokenData);
		final String token = jwtBuilder.signWith(SignatureAlgorithm.HS512, SECRET_KEY).compact();
		log.debug("jwt token successfully created.");
		return token;
	}

	public static Claims decodeJWT(String jwt) {
		try {
			// This line will throw an exception if it is not a signed JWS (as expected)
			Claims claims = Jwts.parser().setSigningKey(SECRET_KEY.getBytes())
					.parseClaimsJws(jwt).getBody();
			return claims;
		} catch (ExpiredJwtException | MalformedJwtException | SignatureException | UnsupportedJwtException
				| IllegalArgumentException e) {
			log.error("Unable to decode jwt ::{}" + e.getMessage());
			return null;
		}
	}
}
