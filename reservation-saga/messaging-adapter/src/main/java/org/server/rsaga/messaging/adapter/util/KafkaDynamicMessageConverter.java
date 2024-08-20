package org.server.rsaga.messaging.adapter.util;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

@Slf4j
public class KafkaDynamicMessageConverter {

    private KafkaDynamicMessageConverter() {
        throw new IllegalStateException("KafkaDynamicMessageConverter is a Utility class.");
    }

    /**
     * @param dynamicMessage Deserializer 가 메시지 타입을 알지 못하면 DynamicMessage 를 생성한다. 이를 역직렬화 하기 위한 메서드
     * @return 타입을 찾아 역직렬화한 값 T.
     */
    @SuppressWarnings("unchecked")
    public static  <T> T handleDynamicMessage(DynamicMessage dynamicMessage) {
        try {
            Descriptors.Descriptor descriptor = dynamicMessage.getDescriptorForType();
            Class<?> msgClass = Class.forName(descriptor.getFullName());
            Method parseMethod = msgClass.getMethod("parseFrom", byte[].class);
            return (T) parseMethod.invoke(null, dynamicMessage.toByteArray());
        } catch (Exception e) {
            log.error("Failed to handle DynamicMessage", e);
            return (T) dynamicMessage; // 변환 실패 시 원본 반환
        }
    }
}
