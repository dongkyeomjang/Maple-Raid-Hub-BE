package com.mapleraid.domain.post;

public enum ApplicationStatus {
    /**
     * 지원됨 (대기중)
     */
    APPLIED,

    /**
     * 수락됨
     */
    ACCEPTED,

    /**
     * 거절됨
     */
    REJECTED,

    /**
     * 모집글 취소로 취소됨
     */
    CANCELED,

    /**
     * 지원자가 취소함
     */
    WITHDRAWN
}
