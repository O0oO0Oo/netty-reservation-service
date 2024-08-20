package org.server.rsaga.saga.util;

import io.hypersistence.tsid.TSID;
import org.server.rsaga.saga.api.SagaMessage;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class SagaMessageUtil {

    private SagaMessageUtil() {
        throw new IllegalStateException("SagaMessageConverter is a utility class.");
    }

    public static byte[] tsidToBytes(TSID value) {
        return value.toBytes();
    }

    public static TSID extractTsid(byte[] bytes) {
        return TSID.from(bytes);
    }

    public static byte[] intToByteArray(int value) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(4);

        byteBuffer.putInt(value);
        return byteBuffer.array();
    }

    public static int extractInt(byte[] bytes) {
        checkBytesIsNull(bytes);

        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        return byteBuffer.getInt();
    }

    public static String extractString(byte[] bytes) {
        checkBytesIsNull(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private static void checkBytesIsNull(byte[] bytes) {
        if (bytes == null) {
            throw new IllegalArgumentException("The byte must not be null.");
        }
    }
}
