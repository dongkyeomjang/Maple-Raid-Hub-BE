package com.mapleraid.post.adapter.in.web.admin;

import com.mapleraid.core.security.AdminActionSigner;
import com.mapleraid.post.application.port.out.PostRepository;
import com.mapleraid.post.domain.Post;
import com.mapleraid.post.domain.PostId;
import com.mapleraid.post.domain.PostStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 관리자(개발자)가 Discord DM 의 Link Button 을 통해 호출하는 엔드포인트.
 * 인증 대신 HMAC 서명 + 만료시각 기반으로 요청 유효성 검증.
 * secret 이 설정되지 않으면 AdminActionSigner 가 모든 요청을 거부한다.
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/guest-posts")
@RequiredArgsConstructor
public class AdminGuestPostController {

    public static final String ACTION_CANCEL = "cancel";

    private final PostRepository postRepository;
    private final AdminActionSigner signer;

    @GetMapping(value = "/{postId}/cancel", produces = MediaType.TEXT_HTML_VALUE + "; charset=UTF-8")
    @Transactional
    public ResponseEntity<String> cancelGuestPost(
            @PathVariable String postId,
            @RequestParam("exp") long exp,
            @RequestParam("sig") String sig) {

        if (!signer.verify(ACTION_CANCEL, postId, exp, sig)) {
            log.warn("[관리자 액션] 서명 검증 실패 postId={}", postId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(renderPage("⛔ 요청 거부", "서명이 유효하지 않거나 링크가 만료되었습니다."));
        }

        Post post = postRepository.findById(PostId.of(postId)).orElse(null);
        if (post == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(renderPage("❓ 모집글 없음", "해당 모집글을 찾을 수 없습니다. 이미 삭제되었을 수 있습니다."));
        }
        if (!post.isGuest()) {
            log.warn("[관리자 액션] 비회원 글이 아님 postId={}", postId);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(renderPage("⛔ 거부", "이 엔드포인트는 비회원 모집글에 대해서만 동작합니다."));
        }
        if (post.getStatus() == PostStatus.CANCELED) {
            return ResponseEntity.ok(renderPage("ℹ️ 이미 처리됨",
                    "이 모집글은 이미 취소된 상태입니다. (postId=" + postId + ")"));
        }

        post.cancel();
        postRepository.save(post);
        log.info("[관리자 액션] 비회원 모집글 취소 처리 postId={}", postId);

        return ResponseEntity.ok(renderPage("✅ 글 내림 완료",
                "비회원 모집글이 취소(CANCELED) 상태로 변경되었습니다.<br>postId=" + postId));
    }

    private String renderPage(String title, String bodyHtml) {
        return "<!doctype html><html lang=\"ko\"><head><meta charset=\"utf-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">"
                + "<title>" + escape(title) + "</title>"
                + "<style>"
                + "body{font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',sans-serif;"
                + "background:#0f1115;color:#e6e8ee;margin:0;padding:48px 24px;}"
                + ".card{max-width:520px;margin:0 auto;background:#1a1d24;border:1px solid #2a2e37;"
                + "border-radius:12px;padding:32px;}"
                + "h1{margin:0 0 16px;font-size:22px;}"
                + "p{line-height:1.6;color:#b9bec8;margin:0;word-break:break-all;}"
                + "</style></head>"
                + "<body><div class=\"card\"><h1>" + escape(title) + "</h1><p>" + bodyHtml + "</p></div></body></html>";
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
