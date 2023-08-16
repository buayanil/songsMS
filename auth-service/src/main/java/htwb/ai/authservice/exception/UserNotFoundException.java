package htwb.ai.authservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason = "User not found")
public class UserNotFoundException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 2136472234910389713L;
    private final String resourceName;
    private final String fieldName;
    private final Object fieldValue;

    public UserNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s : '%s'", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

}

