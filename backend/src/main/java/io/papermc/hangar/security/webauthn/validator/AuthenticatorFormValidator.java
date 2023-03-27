package io.papermc.hangar.security.webauthn.validator;

import com.webauthn4j.springframework.security.WebAuthnRegistrationRequestValidator;
import com.webauthn4j.validator.exception.ValidationException;
import io.papermc.hangar.security.webauthn.model.AuthenticatorForm;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.validation.Validator;

@Component
public class AuthenticatorFormValidator implements Validator {

    private static final String NOT_NULL = "not.null";

    private final WebAuthnRegistrationRequestValidator webAuthnRegistrationRequestValidator;
    private final HttpServletRequest request;

    public AuthenticatorFormValidator(final WebAuthnRegistrationRequestValidator webAuthnRegistrationRequestValidator, final HttpServletRequest request) {
        this.webAuthnRegistrationRequestValidator = webAuthnRegistrationRequestValidator;
        this.request = request;
    }

    @Override
    public void validate(final @NotNull Object target, final @NotNull Errors errors) {
        final AuthenticatorForm form = (AuthenticatorForm) target;
        if (form.id() == null) {
            if (form.credentialId() == null) {
                errors.rejectValue("credentialId", NOT_NULL);
            }
            if (form.attestationObject() == null) {
                errors.rejectValue("attestationObject", NOT_NULL);
            }
            if (form.clientData() == null) {
                errors.rejectValue("clientData", NOT_NULL);
            }
            if (form.clientExtensionsJSON() == null) {
                errors.rejectValue("clientExtensionsJSON", NOT_NULL);
            }
            if (errors.hasErrors()) {
                return;
            }
            try {
                this.webAuthnRegistrationRequestValidator.validate(
                    this.request,
                    form.clientData().clientDataBase64(),
                    form.attestationObject().attestationObjectBase64(),
                    form.transports(),
                    form.clientExtensionsJSON());
            } catch (final ValidationException exception) {
                errors.reject("e.AuthenticatorFormValidator.invalidAuthenticator", "AuthenticatorEntity is invalid.");
            }
        }
    }

    @Override
    public boolean supports(final @NotNull Class<?> clazz) {
        return AuthenticatorForm.class.isAssignableFrom(clazz);
    }
}
