package com.server.reservation.netty.http.mapping;

import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

class HandlerExecutionTest {

    private NettySimpleUrlHandlerMapping<HttpMethodHandler> handlerMapping;
    private AnnotationConfigApplicationContext context;

    @BeforeEach
    void beforeEach() {
        context = new AnnotationConfigApplicationContext();
        context.register(TestController.class);
        context.refresh();

        handlerMapping = new NettySimpleUrlHandlerMapping<>(new HttpMethodHandlerFactory());
        handlerMapping.registerHandlers(context, new Class[]{RestController.class});
    }

    @Test
    @DisplayName("Handler mapping test - succeed")
    public void givenValidFullHttpRequest_whenGetHandler_thenReturnValidValue() throws Exception {
        // Given
        FullHttpRequest request1 = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/test");
        FullHttpRequest request2 = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/test/get");
        String intVal = "5";
        FullHttpRequest request3 = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/test/get/" + intVal + "/int");
        String doubleVal = "1.2345";
        FullHttpRequest request4 = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/test/get/" + doubleVal + "/double");
        String booleanVal = "true";
        FullHttpRequest request5 = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/test/get/" + booleanVal + "/boolean");

        // When
        Object execute1 = handlerMapping.getHandler(request1).execute();
        Object execute2 = handlerMapping.getHandler(request2).execute();
        Object execute3 = handlerMapping.getHandler(request3).execute();
        Object execute4 = handlerMapping.getHandler(request4).execute();
        Object execute5 = handlerMapping.getHandler(request5).execute();

        // Then
        Assertions.assertEquals(execute1, "req1");
        Assertions.assertEquals(execute2, "req2");
        Assertions.assertEquals(execute3, "req3 " + intVal);
        Assertions.assertEquals(execute4, "req4 " + doubleVal);
        Assertions.assertEquals(execute5, "req5 " + booleanVal);
    }

    @Test
    @DisplayName("Handler mapping test - failed - invalid url")
    public void givenInvalidFullHttpRequest_whenGetHandler_thenFailed() throws Exception {
        // Given
        FullHttpRequest request1 = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/invalid-url");
        String intVal = "Not Integer";
        FullHttpRequest request2 = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/test/get/" + intVal + "/int");

        // When
        Object handler1 = handlerMapping.getHandler(request1);
        HandlerExecution handler2 = handlerMapping.getHandler(request2);

        // Then
        Assertions.assertNull(handler1);
        Assertions.assertThrows(IllegalArgumentException.class, () -> handler2.execute());
    }

    @Test
    @DisplayName("print annotation, value name")
    public void givenTestControllerClazz_whenInvokeAllMethod_thenPrintString(){
        Class<TestController> testControllerClass = TestController.class;

        for (Method method : testControllerClass.getDeclaredMethods()) {
            System.out.println(method.getName());

            Parameter[] parameters = method.getParameters();
            for (Parameter parameter : parameters) {
                for(Annotation annotation : parameter.getAnnotations()){
                    System.out.print(annotation + " ");
                }
                System.out.print(parameter.getName() + " ");
            }
            System.out.println();
        }
    }

    @RestController
    @RequestMapping("/test")
    static class TestController {

        @GetMapping
        public String findEmptyPathMethod() {
            return "req1";
        }

        @GetMapping("/get")
        public String findGetMethod() {
            return "req2";
        }

        @GetMapping("/get/{val}/int")
        public String findMethodInt(@PathVariable("val") int val) {
            return "req3 " + val;
        }

        @GetMapping("/get/{val}/double")
        public String findMethodDouble(@PathVariable("val") double val) {
            return "req4 " + val;
        }

        @GetMapping("/get/{val}/boolean/")
        public String findMethodBoolean(@PathVariable("val") boolean val) {
            return "req5 " + val;
        }
    }

}