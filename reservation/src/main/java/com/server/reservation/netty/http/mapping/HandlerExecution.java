package com.server.reservation.netty.http.mapping;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import org.apache.commons.beanutils.ConvertUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.text.DecimalFormat;
import java.util.*;

public class HandlerExecution {
    private final ObjectMapper objectMapper = new ObjectMapper();

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
    private Object[] parameterConvert() throws JsonProcessingException {
        Method method = handler.getMethod();
        List<Object> args = new ArrayList<>(method.getParameterCount());

        for (Parameter parameter : method.getParameters()) {
            for (Annotation annotation : parameter.getAnnotations()) {
                if (annotation instanceof PathVariable pathVariable && Objects.nonNull(uriTemplateVariables)) {
                    String pathVar = pathVariable.value();

                    uriTemplateVariables.computeIfPresent(
                            pathVar,
                            (k, v) -> {
                                Object convert = ConvertUtils.convert(v, parameter.getType());

                                isValidConvertOrElseThrow(v, convert);

                                args.add(convert);
                                return v;
                            }
                    );
                }
                else if (annotation instanceof RequestBody) {
                    // TODO : Exception -> JsonProcessingException
                    Object readValue = objectMapper.readValue(bodyString, parameter.getType());
                    args.add(readValue);
                }
            }
        }

        return args.isEmpty() ? null : args.toArray();
    }

    private void isValidConvertOrElseThrow(String origin, Object convert) {
        String convertToOrigin;
        if (convert instanceof Float || convert instanceof Double){
            // 15 자리까지 지원
            convertToOrigin = decimalFormat.format(convert);
        }
        else{
            convertToOrigin = String.valueOf(convert);
        }

        if (!origin.equals(convertToOrigin)) {
            // TODO : Exception -> PathParameter IllegalArgument
            throw new IllegalArgumentException();
        }
    }
}