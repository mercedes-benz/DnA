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

package com.daimler.data.controller.exceptions;

import com.daimler.data.dto.fabricWorkspace.FabricWorkspaceCreateRequestVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonInputMessage;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.core.MethodParameter;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class GenericControllerAdvice extends ResponseEntityExceptionHandler implements RequestBodyAdvice {

	private final ObjectMapper strictObjectMapper;

	public GenericControllerAdvice(ObjectMapper objectMapper) {
		this.strictObjectMapper = objectMapper.copy()
				.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
	}

	@Override
	public boolean supports(MethodParameter methodParameter, Type targetType,
			Class<? extends HttpMessageConverter<?>> converterType) {
		return FabricWorkspaceCreateRequestVO.class.equals(targetType);
	}

	@Override
	public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter,
			Type targetType, Class<? extends HttpMessageConverter<?>> converterType) throws IOException {
		byte[] body = StreamUtils.copyToByteArray(inputMessage.getBody());
		try {
			strictObjectMapper.readValue(body, FabricWorkspaceCreateRequestVO.class);
		} catch (UnrecognizedPropertyException exception) {
			String parentPath = formatParentPath(exception);
			String cleanMessage = "Unrecognized field '" + exception.getPropertyName()
					+ "' \u2014 not allowed at " + parentPath + " of the request body";
			throw new HttpMessageNotReadableException(cleanMessage, exception, inputMessage);
		} catch (JsonProcessingException exception) {
			// Let the regular message converter handle malformed or otherwise invalid JSON.
		}

		return new MappingJacksonInputMessage(new ByteArrayInputStream(body), inputMessage.getHeaders());
	}

	@Override
	public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter,
			Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
		return body;
	}

	@Override
	public Object handleEmptyBody(Object body, HttpInputMessage inputMessage, MethodParameter parameter,
			Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
		return body;
	}

	private String formatParentPath(UnrecognizedPropertyException exception) {
		List<JsonMappingException.Reference> path = exception.getPath();
		if (path == null || path.isEmpty()) {
			return "this level";
		}

		StringBuilder formattedPath = new StringBuilder("path '");
		boolean firstElement = true;
		for (JsonMappingException.Reference reference : path) {
			if (reference.getFieldName() != null) {
				if (!firstElement) {
					formattedPath.append('.');
				}
				formattedPath.append(reference.getFieldName());
			} else if (reference.getIndex() >= 0) {
				formattedPath.append('[').append(reference.getIndex()).append(']');
			}
			firstElement = false;
		}
		return formattedPath.append("'").toString();
	}

	@ExceptionHandler(Throwable.class)
	public ResponseEntity<GenericMessage> showErrMsg(final Throwable e) {
		String message = e.getMessage();
		if (message == null || "".equalsIgnoreCase(message)) {
			message = "Internal error occured";
		}
		MessageDescription errorMsgDesc = new MessageDescription(message);
		GenericMessage customMessageDto = new GenericMessage();
		customMessageDto.addErrors(errorMsgDesc);
		return new ResponseEntity<GenericMessage>(customMessageDto, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@Override
	protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		String unsupported = "Unsupported content type: " + ex.getContentType();
		String supported = "Supported content types: " + MediaType.toString(ex.getSupportedMediaTypes());
		MessageDescription unsupportedMsgDesc = new MessageDescription(unsupported);
		MessageDescription supportedMsgDesc = new MessageDescription(supported);
		GenericMessage customMessageDto = new GenericMessage();
		customMessageDto.addErrors(unsupportedMsgDesc);
		customMessageDto.addWarnings(supportedMsgDesc);
		return new ResponseEntity(customMessageDto, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
	}

	@Override
	protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		if (ex.getCause() instanceof UnrecognizedPropertyException) {
			GenericMessage errorMessageDto = new GenericMessage();
			errorMessageDto.addErrors(new MessageDescription(ex.getMessage()));
			return new ResponseEntity(errorMessageDto, HttpStatus.BAD_REQUEST);
		}
		Throwable mostSpecificCause = ex.getMostSpecificCause();
		GenericMessage errorMessageDto;
		if (mostSpecificCause != null) {
			String exceptionName = mostSpecificCause.getClass().getName();
			String message = mostSpecificCause.getMessage();
			String consolidatedErrMsg = exceptionName + ";" + message;
			MessageDescription consolidatedErrMsgDesc = new MessageDescription(consolidatedErrMsg);
			errorMessageDto = new GenericMessage();
			errorMessageDto.addErrors(consolidatedErrMsgDesc);
		} else {
			MessageDescription errMsgDesc = new MessageDescription(ex.getMessage());
			errorMessageDto = new GenericMessage();
			errorMessageDto.addErrors(errMsgDesc);
		}
		return new ResponseEntity(errorMessageDto, HttpStatus.BAD_REQUEST);
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
		List<ObjectError> globalErrors = ex.getBindingResult().getGlobalErrors();
		List<MessageDescription> errors = new ArrayList<>();

		String error;
		for (FieldError fieldError : fieldErrors) {
			error = "";
			error = fieldError.getField() + ", " + fieldError.getDefaultMessage();
			MessageDescription msgDesc = new MessageDescription(error);
			errors.add(msgDesc);
		}
		for (ObjectError objectError : globalErrors) {
			error = "";
			error = objectError.getObjectName() + ", " + objectError.getDefaultMessage();
			MessageDescription msgDesc = new MessageDescription(error);
			errors.add(msgDesc);
		}
		GenericMessage errorMessageDto = new GenericMessage();
		errorMessageDto.setErrors(errors);
		Object result = errorMessageDto;

		return new ResponseEntity(result, HttpStatus.BAD_REQUEST);
	}

	 @ExceptionHandler(OpenMetadataClientException.class)
    public ResponseEntity<String> handleOpenMetadataClientException(OpenMetadataClientException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("OpenMetadata operation failed: " + ex.getMessage());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleEntityNotFoundException(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ex.getMessage());
    }

    @ExceptionHandler(EntityAlreadyExistsException.class)
    public ResponseEntity<String> handleEntityAlreadyExistsException(EntityAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ex.getMessage());
    }


}
