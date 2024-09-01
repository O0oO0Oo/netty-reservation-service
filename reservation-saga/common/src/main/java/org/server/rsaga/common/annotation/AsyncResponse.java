package org.server.rsaga.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@link org.server.rsaga.common.dto.FullHttpAsyncResponse} 로 응답을 감싸서 주어야 한다.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AsyncResponse {
}