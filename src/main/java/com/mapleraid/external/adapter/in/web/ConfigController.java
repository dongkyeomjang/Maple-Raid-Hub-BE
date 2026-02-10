package com.mapleraid.external.adapter.in.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mapleraid.core.dto.ResponseDto;
import com.mapleraid.external.adapter.in.web.dto.BossConfigDto;
import com.mapleraid.external.adapter.in.web.dto.WorldGroupConfigDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/config")
public class ConfigController {

    private final ObjectMapper objectMapper;

    @Value("classpath:config/world-groups.json")
    private Resource worldGroupsResource;

    @Value("classpath:config/bosses.json")
    private Resource bossesResource;

    @Value("classpath:config/boss-bundles.json")
    private Resource bossBundlesResource;

    public ConfigController(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @GetMapping("/world-groups")
    public ResponseDto<List<WorldGroupConfigDto>> getWorldGroups() throws IOException {
        JsonNode root = objectMapper.readTree(worldGroupsResource.getInputStream());
        JsonNode groupsNode = root.get("groups");

        List<WorldGroupConfigDto> groups = new ArrayList<>();
        if (groupsNode != null && groupsNode.isArray()) {
            for (JsonNode groupNode : groupsNode) {
                String id = groupNode.get("id").asText();
                String displayName = groupNode.get("displayName").asText();

                // 월드 이름 목록 추출
                List<String> worldNames = new ArrayList<>();
                JsonNode worldsNode = groupNode.get("worlds");
                if (worldsNode != null && worldsNode.isArray()) {
                    for (JsonNode worldNode : worldsNode) {
                        if (worldNode.has("isActive") && worldNode.get("isActive").asBoolean()) {
                            worldNames.add(worldNode.get("displayName").asText());
                        }
                    }
                }

                groups.add(WorldGroupConfigDto.from(id, displayName, worldNames));
            }
        }

        return ResponseDto.ok(groups);
    }

    @GetMapping("/bosses")
    public ResponseDto<List<BossConfigDto>> getBosses() throws IOException {
        JsonNode root = objectMapper.readTree(bossesResource.getInputStream());
        JsonNode bossesNode = root.get("bosses");

        List<BossConfigDto> bosses = new ArrayList<>();
        if (bossesNode != null && bossesNode.isArray()) {
            for (JsonNode bossNode : bossesNode) {
                // isActive 체크 - 비활성 보스 제외
                if (bossNode.has("isActive") && !bossNode.get("isActive").asBoolean()) {
                    continue;
                }

                String id = bossNode.get("id").asText();
                String bossFamily = bossNode.get("bossFamily").asText();
                String displayName = bossNode.get("displayName").asText();
                String shortName = bossNode.has("shortName") ? bossNode.get("shortName").asText() : displayName;
                String difficulty = bossNode.get("difficulty").asText();

                // partySize 추출
                int maxPartySize = 6;
                JsonNode partySizeNode = bossNode.get("recommendedPartySize");
                if (partySizeNode != null && partySizeNode.has("max")) {
                    maxPartySize = partySizeNode.get("max").asInt();
                }

                // reset 타입 추출
                boolean weeklyReset = bossNode.has("weeklyReset") && bossNode.get("weeklyReset").asBoolean();
                boolean monthlyReset = bossNode.has("monthlyReset") && bossNode.get("monthlyReset").asBoolean();

                // 결정석 가격 추출
                long crystalPrice = bossNode.has("crystalPrice") ? bossNode.get("crystalPrice").asLong() : 0L;

                bosses.add(BossConfigDto.from(id, bossFamily, displayName, shortName, difficulty, maxPartySize, weeklyReset, monthlyReset, crystalPrice));
            }
        }

        return ResponseDto.ok(bosses);
    }

    @GetMapping("/boss-bundles")
    public ResponseDto<JsonNode> getBossBundles() throws IOException {
        JsonNode root = objectMapper.readTree(bossBundlesResource.getInputStream());
        return ResponseDto.ok(root);
    }
}
