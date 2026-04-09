package com.retailstore.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.ItemStreamException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.CannotCreateTransactionException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;


/**
 * Global exception handler for the Retail Store application.
 *
 * <p>This class centralizes exception handling across all REST controllers.
 * It converts thrown exceptions into standardized API error responses.</p>
 *
 * <p>Each handler maps a specific exception to an appropriate HTTP status
 * code and returns a structured {@link ApiError} response containing:</p>
 *
 * <ul>
 *     <li>Timestamp of the error</li>
 *     <li>HTTP status code</li>
 *     <li>Error type</li>
 *     <li>Error message</li>
 *     <li>Request path where the error occurred</li>
 * </ul>
 *
 * <p>This approach ensures consistent error handling and improves API
 * usability for client applications.</p>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles exceptions thrown when a requested resource cannot be found.
     *
     * @param exception      the thrown {@link ResourceNotFoundException}
     * @param request the HTTP request that caused the exception
     * @return {@link ResponseEntity} containing {@link ApiError} with HTTP status 404
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException exception,
                                                   HttpServletRequest request){

        return buildErrorResponse(exception.getMessage(), HttpStatus.NOT_FOUND, request);
    }

    /**
     * Handles business logic failures due to insufficient resources
     * (e.g., insufficient inventory or balance).
     *
     * @param exception      the thrown {@link ResourceInsufficientException}
     * @param request the HTTP request that caused the exception
     * @return {@link ResponseEntity} containing {@link ApiError} with HTTP status 409
     */
    @ExceptionHandler(ResourceInsufficientException.class)
    public ResponseEntity<ApiError> handleInsufficient(ResourceInsufficientException exception,
                                                   HttpServletRequest request){

        return buildErrorResponse(exception.getMessage(), HttpStatus.CONFLICT, request);
    }

    /**
     * Handles conflicts such as attempting to create a resource
     * that already exists or deleting a resource that is in use.
     *
     * @param exception the thrown {@link ResourceConflictException}
     * @param request   the HTTP request that caused the exception
     * @return {@link ResponseEntity} containing {@link ApiError} with HTTP status 409
     */
    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<ApiError> handleConflict(ResourceConflictException exception,
                                                   HttpServletRequest request) {

        return buildErrorResponse(exception.getMessage(), HttpStatus.CONFLICT, request);
    }

    /**
     * Handles malformed or unreadable HTTP request bodies,
     * such as invalid JSON or enum deserialization errors.
     *
     * @param exception      the thrown {@link HttpMessageNotReadableException}
     * @param request the HTTP request that caused the exception
     * @return {@link ResponseEntity} containing {@link ApiError} with HTTP status 400
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleHttpMessageNotReadable(HttpMessageNotReadableException exception,
                                                       HttpServletRequest request){

        String message = "Invalid request body.";

        Throwable cause = exception.getMostSpecificCause();

        if (cause instanceof IllegalArgumentException){
            message = cause.getMessage();

        } else if (cause instanceof UnrecognizedPropertyException ex) {
            message = "Invalid request body. Unkown field: " + ex.getPropertyName();

        } else if (cause instanceof InvalidFormatException ex) {

            Throwable rootCause = ex.getCause();
            if (rootCause instanceof IllegalArgumentException){
                message = rootCause.getMessage();

            }else if (ex.getTargetType().isEnum()){
                message = "Invalid value '" + ex.getValue() + "' for field";
            }
        }

        return buildErrorResponse(message, HttpStatus.BAD_REQUEST, request);
    }

    /**
     * Handles validation errors triggered by {@code @Valid} annotations.
     *
     * @param exception      the thrown {@link MethodArgumentNotValidException}
     * @param request the HTTP request that caused the exception
     * @return {@link ResponseEntity} containing {@link ApiError} with HTTP status 400
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValid(MethodArgumentNotValidException exception,
                                                                 HttpServletRequest request){

        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return buildErrorResponse(message, HttpStatus.BAD_REQUEST, request);
    }

    /**
     * Handles invalid method arguments passed within the application.
     *
     * @param exception      the thrown {@link IllegalArgumentException}
     * @param request the HTTP request that caused the exception
     * @return {@link ResponseEntity} containing {@link ApiError} with HTTP status 400
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArg(IllegalArgumentException exception,
                                                   HttpServletRequest request){

        return buildErrorResponse(exception.getMessage(), HttpStatus.BAD_REQUEST, request);
    }

    /**
     * Handles illegal application states encountered during processing.
     *
     * @param exception      the thrown {@link IllegalStateException}
     * @param request the HTTP request that caused the exception
     * @return {@link ResponseEntity} containing {@link ApiError} with HTTP status 400
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> handleIllegalState(IllegalStateException exception,
                                                     HttpServletRequest request){

        return buildErrorResponse(exception.getMessage(), HttpStatus.BAD_REQUEST, request);
    }

    /**
     * Handles authorization failures where the user lacks required permissions.
     *
     * @param request the HTTP request that caused the exception
     * @return {@link ResponseEntity} containing {@link ApiError} with HTTP status 403
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(HttpServletRequest request){

        return buildErrorResponse("Access denied", HttpStatus.FORBIDDEN, request);
    }

    /**
     * Handles scenarios where a user account is locked.
     *
     * @param exception      the thrown {@link AccountLockedException}
     * @param request the HTTP request that caused the exception
     * @return {@link ResponseEntity} containing {@link ApiError} with HTTP status 403
     */
    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<ApiError> handleAccountLocked(AccountLockedException exception,
                                                       HttpServletRequest request){

        return buildErrorResponse(exception.getMessage(), HttpStatus.LOCKED, request);
    }

    /**
     * Handles unauthorize such as attempting to unauthorized resource
     *
     * @param exception      the thrown {@link UnauthorizedException}
     * @param request the HTTP request that caused the exception
     * @return {@link ResponseEntity} containing {@link ApiError} with HTTP status 409
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiError> handleConflict(UnauthorizedException exception,
                                                   HttpServletRequest request){

        return buildErrorResponse(exception.getMessage(), HttpStatus.UNAUTHORIZED, request);
    }

    /**
     * Handles HTTP requests with unsupported methods (e.g., PATCH on a GET-only endpoint)
     *
     * @param exception the thrown {@link HttpRequestMethodNotSupportedException}
     * @param request   the HTTP request that caused the exception
     * @return {@link ResponseEntity} containing {@link ApiError} with HTTP status 405
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> handleMethodNotAllowed(HttpRequestMethodNotSupportedException exception,
                                                           HttpServletRequest request) {

        String method = exception.getMethod();
        String message = "Request method '" + method + "' is not supported for this endpoint";

        return buildErrorResponse(message, HttpStatus.METHOD_NOT_ALLOWED, request);
    }

    /**
     * Handles invalid login attempts (bad username/password).
     *
     * @param exception the thrown {@link BadCredentialsException}
     * @param request   the HTTP request causing the exception
     * @return ResponseEntity containing ApiError with HTTP status 401
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials(BadCredentialsException exception,
                                                         HttpServletRequest request) {

        String message = "Invalid username or password";

        return buildErrorResponse(message, HttpStatus.UNAUTHORIZED, request);
    }



    // ====================<< START OF SPRING BATCH EXCEPTIONS >>====================

    @ExceptionHandler(JobExecutionAlreadyRunningException.class)
    public ResponseEntity<String> jobRunning(JobExecutionAlreadyRunningException ex) {
        log.error("Job already running", ex);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(JobInstanceAlreadyCompleteException.class)
    public ResponseEntity<String> jobComplete(JobInstanceAlreadyCompleteException ex) {
        log.error("Job already completed", ex);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(JobRestartException.class)
    public ResponseEntity<String> jobRestart(JobRestartException ex) {
        log.error("Job restart failed", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(JobParametersInvalidException.class)
    public ResponseEntity<String> invalidParams(JobParametersInvalidException ex) {
        log.error("Invalid job parameters", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(FlatFileParseException.class)
    public ResponseEntity<String> fileParse(FlatFileParseException ex) {
        log.error("File parsing error", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error parsing file at line: " + ex.getLineNumber());
    }

    @ExceptionHandler({UnexpectedInputException.class, ParseException.class, ItemStreamException.class})
    public ResponseEntity<String> batchInput(Exception ex) {
        log.error("Batch input error", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    // ------------------------- DB & JPA ------------------------------

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> dataIntegrity(DataIntegrityViolationException ex) {
        log.error("DB integrity violation", ex);
        return ResponseEntity.status(HttpStatus.CONFLICT).body("Data integrity error");
    }

    @ExceptionHandler(CannotCreateTransactionException.class)
    public ResponseEntity<String> dbDown(CannotCreateTransactionException ex) {
        log.error("Cannot create DB transaction", ex);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Database unreachable");
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> entityNotFound(EntityNotFoundException ex) {
        log.error("Entity not found", ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<String> constraint(ConstraintViolationException ex) {
        log.error("Validation error", ex);
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    // ====================<< END OF SPRING BATCH EXCEPTIONS >>====================

    /**
     * Handles all uncaught exceptions as a fallback mechanism.
     *
     * <p>This prevents internal errors from leaking sensitive
     * implementation details to the client.</p>
     *
     * @param exception      the thrown {@link Exception}
     * @param request the HTTP request that caused the exception
     * @return {@link ResponseEntity} containing {@link ApiError} with HTTP status 500
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleOther(Exception exception,
                                              HttpServletRequest request){

        return buildErrorResponse("Something went wrong: " + exception.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    /**
     * Helper method to build a standardized {@link ApiError} response.
     *
     * @param message the exception that occurred
     * @param status    the HTTP status associated with the exception
     * @param request   the HTTP request that triggered the exception
     * @return {@link ResponseEntity} containing the structured error response
     */
    private ResponseEntity<ApiError> buildErrorResponse(String message,
                                                        HttpStatus status,
                                                        HttpServletRequest request){
        ApiError error = ApiError.builder()
                .timeStamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(error, status);
    }
}
