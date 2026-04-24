package com.mapleraid.core.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

/**
 * 관리자용 Discord 링크 버튼에 사용할 HMAC 서명/검증 유틸.
 *
 * <ul>
 *   <li>링크: {@code .../cancel?exp={unixMs}&sig={HMAC}}</li>
 *   <li>서명 대상 문자열: {@code action + ":" + postId + ":" + exp}</li>
 *   <li>알고리즘: HMAC-SHA256 → URL-safe Base64 (padding 없음)</li>
 *   <li>secret 이 비어있으면 생성 측 {@code null}, 검증 측 항상 {@code false}</li>
 * </ul>
 */
@Slf4j
@Component
public class AdminActionSigner {

    private static final String HMAC_ALGO = "HmacSHA256";
    private static final Duration LINK_TTL = Duration.ofDays(7);

    private final byte[] secretBytes;
    private final boolean enabled;

    public AdminActionSigner(@Value("${admin.action-secret:}") String secret) {
        if (secret == null || secret.isBlank()) {
            this.secretBytes = new byte[0];
            this.enabled = false;
            log.warn("[AdminActionSigner] admin.action-secret 이 설정되지 않았습니다. 관리자 액션 링크 기능이 비활성화됩니다.");
        } else {
            this.secretBytes = secret.getBytes(StandardCharsets.UTF_8);
            this.enabled = true;
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 만료시각과 서명을 동시에 계산해 반환.
     * secret 미설정이면 null 반환.
     */
    public SignedParams sign(String action, String postId) {
        if (!enabled) {
            return null;
        }
        long exp = Instant.now().plus(LINK_TTL).toEpochMilli();
        String sig = computeHmac(action, postId, exp);
        return new SignedParams(exp, sig);
    }

    /**
     * 서명 + 만료시각 검증. secret 미설정이면 항상 false.
     */
    public boolean verify(String action, String postId, long exp, String sig) {
        if (!enabled) {
            return false;
        }
        if (sig == null || sig.isBlank()) {
            return false;
        }
        if (Instant.now().toEpochMilli() > exp) {
            return false;
        }
        String expected = computeHmac(action, postId, exp);
        // timing-safe 비교
        byte[] a = expected.getBytes(StandardCharsets.UTF_8);
        byte[] b = sig.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(a, b);
    }

    private String computeHmac(String action, String postId, long exp) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGO);
            mac.init(new SecretKeySpec(secretBytes, HMAC_ALGO));
            String payload = action + ":" + postId + ":" + exp;
            byte[] raw = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
        } catch (Exception e) {
            throw new IllegalStateException("HMAC 계산 실패", e);
        }
    }

    public record SignedParams(long exp, String sig) {
    }
}
