package io.github.cnadjim.dynamic.search.exception;

public class ResourceNotFoundException extends RuntimeException {

    private static final String MESSAGE = "%s not found with %s : %s";

    public ResourceNotFoundException(String ressourceName, String fieldName, Object fieldValue) {
        super(String.format(MESSAGE, ressourceName, fieldName, fieldValue));
    }

    public ResourceNotFoundException(String ressourceName, Object idValue) {
        super(String.format(MESSAGE, ressourceName, "id", idValue));
    }

    public ResourceNotFoundException(Object fieldValue) {
        super(String.format(MESSAGE, "Resource", "id", fieldValue));
    }

}
