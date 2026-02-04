package com.mapleraid.core.exception.definition;

import com.mapleraid.core.constant.Constants;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Method Not Allowed Error
    METHOD_NOT_ALLOWED(40500, HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 HTTP 메소드입니다."),

    // Invalid Argument Error
    MISSING_REQUEST_PARAMETER(40000, HttpStatus.BAD_REQUEST, "필수 요청 파라미터가 누락되었습니다."),
    INVALID_ARGUMENT(40001, HttpStatus.BAD_REQUEST, "요청에 유효하지 않은 인자입니다."),
    INVALID_PARAMETER_FORMAT(40002, HttpStatus.BAD_REQUEST, "요청에 유효하지 않은 인자 형식입니다."),
    INVALID_HEADER_ERROR(40003, HttpStatus.BAD_REQUEST, "유효하지 않은 헤더입니다."),
    MISSING_REQUEST_HEADER(40004, HttpStatus.BAD_REQUEST, "필수 요청 헤더가 누락되었습니다."),
    BAD_REQUEST_PARAMETER(40005, HttpStatus.BAD_REQUEST, "잘못된 요청 파라미터입니다."),
    UNSUPPORTED_MEDIA_TYPE(40006, HttpStatus.BAD_REQUEST, "지원하지 않는 미디어 타입입니다."),
    BAD_REQUEST_JSON(40007, HttpStatus.BAD_REQUEST, "잘못된 JSON 형식입니다."),
    INVALID_SECURITY_INFO(400001, HttpStatus.BAD_REQUEST, "필수 정보가 누락되었습니다."),
    INVALID_WORLD(40008, HttpStatus.BAD_REQUEST, "유효하지 않은 월드입니다."),
    CHARACTER_LEVEL_TOO_LOW(40009, HttpStatus.BAD_REQUEST, String.format("레벨 %d 이상의 캐릭터만 등록할 수 있습니다.", Constants.MIN_CHARACTER_LEVEL)),
    VERIFICATION_INVALID_STATUS(40010, HttpStatus.BAD_REQUEST, "이미 완료된 챌린지입니다."),
    VERIFICATION_EXPIRED(40011, HttpStatus.BAD_REQUEST, "인증 시간이 만료되었습니다."),
    VERIFICATION_MAX_CHECKS(40012, HttpStatus.BAD_REQUEST, "최대 검사 횟수(10회)를 초과했습니다."),
    VERIFICATION_RATE_LIMITED(40013, HttpStatus.BAD_REQUEST, "잠시 후 다시 시도해주세요."),
    CHARACTER_CANNOT_VERIFY(40014, HttpStatus.BAD_REQUEST, "인증할 수 없는 상태의 캐릭터입니다."),
    VERIFICATION_DAILY_LIMIT(40015, HttpStatus.BAD_REQUEST, String.format("일일 인증 시도 횟수(%d회)를 초과했습니다.", Constants.DAILY_CHALLENGE_LIMIT)),
    VERIFICATION_COOLDOWN(40016, HttpStatus.BAD_REQUEST, "인증 실패 후 1시간이 지나야 재시도할 수 있습니다."),
    NOT_ENOUGH_SYMBOLS(40017, HttpStatus.BAD_REQUEST, "인증에 필요한 심볼이 부족합니다. 최소 2개 이상의 심볼이 필요합니다."),
    REVIEW_SELF_REVIEW(40018, HttpStatus.BAD_REQUEST, "본인에게는 리뷰를 작성할 수 없습니다."),
    REVIEW_NO_TAGS(40019, HttpStatus.BAD_REQUEST, "최소 1개의 태그를 선택해야 합니다."),
    REVIEW_TOO_MANY_TAGS(40020, HttpStatus.BAD_REQUEST, "최대 3개의 태그만 선택할 수 있습니다."),
    REVIEW_WINDOW_EXPIRED(40021, HttpStatus.BAD_REQUEST, "리뷰 작성 기간(7일)이 지났습니다."),
    DM_SAME_USER(40022, HttpStatus.BAD_REQUEST, "자기 자신에게 DM을 보낼 수 없습니다."),
    DM_REQUIRES_VERIFIED_CHARACTER(40023, HttpStatus.BAD_REQUEST, "DM을 보내려면 인증된 캐릭터가 필요합니다."),
    POST_REQUIRES_VERIFIED_CHARACTER(40024, HttpStatus.BAD_REQUEST, "모집글 작성을 위해 인증된 캐릭터가 필요합니다."),
    APPLICATION_REQUIRES_VERIFIED_CHARACTER(40025, HttpStatus.BAD_REQUEST, "지원하려면 인증된 캐릭터가 필요합니다."),
    POST_NO_BOSS_SELECTED(40026, HttpStatus.BAD_REQUEST, "최소 1개 이상의 보스를 선택해야 합니다."),
    POST_INVALID_MEMBER_COUNT(40027, HttpStatus.BAD_REQUEST, "모집 인원은 2~6명이어야 합니다."),
    APPLICATION_SELF_APPLY(40028, HttpStatus.BAD_REQUEST, "본인 모집글에는 지원할 수 없습니다."),
    APPLICATION_WORLD_GROUP_MISMATCH(40029, HttpStatus.BAD_REQUEST, "해당 모집글의 월드 그룹과 일치하지 않습니다."),
    APPLICATION_POST_NOT_RECRUITING(40030, HttpStatus.BAD_REQUEST, "모집이 마감된 모집글입니다."),
    APPLICATION_CANNOT_WITHDRAW(40031, HttpStatus.BAD_REQUEST, "대기 중인 지원만 취소할 수 있습니다."),
    APPLICATION_INVALID_STATUS(40032, HttpStatus.BAD_REQUEST, "대기 중인 지원만 처리할 수 있습니다."),
    POST_CANNOT_CLOSE(40033, HttpStatus.BAD_REQUEST, "모집 중인 모집글만 마감할 수 있습니다."),
    POST_INSUFFICIENT_MEMBERS(40034, HttpStatus.BAD_REQUEST, "파티를 결성하려면 최소 2명 이상이 필요합니다."),
    POST_HAS_PARTY_ROOM(40035, HttpStatus.BAD_REQUEST, "파티룸이 생성된 모집글은 취소할 수 없습니다."),
    POST_NOT_EDITABLE(40036, HttpStatus.BAD_REQUEST, "모집 중인 모집글만 수정할 수 있습니다."),
    POST_MEMBER_COUNT_BELOW_CURRENT(40037, HttpStatus.BAD_REQUEST, "현재 파티원 수보다 적은 인원으로 변경할 수 없습니다."),
    PARTY_LEADER_CANNOT_LEAVE(40038, HttpStatus.BAD_REQUEST, "파티장은 탈퇴할 수 없습니다. 파티를 취소하거나 리더를 위임해주세요."),
    PARTY_NO_READY_CHECK(40039, HttpStatus.BAD_REQUEST, "레디 체크가 시작되지 않았습니다."),
    CHARACTER_WORLD_MISMATCH(40043, HttpStatus.BAD_REQUEST, "입력한 월드와 캐릭터의 실제 월드가 일치하지 않습니다."),
    PARTY_NOT_ACTIVE(40040, HttpStatus.BAD_REQUEST, "활성 상태의 파티룸이 아닙니다."),
    PARTY_NOT_COMPLETED(40041, HttpStatus.BAD_REQUEST, "완료된 파티만 리뷰를 작성할 수 있습니다."),
    REVIEW_INVALID_TARGET(40042, HttpStatus.BAD_REQUEST, "해당 사용자는 파티 멤버가 아닙니다."),

    // Access Denied Error
    ACCESS_DENIED(40300, HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    CHARACTER_NOT_OWNER(40301, HttpStatus.FORBIDDEN, "본인의 캐릭터만 사용할 수 있습니다."),
    POST_NOT_AUTHOR(40302, HttpStatus.FORBIDDEN, "모집글 작성자만 처리할 수 있습니다."),
    DM_NOT_PARTICIPANT(40303, HttpStatus.FORBIDDEN, "해당 DM 방의 참가자가 아닙니다."),
    APPLICATION_NOT_OWNER(40304, HttpStatus.FORBIDDEN, "본인의 지원만 취소할 수 있습니다."),
    PARTY_NOT_LEADER(40305, HttpStatus.FORBIDDEN, "파티장만 이 작업을 수행할 수 있습니다."),
    PARTY_NOT_MEMBER(40306, HttpStatus.FORBIDDEN, "해당 파티룸의 멤버가 아닙니다."),

    // Unauthorized Error
    FAILURE_LOGIN(40100, HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다."),
    EXPIRED_TOKEN_ERROR(40101, HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    INVALID_TOKEN_ERROR(40102, HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    TOKEN_MALFORMED_ERROR(40103, HttpStatus.UNAUTHORIZED, "토큰이 올바르지 않습니다."),
    TOKEN_TYPE_ERROR(40104, HttpStatus.UNAUTHORIZED, "토큰 타입이 일치하지 않거나 비어있습니다."),
    TOKEN_UNSUPPORTED_ERROR(40105, HttpStatus.UNAUTHORIZED, "지원하지 않는 토큰입니다."),
    TOKEN_GENERATION_ERROR(40106, HttpStatus.UNAUTHORIZED, "토큰 생성에 실패하였습니다."),
    TOKEN_UNKNOWN_ERROR(40107, HttpStatus.UNAUTHORIZED, "알 수 없는 토큰입니다."),
    INVALID_API_KEY_ERROR(40108, HttpStatus.UNAUTHORIZED, "유효하지 않은 API KEY입니다."),
    AUTH_INVALID_CREDENTIALS(40109, HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다."),
    UNAUTHORIZED(40110, HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),

    // Not Found Error
    NOT_FOUND_END_POINT(40400, HttpStatus.NOT_FOUND, "존재하지 않는 API 엔드포인트입니다."),
    NOT_FOUND_AUTHORIZATION_HEADER(40401, HttpStatus.NOT_FOUND, "Authorization 헤더가 존재하지 않습니다."),
    NOT_FOUND_CHARACTER(40402, HttpStatus.NOT_FOUND, "해당 캐릭터를 찾을 수 없습니다. 캐릭터명과 월드를 확인해주세요."),
    NOT_FOUND_VERIFICATION_CHALLENGE(40403, HttpStatus.NOT_FOUND, "챌린지를 찾을 수 없습니다."),
    POST_NOT_FOUND(40404, HttpStatus.NOT_FOUND, "모집글을 찾을 수 없습니다."),
    DM_ROOM_NOT_FOUND(40405, HttpStatus.NOT_FOUND, "DM 방을 찾을 수 없습니다."),
    PARTY_NOT_FOUND(40406, HttpStatus.NOT_FOUND, "파티룸을 찾을 수 없습니다."),
    APPLICATION_NOT_FOUND(40407, HttpStatus.NOT_FOUND, "해당 지원을 찾을 수 없습니다."),
    USER_NOT_FOUND(40408, HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),

    // Conflict Error
    CHARACTER_ALREADY_REGISTERED(40900, HttpStatus.CONFLICT, "이미 등록한 캐릭터입니다."),
    CHARACTER_ALREADY_VERIFIED(40901, HttpStatus.CONFLICT, "이 캐릭터는 이미 다른 사용자가 인증했습니다."),
    VERIFICATION_ALREADY_PENDING(40902, HttpStatus.CONFLICT, "이미 진행 중인 인증이 있습니다."),
    CHARACTER_ALREADY_REVOKED(40903, HttpStatus.CONFLICT, "이미 다른 사용자에게 인증된 캐릭터입니다."),
    AUTH_ALREADY_EXISTS(40904, HttpStatus.CONFLICT, "이미 가입된 사용자입니다."),
    AUTH_USERNAME_DUPLICATE(40905, HttpStatus.CONFLICT, "이미 사용 중인 아이디입니다."),
    AUTH_NICKNAME_DUPLICATE(40906, HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),
    APPLICATION_DUPLICATE(40907, HttpStatus.CONFLICT, "이미 지원한 모집글입니다."),
    REVIEW_ALREADY_SUBMITTED(40908, HttpStatus.CONFLICT, "이미 해당 파티원에 대한 리뷰를 작성했습니다."),
    PARTY_ALREADY_MEMBER(40909, HttpStatus.CONFLICT, "이미 파티룸의 멤버입니다."),

    // Internal Server Error
    INTERNAL_SERVER_ERROR(50000, HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 에러입니다."),
    INTERNAL_DATA_ERROR(50001, HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 데이터 에러입니다."),
    UPLOAD_FILE_ERROR(50002, HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패하였습니다."),
    INTERNAL_SERVER_ERROR_IN_SQL(50006, HttpStatus.INTERNAL_SERVER_ERROR, "SQL 처리 중 에러가 발생하였습니다."),
    CONVERT_FILE_ERROR(50007, HttpStatus.INTERNAL_SERVER_ERROR, "파일 변환에 실패하였습니다."),

    // External Server Error
    EXTERNAL_SERVER_ERROR(50200, HttpStatus.BAD_GATEWAY, "시스템 에러입니다. \n잠시후 다시 시도해주세요."),
    CHARACTER_INFO_UNAVAILABLE(50201, HttpStatus.BAD_GATEWAY, "캐릭터 정보를 조회할 수 없습니다."),
    CHARACTER_SYMBOL_UNAVAILABLE(50202, HttpStatus.BAD_GATEWAY, "심볼 정보를 조회할 수 없습니다."),

    // ============================================== STOMP ERROR ==============================================
    // === 400 ====


    // === 404 ====

    // === 500 ====
    SYSTEM_ERROR_IN_BIKE_COMMAND(500001, HttpStatus.INTERNAL_SERVER_ERROR, "시스템 에러입니다.\n관리자에게 문의해주세요."),
    ;


    private final Integer code;
    private final HttpStatus httpStatus;
    private final String message;
}
