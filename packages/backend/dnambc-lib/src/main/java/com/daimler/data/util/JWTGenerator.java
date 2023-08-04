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

package com.daimler.data.util;

import com.daimler.data.controller.LoginController;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.xml.bind.DatatypeConverter;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class JWTGenerator {

	// private static Logger log = LoggerFactory.getLogger(JWTGenerator.class);

	private static String SECRET_KEY;

	private static int TOKEN_EXPIRY;

	@Value("${jwt.secret.key}")
	public void setSecretKey(String secretKey) {
		SECRET_KEY = secretKey;
	}

	@Value("${jwt.secret.tokenExpiry}")
	public void setTokenExpiry(int tokenExpiry) {
		TOKEN_EXPIRY = tokenExpiry;
	}

	public static String createJWT(Map<String, Object> tokenData) {

		final JwtBuilder jwtBuilder = Jwts.builder();
		jwtBuilder.setClaims(tokenData);
		final String token = jwtBuilder.signWith(SignatureAlgorithm.HS512, SECRET_KEY).compact();
		return token;
	}

	public static String createJWT(LoginController.UserInfo userInfo) {

		final Map<String, Object> tokenData = new HashMap<>();
		tokenData.put("id", userInfo.getId());
		tokenData.put("firstName", userInfo.getFirstName());
		tokenData.put("lastName", userInfo.getLastName());
		tokenData.put("email", userInfo.getEmail());
		tokenData.put("nounce", Math.random());
		tokenData.put("mobileNumber", userInfo.getMobileNumber());
		tokenData.put("department", userInfo.getDepartment());
		tokenData.put("digiRole", userInfo.getDigiRole());
		tokenData.put("divisionAdmins", userInfo.getDivisionAdmins());

		final JwtBuilder jwtBuilder = Jwts.builder();
		jwtBuilder.setClaims(tokenData);
		final String token = jwtBuilder.signWith(SignatureAlgorithm.HS512, SECRET_KEY).compact();
		return token;
	}

	public static String generateJWT(LoginController.UserInfo userInfo, String authToken) {

		final Map<String, Object> tokenData = new HashMap<>();
		tokenData.put("id", userInfo.getId());
		tokenData.put("firstName", userInfo.getFirstName());
		tokenData.put("lastName", userInfo.getLastName());
		tokenData.put("email", userInfo.getEmail());
		tokenData.put("nounce", Math.random());
		tokenData.put("mobileNumber", userInfo.getMobileNumber());
		tokenData.put("department", userInfo.getDepartment());
		tokenData.put("digiRole", userInfo.getDigiRole());
		tokenData.put("divisionAdmins", userInfo.getDivisionAdmins());
		tokenData.put("authToken", authToken);
		final JwtBuilder jwtBuilder = Jwts.builder();
		jwtBuilder.setClaims(tokenData);
		jwtBuilder.setExpiration(new Date(System.currentTimeMillis() + TOKEN_EXPIRY * 60 * 1000));
		final String token = jwtBuilder.signWith(SignatureAlgorithm.HS512, SECRET_KEY).compact();
		return token;
	}

	public static String refreshJWT(Claims claims, String authToken) {

		final Map<String, Object> tokenData = new HashMap<>();
		tokenData.put("id", (String) claims.get("id"));
		tokenData.put("firstName", (String) claims.get("firstName"));
		tokenData.put("lastName", (String) claims.get("lastName"));
		tokenData.put("email", (String) claims.get("email"));
		tokenData.put("nounce", Math.random());
		tokenData.put("mobileNumber", (String) claims.get("mobileNumber"));
		tokenData.put("department", (String) claims.get("department"));
		tokenData.put("digiRole", (List) claims.get("digiRole"));
		tokenData.put("divisionAdmins", (List) claims.get("divisionAdmins"));
		tokenData.put("authToken", authToken);
		final JwtBuilder jwtBuilder = Jwts.builder();
		jwtBuilder.setClaims(tokenData);
		jwtBuilder.setExpiration(new Date(System.currentTimeMillis() + TOKEN_EXPIRY * 60 * 1000));
		final String token = jwtBuilder.signWith(SignatureAlgorithm.HS512, SECRET_KEY).compact();
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
			log.error("Error parsing JWT:" + e.getMessage());
			return null;
		}
	}
}
