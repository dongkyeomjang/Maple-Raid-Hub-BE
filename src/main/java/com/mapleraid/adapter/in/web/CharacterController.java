package com.mapleraid.adapter.in.web;

import com.mapleraid.adapter.in.web.dto.ApiResponse;
import com.mapleraid.adapter.in.web.dto.character.CharacterResponse;
import com.mapleraid.adapter.in.web.dto.character.ClaimCharacterRequest;
import com.mapleraid.adapter.in.web.security.CurrentUser;
import com.mapleraid.application.service.CharacterService;
import com.mapleraid.domain.character.Character;
import com.mapleraid.domain.character.CharacterId;
import com.mapleraid.domain.user.UserId;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/characters")
public class CharacterController {

    private final CharacterService characterService;

    public CharacterController(CharacterService characterService) {
        this.characterService = characterService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CharacterResponse>> claimCharacter(
            @CurrentUser UserId userId,
            @Valid @RequestBody ClaimCharacterRequest request) {

        Character character = characterService.claimCharacter(
                userId,
                request.characterName(),
                request.worldName()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(CharacterResponse.from(character)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CharacterResponse>>> getMyCharacters(@CurrentUser UserId userId) {
        List<Character> characters = characterService.getMyCharacters(userId);

        List<CharacterResponse> responses = characters.stream()
                .map(CharacterResponse::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/{characterId}")
    public ResponseEntity<ApiResponse<CharacterResponse>> getCharacter(
            @PathVariable String characterId) {

        Character character = characterService.getCharacter(CharacterId.of(characterId));
        return ResponseEntity.ok(ApiResponse.success(CharacterResponse.from(character)));
    }

    @DeleteMapping("/{characterId}")
    public ResponseEntity<Void> deleteCharacter(
            @CurrentUser UserId userId,
            @PathVariable String characterId) {

        characterService.deleteCharacter(CharacterId.of(characterId), userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{characterId}/sync")
    public ResponseEntity<ApiResponse<CharacterResponse>> syncCharacter(
            @PathVariable String characterId) {

        Character character = characterService.syncCharacterInfo(CharacterId.of(characterId));
        return ResponseEntity.ok(ApiResponse.success(CharacterResponse.from(character)));
    }
}
