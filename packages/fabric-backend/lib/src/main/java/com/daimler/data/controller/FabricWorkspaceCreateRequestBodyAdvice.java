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

package com.daimler.data.controller;

import com.daimler.data.dto.fabricWorkspace.FabricWorkspaceCreateRequestVO;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.daimler.data.controller.exceptions.UnknownRequestPropertyException;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.json.MappingJacksonInputMessage;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

@ControllerAdvice
public class FabricWorkspaceCreateRequestBodyAdvice extends RequestBodyAdviceAdapter {

	private final ObjectMapper strictObjectMapper;

	public FabricWorkspaceCreateRequestBodyAdvice(ObjectMapper objectMapper) {
		this.strictObjectMapper = objectMapper.copy()
				.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
	}

	@Override
	public boolean supports(MethodParameter methodParameter, Type targetType,
			Class<? extends org.springframework.http.converter.HttpMessageConverter<?>> converterType) {
		return FabricWorkspaceCreateRequestVO.class.equals(targetType);
	}

	@Override
	public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter,
			Type targetType, Class<? extends org.springframework.http.converter.HttpMessageConverter<?>> converterType)
			throws IOException {
		byte[] body = StreamUtils.copyToByteArray(inputMessage.getBody());
		try {
			strictObjectMapper.readValue(body, FabricWorkspaceCreateRequestVO.class);
		} catch (UnrecognizedPropertyException exception) {
			String message = "Unrecognized field '" + exception.getPropertyName() + "' is not allowed at "
					+ formatParentPath(exception) + " of the request body";
			throw new UnknownRequestPropertyException(message, exception, inputMessage);
		} catch (JsonProcessingException exception) {
			// Let the regular message converter handle malformed or otherwise invalid JSON.
		}

		MappingJacksonInputMessage replayMessage =
				new MappingJacksonInputMessage(new ByteArrayInputStream(body), inputMessage.getHeaders());
		return replayMessage;
	}

	private String formatParentPath(UnrecognizedPropertyException exception) {
		List<JsonMappingException.Reference> path = exception.getPath();
		if (path == null || path.size() <= 1) {
			return "the top level";
		}

		StringBuilder formattedPath = new StringBuilder("'");
		boolean firstElement = true;
		for (JsonMappingException.Reference reference : path.subList(0, path.size() - 1)) {
			if (reference.getFieldName() != null) {
				if (!firstElement) {
					formattedPath.append('.');
				}
				formattedPath.append(reference.getFieldName());
				firstElement = false;
			} else if (reference.getIndex() >= 0) {
				formattedPath.append('[').append(reference.getIndex()).append(']');
			}
		}
		return formattedPath.append("'").toString();
	}
}
