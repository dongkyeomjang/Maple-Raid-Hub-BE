package com.mapleraid.party.domain;

import com.mapleraid.character.domain.CharacterId;
import com.mapleraid.user.domain.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class PartyMemberTest {

    @Test
    @DisplayName("leave()를 호출하면 leftAt이 설정되고, hasLeft()가 true, isActive()가 false가 된다")
    void leave_setsLeftAtAndChangesStatus() {
        PartyMember member = new PartyMember(UserId.generate(), CharacterId.generate(), false);

        assertThat(member.isActive()).isTrue();
        assertThat(member.hasLeft()).isFalse();
        assertThat(member.getLeftAt()).isNull();

        member.leave();

        assertThat(member.isActive()).isFalse();
        assertThat(member.hasLeft()).isTrue();
        assertThat(member.getLeftAt()).isNotNull();
    }

    @Test
    @DisplayName("reconstitute로 복원된 탈퇴 멤버의 상태가 올바르다")
    void reconstitute_leftMember_statusCorrect() {
        java.time.Instant leftAt = java.time.Instant.now();

        PartyMember member = PartyMember.reconstitute(
                1L, UserId.generate(), CharacterId.generate(),
                false, false, null,
                java.time.Instant.now().minusSeconds(3600), leftAt, 0
        );

        assertThat(member.isActive()).isFalse();
        assertThat(member.hasLeft()).isTrue();
        assertThat(member.getLeftAt()).isEqualTo(leftAt);
    }

    @Test
    @DisplayName("활성 멤버의 상태가 올바르다")
    void activeMember_statusCorrect() {
        PartyMember member = new PartyMember(UserId.generate(), CharacterId.generate(), true);

        assertThat(member.isActive()).isTrue();
        assertThat(member.hasLeft()).isFalse();
        assertThat(member.isLeader()).isTrue();
    }
}
