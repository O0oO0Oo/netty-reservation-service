package com.server.reservation.netty.http.mapping;

import org.springframework.util.CollectionUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Decode 클래스
 */
public class NettyUrlPathHelper {
    public Map<String, String> decodePathVariables(Map<String, String> vars) {
        Map<String, String> decodedVars = CollectionUtils.newLinkedHashMap(vars.size());
        vars.forEach((key, values) -> decodedVars.put(key, decode(values)));
        return decodedVars;
    }

    private String decode(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("Failed to decode value: " + value, e);
        }
    }
}
