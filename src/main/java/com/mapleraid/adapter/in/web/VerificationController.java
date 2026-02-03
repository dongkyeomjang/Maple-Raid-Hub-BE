package com.mapleraid.adapter.in.web;

import com.mapleraid.adapter.in.web.dto.ApiResponse;
import com.mapleraid.adapter.in.web.dto.verification.ChallengeResponse;
import com.mapleraid.adapter.in.web.dto.verification.VerificationResultResponse;
import com.mapleraid.adapter.in.web.security.CurrentUser;
import com.mapleraid.application.service.VerificationService;
import com.mapleraid.domain.character.CharacterId;
import com.mapleraid.domain.character.VerificationChallenge;
import com.mapleraid.domain.character.VerificationChallengeId;
import com.mapleraid.domain.user.UserId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/verification")
public class VerificationController {

    private final VerificationService verificationService;

    public VerificationController(VerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @PostMapping("/characters/{characterId}/challenges")
    public ResponseEntity<ApiResponse<ChallengeResponse>> createChallenge(
            @CurrentUser UserId userId,
            @PathVariable String characterId) {

        VerificationChallenge challenge = verificationService.createChallenge(
                CharacterId.of(characterId),
                userId
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(ChallengeResponse.from(challenge)));
    }

    @GetMapping("/challenges/{challengeId}")
    public ResponseEntity<ApiResponse<ChallengeResponse>> getChallengeStatus(
            @PathVariable String challengeId) {

        VerificationChallenge challenge = verificationService.getChallengeStatus(
                VerificationChallengeId.of(challengeId)
        );

        return ResponseEntity.ok(ApiResponse.success(ChallengeResponse.from(challenge)));
    }

    @GetMapping("/characters/{characterId}/challenges/pending")
    public ResponseEntity<ApiResponse<ChallengeResponse>> getPendingChallenge(
            @PathVariable String characterId) {

        return verificationService.getPendingChallenge(CharacterId.of(characterId))
                .map(challenge -> ResponseEntity.ok(ApiResponse.success(ChallengeResponse.from(challenge))))
                .orElse(ResponseEntity.ok(ApiResponse.success(null)));
    }

    @PostMapping("/challenges/{challengeId}/check")
    public ResponseEntity<ApiResponse<VerificationResultResponse>> checkVerification(
            @CurrentUser UserId userId,
            @PathVariable String challengeId) {

        VerificationChallenge.VerificationResult result = verificationService.checkVerification(
                VerificationChallengeId.of(challengeId),
                userId
        );

        return ResponseEntity.ok(ApiResponse.success(VerificationResultResponse.from(result)));
    }
}
