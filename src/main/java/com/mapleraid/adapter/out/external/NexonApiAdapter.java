package com.mapleraid.adapter.out.external;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mapleraid.application.port.out.NexonApiPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class NexonApiAdapter implements NexonApiPort {

    private static final Logger log = LoggerFactory.getLogger(NexonApiAdapter.class);

    private static final String BASE_URL = "https://open.api.nexon.com/maplestory/v1";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String apiKey;

    public NexonApiAdapter(
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            @Value("${nexon.api.key}") String apiKey) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
    }

    @Override
    public Optional<String> resolveOcid(String characterName, String worldName) {
        try {
            URI uri = UriComponentsBuilder.fromHttpUrl(BASE_URL + "/id")
                    .queryParam("character_name", characterName)
                    .encode()
                    .build()
                    .toUri();

            log.debug("Requesting OCID for character: {} -> {}", characterName, uri);

            HttpEntity<Void> request = createRequest();
            ResponseEntity<String> response = restTemplate.exchange(
                    uri, HttpMethod.GET, request, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                String ocid = root.path("ocid").asText(null);
                return Optional.ofNullable(ocid);
            }
        } catch (RestClientException e) {
            log.warn("Failed to resolve OCID for character: {} in world: {} - {}", characterName, worldName, e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error resolving OCID: {}", e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<CharacterBasicInfo> getCharacterBasic(String ocid) {
        try {
            URI uri = UriComponentsBuilder.fromHttpUrl(BASE_URL + "/character/basic")
                    .queryParam("ocid", ocid)
                    .encode()
                    .build()
                    .toUri();

            HttpEntity<Void> request = createRequest();
            ResponseEntity<String> response = restTemplate.exchange(
                    uri, HttpMethod.GET, request, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());

                return Optional.of(new CharacterBasicInfo(
                        root.path("character_name").asText(),
                        root.path("world_name").asText(),
                        root.path("character_class").asText(),
                        root.path("character_level").asInt(),
                        root.path("character_image").asText(null)
                ));
            }
        } catch (RestClientException e) {
            log.warn("Failed to get character basic info for OCID: {} - {}", ocid, e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error getting character basic info: {}", e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<EquipmentInfo> getItemEquipment(String ocid) {
        try {
            URI uri = UriComponentsBuilder.fromHttpUrl(BASE_URL + "/character/item-equipment")
                    .queryParam("ocid", ocid)
                    .encode()
                    .build()
                    .toUri();

            HttpEntity<Void> request = createRequest();
            ResponseEntity<String> response = restTemplate.exchange(
                    uri, HttpMethod.GET, request, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                String date = root.path("date").asText(null);
                Map<String, EquipmentSlot> slots = parseEquipmentSlots(root);

                return Optional.of(new EquipmentInfo(date, slots));
            }
        } catch (RestClientException e) {
            log.warn("Failed to get item equipment for OCID: {} - {}", ocid, e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error getting item equipment: {}", e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<SymbolEquipmentInfo> getSymbolEquipment(String ocid) {
        try {
            URI uri = UriComponentsBuilder.fromHttpUrl(BASE_URL + "/character/symbol-equipment")
                    .queryParam("ocid", ocid)
                    .encode()
                    .build()
                    .toUri();

            HttpEntity<Void> request = createRequest();
            ResponseEntity<String> response = restTemplate.exchange(
                    uri, HttpMethod.GET, request, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                List<SymbolInfo> symbols = parseSymbols(root);
                return Optional.of(new SymbolEquipmentInfo(symbols));
            }
        } catch (RestClientException e) {
            log.warn("Failed to get symbol equipment for OCID: {} - {}", ocid, e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error getting symbol equipment: {}", e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<CharacterStatInfo> getCharacterStat(String ocid) {
        try {
            URI uri = UriComponentsBuilder.fromHttpUrl(BASE_URL + "/character/stat")
                    .queryParam("ocid", ocid)
                    .encode()
                    .build()
                    .toUri();

            HttpEntity<Void> request = createRequest();
            ResponseEntity<String> response = restTemplate.exchange(
                    uri, HttpMethod.GET, request, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                long combatPower = parseCombatPower(root);
                return Optional.of(new CharacterStatInfo(combatPower));
            }
        } catch (RestClientException e) {
            log.warn("Failed to get character stat for OCID: {} - {}", ocid, e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error getting character stat: {}", e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<CharacterStatInfo> getCharacterStatByPreset(String ocid, int presetNo) {
        // preset_no 파라미터가 API에서 지원되지 않을 수 있어 기본 조회로 대체
        return getCharacterStat(ocid);
    }

    @Override
    public Optional<CharacterStatInfo> getMaxCombatPowerStat(String ocid) {
        // preset_no 파라미터 문제로 인해 기본 스탯만 조회
        return getCharacterStat(ocid);
    }

    private long parseCombatPower(JsonNode root) {
        JsonNode finalStatArray = root.path("final_stat");
        if (finalStatArray.isArray()) {
            for (JsonNode stat : finalStatArray) {
                String statName = stat.path("stat_name").asText();
                if ("전투력".equals(statName)) {
                    // stat_value는 문자열로 반환됨
                    String statValue = stat.path("stat_value").asText("0");
                    try {
                        return Long.parseLong(statValue);
                    } catch (NumberFormatException e) {
                        log.warn("Failed to parse combat power: {}", statValue);
                        return 0;
                    }
                }
            }
        }
        return 0;
    }

    private List<SymbolInfo> parseSymbols(JsonNode root) {
        List<SymbolInfo> symbols = new ArrayList<>();

        JsonNode symbolArray = root.path("symbol");
        if (symbolArray.isArray()) {
            for (JsonNode symbol : symbolArray) {
                String symbolName = symbol.path("symbol_name").asText();
                String symbolIcon = symbol.path("symbol_icon").asText(null);
                int symbolLevel = symbol.path("symbol_level").asInt(0);
                int growthCount = symbol.path("symbol_growth_count").asInt(0);
                int requireGrowthCount = symbol.path("symbol_require_growth_count").asInt(0);

                if (symbolName != null && !symbolName.isEmpty()) {
                    symbols.add(new SymbolInfo(symbolName, symbolIcon, symbolLevel, growthCount, requireGrowthCount));
                }
            }
        }

        return symbols;
    }

    private Map<String, EquipmentSlot> parseEquipmentSlots(JsonNode root) {
        Map<String, EquipmentSlot> slots = new HashMap<>();

        JsonNode itemEquipment = root.path("item_equipment");
        if (itemEquipment.isArray()) {
            for (JsonNode item : itemEquipment) {
                String slotName = item.path("item_equipment_slot").asText();
                String itemName = item.path("item_name").asText();
                String itemIcon = item.path("item_icon").asText(null);

                if (slotName != null && !slotName.isEmpty()) {
                    // Normalize slot names to match our verification item slots
                    String normalizedSlot = normalizeSlotName(slotName);
                    Map<String, Object> details = parseEquipmentDetails(item, slotName);
                    slots.put(normalizedSlot, new EquipmentSlot(itemName, itemIcon, details));
                }
            }
        }

        return slots;
    }

    private Map<String, Object> parseEquipmentDetails(JsonNode item, String originalSlot) {
        Map<String, Object> details = new HashMap<>();
        details.put("originalSlot", originalSlot);

        // 스타포스
        details.put("starforce", item.path("starforce").asText("0"));

        // 잠재능력
        details.put("potentialOptionGrade", item.path("potential_option_grade").asText(null));
        details.put("potentialOption1", item.path("potential_option_1").asText(null));
        details.put("potentialOption2", item.path("potential_option_2").asText(null));
        details.put("potentialOption3", item.path("potential_option_3").asText(null));

        // 에디셔널 잠재능력
        details.put("additionalPotentialOptionGrade", item.path("additional_potential_option_grade").asText(null));
        details.put("additionalPotentialOption1", item.path("additional_potential_option_1").asText(null));
        details.put("additionalPotentialOption2", item.path("additional_potential_option_2").asText(null));
        details.put("additionalPotentialOption3", item.path("additional_potential_option_3").asText(null));

        // 소울 정보
        details.put("soulName", item.path("soul_name").asText(null));
        details.put("soulOption", item.path("soul_option").asText(null));

        // 주문서 강화 횟수
        details.put("scrollUpgrade", item.path("scroll_upgrade").asText("0"));

        // 최종 스탯 옵션 (item_total_option)
        JsonNode totalOption = item.path("item_total_option");
        if (!totalOption.isMissingNode() && totalOption.isObject()) {
            Map<String, String> totalOptionMap = new HashMap<>();
            totalOption.fields().forEachRemaining(field -> {
                String value = field.getValue().asText("0");
                if (!"0".equals(value)) {
                    totalOptionMap.put(field.getKey(), value);
                }
            });
            details.put("itemTotalOption", totalOptionMap);
        }

        // 기본 스탯 옵션 (item_base_option)
        JsonNode baseOption = item.path("item_base_option");
        if (!baseOption.isMissingNode() && baseOption.isObject()) {
            Map<String, String> baseOptionMap = new HashMap<>();
            baseOption.fields().forEachRemaining(field -> {
                String value = field.getValue().asText("0");
                if (!"0".equals(value)) {
                    baseOptionMap.put(field.getKey(), value);
                }
            });
            details.put("itemBaseOption", baseOptionMap);
        }

        // 추가옵션 (item_add_option)
        JsonNode addOption = item.path("item_add_option");
        if (!addOption.isMissingNode() && addOption.isObject()) {
            Map<String, String> addOptionMap = new HashMap<>();
            addOption.fields().forEachRemaining(field -> {
                String value = field.getValue().asText("0");
                if (!"0".equals(value)) {
                    addOptionMap.put(field.getKey(), value);
                }
            });
            details.put("itemAddOption", addOptionMap);
        }

        // 주문서 강화 옵션 (item_etc_option)
        JsonNode etcOption = item.path("item_etc_option");
        if (!etcOption.isMissingNode() && etcOption.isObject()) {
            Map<String, String> etcOptionMap = new HashMap<>();
            etcOption.fields().forEachRemaining(field -> {
                String value = field.getValue().asText("0");
                if (!"0".equals(value)) {
                    etcOptionMap.put(field.getKey(), value);
                }
            });
            details.put("itemEtcOption", etcOptionMap);
        }

        // 스타포스 옵션 (item_starforce_option)
        JsonNode starforceOption = item.path("item_starforce_option");
        if (!starforceOption.isMissingNode() && starforceOption.isObject()) {
            Map<String, String> starforceOptionMap = new HashMap<>();
            starforceOption.fields().forEachRemaining(field -> {
                String value = field.getValue().asText("0");
                if (!"0".equals(value)) {
                    starforceOptionMap.put(field.getKey(), value);
                }
            });
            details.put("itemStarforceOption", starforceOptionMap);
        }

        // 아이템 설명 (세트효과 등)
        details.put("itemDescription", item.path("item_description").asText(null));

        // 특별한 링
        details.put("specialRingLevel", item.path("special_ring_level").asText(null));

        return details;
    }

    private String normalizeSlotName(String apiSlotName) {
        // Map Nexon API slot names to our internal slot names
        return switch (apiSlotName.toLowerCase()) {
            case "모자" -> "cap";
            case "신발" -> "shoes";
            case "훈장" -> "medal";
            case "방패", "보조무기" -> "shield";
            case "무기" -> "weapon";
            case "상의" -> "top";
            case "하의" -> "bottom";
            case "장갑" -> "gloves";
            case "망토" -> "cape";
            // 추가 슬롯 매핑
            case "반지1" -> "ring1";
            case "반지2" -> "ring2";
            case "반지3" -> "ring3";
            case "반지4" -> "ring4";
            case "펜던트" -> "pendant1";
            case "펜던트2" -> "pendant2";
            case "얼굴장식" -> "face";
            case "눈장식" -> "eye";
            case "어깨장식" -> "shoulder";
            case "벨트" -> "belt";
            case "포켓 아이템" -> "pocket";
            case "뱃지" -> "badge";
            case "기계 심장" -> "heart";
            case "엠블렘" -> "emblem";
            case "귀고리" -> "earring";
            default -> apiSlotName.toLowerCase().replace(" ", "_");
        };
    }

    private HttpEntity<Void> createRequest() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-nxopen-api-key", apiKey);
        headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
        return new HttpEntity<>(headers);
    }
}
