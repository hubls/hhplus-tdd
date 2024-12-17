package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.UserPoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class PointServiceChargeTest {
    @Autowired
    private PointService pointService;

    @Test
    @DisplayName("최초 유저의 포인트가 잘 충전 되었는지 확인")
    void save() {
        // given
        long userId = 123L;
        long amount = 5_000L;

        // when
        UserPoint userPoint = pointService.chargeUserPoint(userId, amount);

        // then
        assertThat(userPoint.id()).isEqualTo(userId);
        assertThat(userPoint.point()).isEqualTo(amount);
    }

    @Test
    @DisplayName("기존 유저가 포인트 충전 2회가 충전되었는지 확인")
    void saveTwice() {
        // given
        long userId = 456;
        long amount = 5000;
        long totalAmount = 10000;

        // 최초 충전
        UserPoint userPoint = pointService.chargeUserPoint(userId, amount);

        // when (2회 충전)
        UserPoint updatedUserPoint = pointService.chargeUserPoint(userId, amount);

        // then
        assertThat(updatedUserPoint.id()).isEqualTo(userId);
        assertThat(updatedUserPoint.point()).isEqualTo(totalAmount);
    }

    @Test
    @DisplayName("신규 유저가 포인트 최대 충전을 넘어서려 할때")
    void saveMaxPoint() {
        // given
        long userId = 789;
        long amount = 1_000_000L;

        // when: 초과 충전 시도 (IllegalArgumentException 발생)
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pointService.chargeUserPoint(userId, amount); // 초과 충전 1,000,000 추가 시도
        });

        // then: 예외 메시지 검증
        assertThat(exception.getMessage()).contains("충전가능한 최대 포인트");
    }

    @Test
    @DisplayName("기존 유저가 포인트 최대 충전을 넘어서려 할때")
    void saveMaxPointTwice() {
        // given
        long userId = 111;
        long amount = 10_000L;
        long totalAmount = 1_000_000L;

        // 최초 충전
        UserPoint userPoint = pointService.chargeUserPoint(userId, amount);

        // when: 초과 충전 시도 (IllegalArgumentException 발생)
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pointService.chargeUserPoint(userId, totalAmount); // 초과 충전 1,000,000 추가 시도
        });

        // then: 예외 메시지 검증
        assertThat(exception.getMessage()).contains("충전가능한 최대 포인트");
    }
}