package net.github.score.entrypoints.web.handler;


import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
@Slf4j
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    protected ResponseEntity<Object> exceptionHandler(Exception ex, WebRequest request) {
        GenericError apiError = new GenericError(INTERNAL_SERVER_ERROR);
        apiError.setMessage("Unexpected exception occurred.");
        apiError.setDebugMessage(ex.getMessage());
        logger.error("Unexpected exception occurred.", ex);
        return buildResponseEntity(apiError);
    }

    @ExceptionHandler(value = GenericException.class)
    @ResponseBody
    protected ResponseEntity<Object> exceptionHandler(GenericException ex, WebRequest request) {
        GenericError apiError = new GenericError(INTERNAL_SERVER_ERROR);
        apiError.setMessage("Something is broken please check debug message.");
        apiError.setDebugMessage(ex.getMessage());
        logger.error("Something is broken please check debug message.", ex);
        return buildResponseEntity(apiError);
    }

    @ExceptionHandler(value = RuntimeException.class)
    protected ResponseEntity<Object> exceptionHandler(RuntimeException runtimeException, WebRequest request) {
        GenericError apiError = new GenericError(BAD_REQUEST);
        apiError.setMessage("Unexpected runtime exception occurred.");
        apiError.setDebugMessage(runtimeException.getMessage());
        logger.error("Unexpected runtime exception occurred.", runtimeException);
        return buildResponseEntity(apiError);
    }

    private ResponseEntity<Object> buildResponseEntity(GenericError apiError) {
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }
}
