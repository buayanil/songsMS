package htwb.ai.authservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason = "Password wrong")
public class PasswordWrongException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 2136472234910389713L;
    private final String resourceName;
    private final String fieldName;
    private final Object fieldValue;

    public PasswordWrongException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s has a wrong %s for '%s'", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public String getResourceName() {
        return resourceName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Object getFieldValue() {
        return fieldValue;
    }
}