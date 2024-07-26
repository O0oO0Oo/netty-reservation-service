package org.server.rsaga.common.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.BeanDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBase;
import jakarta.validation.*;

import java.io.IOException;
import java.util.Set;

public class BeanValidationDeserializer extends BeanDeserializer {
    private static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private final Validator validator = factory.getValidator();

    public BeanValidationDeserializer(BeanDeserializerBase src) {
        super(src);
    }

    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        Object instance = super.deserialize(p, ctxt);
        validate(instance);
        return instance;
    }

    private void validate(Object instance) {
        Set<ConstraintViolation<Object>> violations = validator.validate(instance);
        if (!violations.isEmpty()) {
            StringBuilder msg = new StringBuilder();
            msg.append("JSON object is not valid. Reasons (").append(violations.size()).append(")\n");
            for (ConstraintViolation<Object> violation : violations) {
                msg.append(violation.getMessage()).append("\n");
            }
            throw new ConstraintViolationException(msg.toString(), violations);
        }
    }
}
