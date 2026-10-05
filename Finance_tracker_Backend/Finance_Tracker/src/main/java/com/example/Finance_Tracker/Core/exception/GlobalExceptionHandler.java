package com.example.Finance_Tracker.Core.exception;

import com.example.Finance_Tracker.Budget.exception.BudgetNotFoundException;
import com.example.Finance_Tracker.Budget.exception.UnauthorizedBudgetAccessException;
import com.example.Finance_Tracker.Transaction.exception.NotFoundException;
import com.example.Finance_Tracker.User.exception.EmailAlreadyExistsException;
import com.example.Finance_Tracker.User.exception.InvalidCredentialsException;
import com.example.Finance_Tracker.User.exception.UserNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.ParameterErrors;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Single place that turns exceptions into HTTP responses.
 * <ul>
 *   <li>Domain exceptions map to 401/403/404/409/400 with their (developer-written) message.</li>
 *   <li>Standard Spring MVC errors (bad JSON, missing params, type mismatch, 405, unknown path...)
 *       are handled by {@link ResponseEntityExceptionHandler} and reshaped into {@link ApiError}.</li>
 *   <li>Anything unexpected becomes a 500 with a generic message; details go to the log only.</li>
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ---------- Domain exceptions ----------

    @ExceptionHandler({
            ResourceNotFoundException.class,
            NotFoundException.class,
            BudgetNotFoundException.class,
            UserNotFoundException.class
    })
    public ResponseEntity<ApiError> handleNotFound(RuntimeException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler({AccessDeniedException.class, UnauthorizedBudgetAccessException.class})
    public ResponseEntity<ApiError> handleForbidden(RuntimeException ex, HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, "You do not have permission to access this resource", request);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiError> handleInvalidCredentials(InvalidCredentialsException ex, HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleConflict(EmailAlreadyExistsException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    /** Invalid input detected in service code, or an unknown sort property (e.g. ?sort=bogus,asc). */
    @ExceptionHandler({IllegalArgumentException.class, PropertyReferenceException.class})
    public ResponseEntity<ApiError> handleBadRequest(RuntimeException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    /** Last resort: never leak internal details to the client. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception on {} {}", request.getMethod(), request.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request);
    }

    // ---------- Spring MVC exceptions (reshaped into ApiError) ----------

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        return validationFailed(fieldErrors, headers, request);
    }

    /**
     * Method validation (Spring 6.1+), e.g. {@code @RequestBody List<@Valid X>} or a constraint on a
     * {@code @RequestParam}. Without this override the client only gets the generic "Validation failure".
     * Keys name the failing value: {@code settings[1].key} for a list element field, {@code page} for a parameter.
     */
    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(HandlerMethodValidationException ex,
                                                                            HttpHeaders headers,
                                                                            HttpStatusCode status,
                                                                            WebRequest request) {
        if (ex.isForReturnValue()) {
            // A response that violates its own constraints is a server bug (500); do not expose the details.
            return super.handleHandlerMethodValidationException(ex, headers, status, request);
        }
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (ParameterValidationResult result : ex.getParameterValidationResults()) {
            String name = parameterPath(result);
            if (result instanceof ParameterErrors errors && errors.hasFieldErrors()) {
                for (FieldError error : errors.getFieldErrors()) {
                    fieldErrors.putIfAbsent(name + "." + error.getField(), error.getDefaultMessage());
                }
            } else {
                result.getResolvableErrors().forEach(error -> fieldErrors.putIfAbsent(name, error.getDefaultMessage()));
            }
        }
        return validationFailed(fieldErrors, headers, request);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex,
                                                             @Nullable Object body,
                                                             HttpHeaders headers,
                                                             HttpStatusCode statusCode,
                                                             WebRequest request) {
        if (request instanceof ServletWebRequest servletRequest
                && servletRequest.getResponse() != null
                && servletRequest.getResponse().isCommitted()) {
            return null;
        }
        // Spring's ProblemDetail "detail" texts are written for clients (e.g. "Required parameter 'month' is not present.").
        // Some exceptions (e.g. 405) arrive with a null body and carry their ProblemDetail on the exception itself.
        ProblemDetail problem = (body instanceof ProblemDetail pd) ? pd
                : (ex instanceof ErrorResponse errorResponse) ? errorResponse.getBody() : null;
        String message = (problem != null && problem.getDetail() != null)
                ? problem.getDetail()
                : "Request could not be processed";
        ApiError apiError = ApiError.of(statusCode, message, path(request), null);
        return new ResponseEntity<>(apiError, headers, statusCode);
    }

    // ---------- helpers ----------

    /** 400 with one "field: message" pair per failing field, joined into {@code message} as well. */
    private static ResponseEntity<Object> validationFailed(Map<String, String> fieldErrors,
                                                           HttpHeaders headers,
                                                           WebRequest request) {
        String message = fieldErrors.entrySet().stream()
                .map(e -> e.getKey() + ": " + e.getValue())
                .collect(Collectors.joining("; "));
        if (message.isEmpty()) {
            message = "Validation failed";
        }
        ApiError body = ApiError.of(HttpStatus.BAD_REQUEST, message, path(request), fieldErrors);
        return new ResponseEntity<>(body, headers, HttpStatus.BAD_REQUEST);
    }

    /** Parameter name plus the element position for container elements, e.g. {@code settings[1]}. */
    private static String parameterPath(ParameterValidationResult result) {
        String name = result.getMethodParameter().getParameterName();
        StringBuilder path = new StringBuilder(name != null ? name : "parameter");
        if (result.getContainerIndex() != null) {
            path.append('[').append(result.getContainerIndex()).append(']');
        } else if (result.getContainerKey() != null) {
            path.append('[').append(result.getContainerKey()).append(']');
        }
        return path.toString();
    }

    private static ResponseEntity<ApiError> build(HttpStatus status, String message, HttpServletRequest request) {
        return ResponseEntity.status(status).body(ApiError.of(status, message, request.getRequestURI(), null));
    }

    private static String path(WebRequest request) {
        if (request instanceof ServletWebRequest servletRequest) {
            return servletRequest.getRequest().getRequestURI();
        }
        return null;
    }
}
