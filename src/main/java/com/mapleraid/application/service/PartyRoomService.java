package com.mapleraid.application.service;

import com.mapleraid.application.port.out.PartyRoomRepository;
import com.mapleraid.application.port.out.ReviewRepository;
import com.mapleraid.application.port.out.UserRepository;
import com.mapleraid.domain.common.DomainException;
import com.mapleraid.domain.party.PartyRoom;
import com.mapleraid.domain.party.PartyRoomId;
import com.mapleraid.domain.party.PartyRoomStatus;
import com.mapleraid.domain.review.Review;
import com.mapleraid.domain.review.ReviewTag;
import com.mapleraid.domain.user.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class PartyRoomService {

    private final PartyRoomRepository partyRoomRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    public PartyRoomService(PartyRoomRepository partyRoomRepository,
                            ReviewRepository reviewRepository,
                            UserRepository userRepository) {
        this.partyRoomRepository = partyRoomRepository;
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
    }

    /**
     * 내 파티룸 목록 조회
     */
    @Transactional(readOnly = true)
    public List<PartyRoom> getMyPartyRooms(UserId userId, PartyRoomStatus status) {
        if (status != null) {
            return partyRoomRepository.findByMemberUserIdAndStatus(userId, status);
        }
        return partyRoomRepository.findByMemberUserId(userId);
    }

    /**
     * 파티룸 상세 조회
     */
    @Transactional(readOnly = true)
    public PartyRoom getPartyRoom(PartyRoomId partyRoomId, UserId requesterId) {
        PartyRoom partyRoom = partyRoomRepository.findById(partyRoomId)
                .orElseThrow(() -> new DomainException("PARTY_NOT_FOUND",
                        "파티룸을 찾을 수 없습니다."));

        if (!partyRoom.isMember(requesterId)) {
            throw new DomainException("PARTY_NOT_MEMBER",
                    "해당 파티룸의 멤버가 아닙니다.");
        }

        return partyRoom;
    }

    /**
     * 파티룸 탈퇴
     */
    public void leavePartyRoom(PartyRoomId partyRoomId, UserId requesterId) {
        PartyRoom partyRoom = partyRoomRepository.findById(partyRoomId)
                .orElseThrow(() -> new DomainException("PARTY_NOT_FOUND",
                        "파티룸을 찾을 수 없습니다."));

        partyRoom.removeMember(requesterId);
        partyRoomRepository.save(partyRoom);
    }

    /**
     * 파티 완료
     */
    public PartyRoom completePartyRoom(PartyRoomId partyRoomId, UserId requesterId) {
        PartyRoom partyRoom = partyRoomRepository.findById(partyRoomId)
                .orElseThrow(() -> new DomainException("PARTY_NOT_FOUND",
                        "파티룸을 찾을 수 없습니다."));

        partyRoom.complete(requesterId);

        // 모든 멤버의 완료 파티 수 증가
        for (UserId memberId : partyRoom.getActiveMemberIds()) {
            userRepository.findById(memberId).ifPresent(user -> {
                user.recordPartyCompletion();
                userRepository.save(user);
            });
        }

        return partyRoomRepository.save(partyRoom);
    }

    /**
     * 레디 체크 시작
     */
    public PartyRoom startReadyCheck(PartyRoomId partyRoomId, UserId requesterId) {
        PartyRoom partyRoom = partyRoomRepository.findById(partyRoomId)
                .orElseThrow(() -> new DomainException("PARTY_NOT_FOUND",
                        "파티룸을 찾을 수 없습니다."));

        partyRoom.startReadyCheck(requesterId);
        return partyRoomRepository.save(partyRoom);
    }

    /**
     * 레디 표시
     */
    public PartyRoom markReady(PartyRoomId partyRoomId, UserId requesterId) {
        PartyRoom partyRoom = partyRoomRepository.findById(partyRoomId)
                .orElseThrow(() -> new DomainException("PARTY_NOT_FOUND",
                        "파티룸을 찾을 수 없습니다."));

        partyRoom.markReady(requesterId);
        return partyRoomRepository.save(partyRoom);
    }

    /**
     * 파티 채팅 읽음 처리
     */
    public void markChatAsRead(PartyRoomId partyRoomId, UserId userId) {
        PartyRoom partyRoom = partyRoomRepository.findById(partyRoomId)
                .orElseThrow(() -> new DomainException("PARTY_NOT_FOUND",
                        "파티룸을 찾을 수 없습니다."));

        if (!partyRoom.isMember(userId)) {
            throw new DomainException("PARTY_NOT_MEMBER",
                    "해당 파티룸의 멤버가 아닙니다.");
        }

        partyRoom.clearUnreadCount(userId);
        partyRoomRepository.save(partyRoom);
    }

    /**
     * 리뷰 작성
     */
    public List<Review> submitReviews(PartyRoomId partyRoomId, UserId reviewerId,
                                      List<ReviewInput> reviewInputs) {
        PartyRoom partyRoom = partyRoomRepository.findById(partyRoomId)
                .orElseThrow(() -> new DomainException("PARTY_NOT_FOUND",
                        "파티룸을 찾을 수 없습니다."));

        // 파티 완료 상태 확인
        if (!partyRoom.isCompleted()) {
            throw new DomainException("PARTY_NOT_COMPLETED",
                    "완료된 파티만 리뷰를 작성할 수 있습니다.");
        }

        // 멤버 확인
        if (!partyRoom.isMember(reviewerId)) {
            throw new DomainException("PARTY_NOT_MEMBER",
                    "해당 파티룸의 멤버가 아닙니다.");
        }

        return reviewInputs.stream().map(input -> {
            // 중복 리뷰 확인
            if (reviewRepository.existsByPartyRoomIdAndReviewerIdAndRevieweeId(
                    partyRoomId, reviewerId, input.targetUserId())) {
                throw new DomainException("REVIEW_ALREADY_SUBMITTED",
                        "이미 해당 파티원에 대한 리뷰를 작성했습니다.",
                        Map.of("targetUserId", input.targetUserId().getValue()));
            }

            // 대상이 파티 멤버인지 확인
            if (!partyRoom.isMember(input.targetUserId())) {
                throw new DomainException("REVIEW_INVALID_TARGET",
                        "해당 사용자는 파티 멤버가 아닙니다.");
            }

            // 리뷰 생성
            Review review = Review.create(
                    partyRoomId,
                    reviewerId,
                    input.targetUserId(),
                    input.tags(),
                    input.comment(),
                    partyRoom.getCompletedAt()
            );

            Review savedReview = reviewRepository.save(review);

            // 대상자 온도 조정
            userRepository.findById(input.targetUserId()).ifPresent(user -> {
                user.adjustTemperature(review.getTemperatureChange());
                userRepository.save(user);
            });

            return savedReview;
        }).toList();
    }

    public record ReviewInput(UserId targetUserId, List<ReviewTag> tags, String comment) {
    }
}
