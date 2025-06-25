package io.papermc.hangar.exceptions.handlers;

import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.exceptions.HangarResponseException;
import io.papermc.hangar.exceptions.MultiHangarApiException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.util.UriComponentsBuilder;

@ControllerAdvice
public class HangarExceptionHandler extends ResponseEntityExceptionHandler {

    private final HangarConfig config;

    @Autowired
    public HangarExceptionHandler(final HangarConfig config) {
        this.config = config;
    }

    @ExceptionHandler(HangarApiException.class)
    public ResponseEntity<HangarApiException> handleException(final HangarApiException exception, final HandlerMethod method, final HttpServletRequest request) {
        return this.<HangarApiException>checkErrorRedirect(method, exception.getStatusCode().value(), exception.getReason(), request)
            .orElseGet(() -> new ResponseEntity<>(exception, exception.getHeaders(), exception.getStatusCode().value()));
    }

    @ExceptionHandler(MultiHangarApiException.class)
    public ResponseEntity<MultiHangarApiException> handleException(final MultiHangarApiException exception, final HandlerMethod method, final HttpServletRequest request) {
        return this.<MultiHangarApiException>checkErrorRedirect(method, exception.getStatusCode().value(), exception.getReason(), request)
            .orElseGet(() -> new ResponseEntity<>(exception, exception.getHeaders(), exception.getStatusCode().value()));
    }

    @ExceptionHandler(HangarResponseException.class)
    public ResponseEntity<HangarResponseException> handleException(final HangarResponseException exception, final HandlerMethod method, final HttpServletRequest request) {
        return this.<HangarResponseException>checkErrorRedirect(method, exception.getStatus().value(), exception.getMessage(), request)
            .orElseGet(() -> ResponseEntity.status(exception.getStatus()).headers(exception.getHeaders()).body(exception));
    }

    private <T> Optional<ResponseEntity<T>> checkErrorRedirect(final HandlerMethod method, final int status, final String message, final HttpServletRequest request) {
        final ErrorRedirect errorRedirect = method.getMethodAnnotation(ErrorRedirect.class);
        if (errorRedirect != null) {
            String returnUrl = (String) request.getAttribute(ErrorRedirect.RETURN_URL);
            if (returnUrl == null) {
                returnUrl = errorRedirect.returnUrl();
            }

            final HttpHeaders headers = new HttpHeaders();
            final String url = UriComponentsBuilder.fromPath("/error")
                .queryParam("returnUrl", returnUrl)
                .queryParam("errorCode", status)
                .queryParam("errorMessage", message)
                .build().toUriString();
            headers.add("Location", url);
            return Optional.of(new ResponseEntity<>(headers, HttpStatus.FOUND));
        }
        return Optional.empty();
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(final @NotNull Exception ex, final Object body, final @NotNull HttpHeaders headers, final @NotNull HttpStatusCode status, final @NotNull WebRequest request) {
        if (this.config.dev()) {
            return new ResponseEntity<>(new HangarApiException(ex.getMessage()), status);
        } else {
            return super.handleExceptionInternal(ex, body, headers, status, request);
        }
    }

    @Override
    protected @NotNull ResponseEntity<Object> handleMethodArgumentNotValid(final @NotNull MethodArgumentNotValidException ex, final @NotNull HttpHeaders headers, final @NotNull HttpStatusCode status, final @NotNull WebRequest request) {
        return new ResponseEntity<>(ex, headers, status);
    }

    @Override
    protected ResponseEntity<Object> handleMaxUploadSizeExceededException(final @NotNull MaxUploadSizeExceededException ex, final @NotNull HttpHeaders headers, final @NotNull HttpStatusCode status, final @NotNull WebRequest request) {
        final HangarApiException apiException = new HangarApiException(HttpStatus.PAYLOAD_TOO_LARGE, "File too large - files have to be less than " + this.config.projects().maxTotalFilesSizeMB() + "MB total");
        return new ResponseEntity<>(apiException, HttpStatus.PAYLOAD_TOO_LARGE);
    }
}
