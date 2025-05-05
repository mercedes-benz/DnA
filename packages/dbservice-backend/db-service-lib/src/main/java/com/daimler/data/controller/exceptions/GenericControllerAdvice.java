package com.daimler.data.controller.exceptions;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class GenericControllerAdvice extends ResponseEntityExceptionHandler {

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

	// @Override
	// protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex,
	// 		HttpHeaders headers, HttpStatus status, WebRequest request) {
	// 	String unsupported = "Unsupported content type: " + ex.getContentType();
	// 	String supported = "Supported content types: " + MediaType.toString(ex.getSupportedMediaTypes());
	// 	MessageDescription unsupportedMsgDesc = new MessageDescription(unsupported);
	// 	MessageDescription supportedMsgDesc = new MessageDescription(supported);
	// 	GenericMessage customMessageDto = new GenericMessage();
	// 	customMessageDto.addErrors(unsupportedMsgDesc);
	// 	customMessageDto.addWarnings(supportedMsgDesc);
	// 	return new ResponseEntity(customMessageDto, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
	// }

	// @Override
	// protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
	// 		HttpHeaders headers, HttpStatus status, WebRequest request) {
	// 	Throwable mostSpecificCause = ex.getMostSpecificCause();
	// 	GenericMessage errorMessageDto;
	// 	if (mostSpecificCause != null) {
	// 		String exceptionName = mostSpecificCause.getClass().getName();
	// 		String message = mostSpecificCause.getMessage();
	// 		String consolidatedErrMsg = exceptionName + ";" + message;
	// 		MessageDescription consolidatedErrMsgDesc = new MessageDescription(consolidatedErrMsg);
	// 		errorMessageDto = new GenericMessage();
	// 		errorMessageDto.addErrors(consolidatedErrMsgDesc);
	// 	} else {
	// 		MessageDescription errMsgDesc = new MessageDescription(ex.getMessage());
	// 		errorMessageDto = new GenericMessage();
	// 		errorMessageDto.addErrors(errMsgDesc);
	// 	}
	// 	return new ResponseEntity(errorMessageDto, HttpStatus.BAD_REQUEST);
	// }

	// @Override
	// protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
	// 		HttpHeaders headers, HttpStatus status, WebRequest request) {
	// 	List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
	// 	List<ObjectError> globalErrors = ex.getBindingResult().getGlobalErrors();
	// 	List<MessageDescription> errors = new ArrayList<>();

	// 	String error;
	// 	for (FieldError fieldError : fieldErrors) {
	// 		error = "";
	// 		error = fieldError.getField() + ", " + fieldError.getDefaultMessage();
	// 		MessageDescription msgDesc = new MessageDescription(error);
	// 		errors.add(msgDesc);
	// 	}
	// 	for (ObjectError objectError : globalErrors) {
	// 		error = "";
	// 		error = objectError.getObjectName() + ", " + objectError.getDefaultMessage();
	// 		MessageDescription msgDesc = new MessageDescription(error);
	// 		errors.add(msgDesc);
	// 	}
	// 	GenericMessage errorMessageDto = new GenericMessage();
	// 	errorMessageDto.setErrors(errors);
	// 	Object result = errorMessageDto;

	// 	return new ResponseEntity(result, HttpStatus.BAD_REQUEST);
	// }

}

