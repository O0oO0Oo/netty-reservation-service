package com.server.reservation.common.config;


import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBase;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class ObjectMapperWithValidation {
    private static final ObjectMapper objectMapper = createObjectMapperWithValidation();

    public static ObjectMapper getObjectMapperWithValidation() {
        return objectMapper;
    }

    private static ObjectMapper createObjectMapperWithValidation() {
        SimpleModule validationModule = new SimpleModule();
        validationModule.setDeserializerModifier(new BeanDeserializerModifier() {
            @Override
            public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config, BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
                if (deserializer instanceof BeanDeserializerBase) {
                    return new BeanValidationDeserializer((BeanDeserializerBase) deserializer);
                }
                return deserializer;
            }
        });

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(validationModule);
        return mapper;
    }
}