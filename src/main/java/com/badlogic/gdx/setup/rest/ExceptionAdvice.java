package com.badlogic.gdx.setup.rest;

import java.util.NoSuchElementException;

import javax.naming.AuthenticationException;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class ExceptionAdvice {

	@ResponseBody
	@ExceptionHandler(NotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	String notFoundHandler(NotFoundException ex) {
		return ex.getMessage();
	}

	@ResponseBody
	@ExceptionHandler(NoSuchElementException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	String notFoundHandler(NoSuchElementException ex) {
		return ex.getMessage();
	}

	@ResponseBody
	@ExceptionHandler(IllegalArgumentException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	String notFoundHandler(IllegalArgumentException ex) {
		return ex.getMessage();
	}
	
	@ResponseBody
	@ExceptionHandler(AuthenticationException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	String authFailedHandler(AuthenticationException aex) {
		return aex.getMessage();
	}
}
