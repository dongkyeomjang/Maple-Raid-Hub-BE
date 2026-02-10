package com.mapleraid.core.utility;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class BossNameResolver {

    private final ObjectMapper objectMapper;

    @Value("classpath:config/bosses.json")
    private Resource bossesResource;

    private Map<String, String> bossNameMap = new HashMap<>();

    @PostConstruct
    public void init() {
        try {
            JsonNode root = objectMapper.readTree(bossesResource.getInputStream());
            JsonNode bossesNode = root.get("bosses");

            if (bossesNode != null && bossesNode.isArray()) {
                for (JsonNode bossNode : bossesNode) {
                    String id = bossNode.get("id").asText();
                    String displayName = bossNode.get("displayName").asText();
                    bossNameMap.put(id, displayName);
                }
            }

            log.info("[BossNameResolver] 보스 이름 {}개 로드 완료", bossNameMap.size());
        } catch (Exception e) {
            log.error("[BossNameResolver] bosses.json 로드 실패: {}", e.getMessage(), e);
        }
    }

    public String resolve(String bossId) {
        return bossNameMap.getOrDefault(bossId, bossId);
    }
}
