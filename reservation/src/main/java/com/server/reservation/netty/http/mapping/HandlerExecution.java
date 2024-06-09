package com.server.reservation.netty.http.mapping;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.reservation.common.config.ObjectMapperWithValidation;
import com.server.reservation.common.exception.CustomException;
import com.server.reservation.common.exception.ErrorCode;
import lombok.Builder;
import org.apache.commons.beanutils.ConvertUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class HandlerExecution {
    private final ObjectMapper objectMapper = ObjectMapperWithValidation.getObjectMapperWithValidation();

    // Non-Threadsafe
    private final DecimalFormat decimalFormat = new DecimalFormat("0.###############");
    private MethodHandler handler;
    private Map<String, String> uriTemplateVariables;
    private String bodyString;

    @Builder
    public HandlerExecution(MethodHandler handler, Map<String, String> uriTemplateVariables, String bodyString) {
        this.handler = handler;
        this.uriTemplateVariables = uriTemplateVariables;
        this.bodyString = bodyString;
    }

    public MethodHandler getHandler() {
        return handler;
    }

    public void setHandler(MethodHandler handler) {
        this.handler = handler;
    }

    // TODO : Exception -> HandlerExecption is Null.
    public Object execute() throws InvocationTargetException, IllegalAccessException, JsonProcessingException {
        return handler.execute(parameterConvert());
    }

    /**
     * 메서드 파라미터 매칭
     * @return {arg1, arg2 ...}
     * @throws JsonProcessingException Json to Object 변환 실패
     */
    private Object[] parameterConvert() {
        Method method = handler.getMethod();
        List<Object> args = new ArrayList<>(method.getParameterCount());

        for (Parameter parameter : method.getParameters()) {
            for (Annotation annotation : parameter.getAnnotations()) {
                if (annotation instanceof PathVariable pathVariable && Objects.nonNull(uriTemplateVariables)) {
                    String pathVar = pathVariable.value();

                    uriTemplateVariables.computeIfPresent(
                            pathVar,
                            (k, v) -> {
                                Object convert = convertOrElseThrow(v, parameter.getType());
                                args.add(convert);
                                return v;
                            }
                    );
                }
                else if (annotation instanceof RequestBody) {
                    Object readValue = readValueOrElseThrow(parameter.getType());
                    args.add(readValue);
                }
            }
        }

        return args.isEmpty() ? null : args.toArray();
    }

    private Object readValueOrElseThrow(Class<?> type) {
        try{
            return objectMapper.readValue(bodyString, type);
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.INVALID_JSON);
        }
    }

    private Object convertOrElseThrow(String origin, Class<?> type) {
        Object convert = ConvertUtils.convert(origin, type);

        String convertToOrigin;
        if (convert instanceof Float || convert instanceof Double){
            // 15 자리까지 지원
            convertToOrigin = decimalFormat.format(convert);
        }
        else{
            convertToOrigin = String.valueOf(convert);
        }

        if (!origin.equals(convertToOrigin)) {
            throw new CustomException(ErrorCode.BAD_REQUEST_PATH_VARIABLE);
        }

        return convert;
    }
}