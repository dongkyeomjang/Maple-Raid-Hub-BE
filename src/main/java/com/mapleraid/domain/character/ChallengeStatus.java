package com.mapleraid.domain.character;

/**
 * 인증 챌린지 상태
 */
public enum ChallengeStatus {
    /**
     * 진행 중
     */
    PENDING,

    /**
     * 성공
     */
    SUCCESS,

    /**
     * 실패 (최대 검사 횟수 초과)
     */
    FAILED,

    /**
     * 만료
     */
    EXPIRED
}
