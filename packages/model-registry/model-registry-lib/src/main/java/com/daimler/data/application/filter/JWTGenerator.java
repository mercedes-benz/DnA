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

package com.daimler.data.application.filter;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;

@Component
public class JWTGenerator {

	private static Logger log = LoggerFactory.getLogger(JWTGenerator.class);

	private static String SECRET_KEY;

	private static int TOKEN_EXPIRY;

	private static String KID;

	@Value("${jwt.secret.key}")
	public void setSecretKey(String secretKey) {
		SECRET_KEY = secretKey;
	}

	@Value("${jwt.secret.tokenExpiry}")
	public void setTokenExpiry(int tokenExpiry) {
		TOKEN_EXPIRY = tokenExpiry;
	}

	@Value("${jwt.secret.kid}")
	public void setKid(String kid) {
		KID = kid;
	}

	public static String generateJWT(String appId) {
		byte[] bytesEncoded = Base64.encodeBase64(SECRET_KEY.getBytes());
		final Map<String, Object> tokenData = new HashMap<>();
		tokenData.put("kid", KID);
		tokenData.put("nounce", Math.random());
		tokenData.put("appId", appId);
		final JwtBuilder jwtBuilder = Jwts.builder();
		jwtBuilder.setClaims(tokenData);
		jwtBuilder.setExpiration(new Date(System.currentTimeMillis() + TOKEN_EXPIRY * 60 * 1000));
		final String token = jwtBuilder.signWith(SignatureAlgorithm.HS256, new String(bytesEncoded)).compact();
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
			log.error("Error parsing JWT:{}", e.getMessage());
			return null;
		}
	}
}
