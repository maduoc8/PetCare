package com.petcare.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(RecursoNoEncontradoException.class)
	public ResponseEntity<ErrorResponse> noEncontrado(RecursoNoEncontradoException ex) {
		return respuesta(HttpStatus.NOT_FOUND, ex.getMessage(), null);
	}

	@ExceptionHandler(ConflictoException.class)
	public ResponseEntity<ErrorResponse> conflicto(ConflictoException ex) {
		return respuesta(HttpStatus.CONFLICT, ex.getMessage(), null);
	}

	@ExceptionHandler(SolicitudInvalidaException.class)
	public ResponseEntity<ErrorResponse> solicitudInvalida(SolicitudInvalidaException ex) {
		return respuesta(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> validacion(MethodArgumentNotValidException ex) {
		Map<String, String> errores = new LinkedHashMap<>();
		for (FieldError error : ex.getBindingResult().getFieldErrors()) {
			errores.putIfAbsent(error.getField(), error.getDefaultMessage());
		}
		return respuesta(HttpStatus.BAD_REQUEST, "Hay datos inválidos en la solicitud", errores);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponse> jsonIlegible(HttpMessageNotReadableException ex) {
		return respuesta(HttpStatus.BAD_REQUEST,
				"El cuerpo de la solicitud no es un JSON válido o tiene un campo con formato incorrecto (fechas: 2026-11-20 / 2026-11-20T10:30:00)",
				null);
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ErrorResponse> tipoIncorrecto(MethodArgumentTypeMismatchException ex) {
		return respuesta(HttpStatus.BAD_REQUEST,
				"El parámetro '" + ex.getName() + "' tiene un valor inválido: " + ex.getValue(), null);
	}

	private ResponseEntity<ErrorResponse> respuesta(HttpStatus status, String mensaje, Map<String, String> errores) {
		ErrorResponse cuerpo = new ErrorResponse(status.value(), status.getReasonPhrase(), mensaje, errores, LocalDateTime.now());
		return ResponseEntity.status(status).body(cuerpo);
	}
}
