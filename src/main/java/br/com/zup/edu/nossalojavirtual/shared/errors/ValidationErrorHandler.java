package br.com.zup.edu.nossalojavirtual.shared.errors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.util.List;

@RestControllerAdvice
public class ValidationErrorHandler {

    private final Logger logger = LoggerFactory.getLogger(ValidationErrorHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValiationErrorMessage(MethodArgumentNotValidException ex, WebRequest webRequest) {
        BindingResult bindingResult = ex.getBindingResult();
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        List<String> errorMessages = fieldErrors.stream()
                .map(fieldError -> String.format("O campo %s %s", fieldError.getField(),
                        fieldError.getDefaultMessage()
                ))
                .toList();
        var pathUri = ((ServletWebRequest) webRequest).getRequest().getRequestURI();
        logger.info("Bean validation error on path: {}", pathUri);
        logger.debug("Errors: ", errorMessages);
        return ResponseEntity.badRequest().body(errorMessages);
    }
}
