package com.mapleraid.character.domain.type;

/**
 * 캐릭터 소유권 인증 상태
 */
public enum EVerificationStatus {
    /**
     * 등록됨, 미인증
     */
    UNVERIFIED_CLAIMED,

    /**
     * 소유권 인증 완료
     */
    VERIFIED_OWNER,

    /**
     * 분쟁 중 (다른 사용자가 인증 시도)
     */
    DISPUTED,

    /**
     * 무효화됨 (다른 사용자가 인증 성공)
     */
    REVOKED
}
