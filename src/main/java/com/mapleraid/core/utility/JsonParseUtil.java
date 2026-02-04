package com.mapleraid.core.utility;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class JsonParseUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private JsonParseUtil() {
    }

    /**
     * Object → JSON 문자열(String)
     */
    public static String convertFromObjectToJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("[JsonParseUtil] JSON 직렬화 오류: object = {}", object, e);
            throw new RuntimeException("[JsonParseUtil] JSON 직렬화 오류", e);
        }
    }

    /**
     * JSON 문자열(String) → Class<T>
     */
    public static <T> T convertFromMessageToClass(String message, Class<T> clazz) {
        try {
            return objectMapper.readValue(message, clazz);
        } catch (JsonProcessingException e) {
            log.error("[JsonParseUtil] JSON 역직렬화 오류: message = {}, clazz = {}", message, clazz, e);
            throw new RuntimeException("[JsonParseUtil] JSON 역직렬화 오류", e);
        }
    }

    /**
     * Object → Class<T>
     * (LinkedHashMap 등 일반 Object를 DTO로 변환할 때)
     */
    public static <T> T convertFromObjectToClass(Object payload, Class<T> clazz) {
        try {
            return objectMapper.convertValue(payload, clazz);
        } catch (IllegalArgumentException e) {
            log.error("[JsonParseUtil] 객체 변환 오류: payload = {}, clazz = {}", payload, clazz, e);
            throw new RuntimeException("[JsonParseUtil] 객체 변환 오류", e);
        }
    }
}
