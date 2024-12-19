package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.UserPoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class PointServiceUseTest {
    @Autowired
    private PointService pointService;

    @Test
    @DisplayName("유저의 포인트사용을 잘 하는지 확인")
    void use() {
        // given
        long userId = 123L;
        long amount = 5_000L;
        long totalAmount = 0L;

        // when (먼저 충전)
        pointService.chargeUserPoint(userId, amount);

        // 포인트 사용
        UserPoint userPoint = pointService.useUserPoint(userId, amount);

        // then
        assertThat(userPoint.id()).isEqualTo(userId);
        assertThat(userPoint.point()).isEqualTo(totalAmount);
    }

    @Test
    @DisplayName("유저의 포인트사용 2번을 잘 하는지 확인")
    void useTwice() {
        // given
        long userId = 456L;
        long amount = 5_000L;
        long cost = 1_000L;
        long totalAmount = 3000L;

        // when (먼저 5,000 충전)
        pointService.chargeUserPoint(userId, amount);

        // 포인트 2번 사용
        pointService.useUserPoint(userId, cost);
        UserPoint userPoint = pointService.useUserPoint(userId, cost);

        // then
        assertThat(userPoint.id()).isEqualTo(userId);
        assertThat(userPoint.point()).isEqualTo(totalAmount);
    }

    @Test
    @DisplayName("유저의 포인트사용을 초과할때 에러발생 하는지")
    void useMorePoint() {
        // given
        long userId = 789L;
        long chargeAmount = 5_000L;
        long excessiveAmount = 6_000L; // 초과 사용 시도

        // 충전
        pointService.chargeUserPoint(userId, chargeAmount);

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pointService.useUserPoint(userId, excessiveAmount);
        });

        // 예외 메시지 검증
        assertThat(exception.getMessage()).contains("잔고부족");
    }
}
