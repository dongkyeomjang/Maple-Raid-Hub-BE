package com.mapleraid.character.application.service.helper;

import com.mapleraid.external.application.port.out.NexonApiPort;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class VerificationHelper {

    private static final List<SymbolDefinition> ALL_SYMBOLS = List.of(
            new SymbolDefinition("아케인심볼 : 소멸의 여로", 200),
            new SymbolDefinition("아케인심볼 : 츄츄 아일랜드", 210),
            new SymbolDefinition("아케인심볼 : 레헬른", 220),
            new SymbolDefinition("아케인심볼 : 아르카나", 225),
            new SymbolDefinition("아케인심볼 : 모라스", 230),
            new SymbolDefinition("아케인심볼 : 에스페라", 235),
            new SymbolDefinition("어센틱심볼 : 세르니움", 260),
            new SymbolDefinition("어센틱심볼 : 아르크스", 265),
            new SymbolDefinition("어센틱심볼 : 오디움", 270),
            new SymbolDefinition("어센틱심볼 : 도원경", 275),
            new SymbolDefinition("어센틱심볼 : 아르테리아", 280),
            new SymbolDefinition("어센틱심볼 : 카르시온", 285),
            new SymbolDefinition("어센틱심볼 : 탈라하트", 290)
    );

    /**
     * 캐릭터 레벨에 따라 선택 가능한 심볼 목록 반환
     */
    public List<String> getAvailableSymbols(int characterLevel, NexonApiPort.SymbolEquipmentInfo symbolEquipment) {
        Set<String> ownedSymbols = symbolEquipment.symbols().stream()
                .map(NexonApiPort.SymbolInfo::symbolName)
                .collect(Collectors.toSet());

        int maxUnlockLevel;
        if (characterLevel < 280) {
            maxUnlockLevel = 275;
        } else if (characterLevel < 285) {
            maxUnlockLevel = 280;
        } else if (characterLevel < 290) {
            maxUnlockLevel = 285;
        } else {
            maxUnlockLevel = 290;
        }

        return ALL_SYMBOLS.stream()
                .filter(s -> s.unlockLevel() <= maxUnlockLevel)
                .map(SymbolDefinition::name)
                .filter(ownedSymbols::contains)
                .collect(Collectors.toList());
    }

    /**
     * 목록에서 랜덤하게 N개 선택
     */
    public List<String> selectRandomSymbols(List<String> symbols, int count) {
        List<String> shuffled = new ArrayList<>(symbols);
        Collections.shuffle(shuffled);
        return shuffled.subList(0, Math.min(count, shuffled.size()));
    }

    private record SymbolDefinition(String name, int unlockLevel) {
    }
}
