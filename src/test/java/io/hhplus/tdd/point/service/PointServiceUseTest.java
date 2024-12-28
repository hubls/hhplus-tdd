package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.UserPoint;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.UserPointRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PointServiceUseTest {
    @Mock
    private UserPointRepository userPointRepository;

    @Mock
    private PointHistoryRepository pointHistoryRepository;

    @InjectMocks
    private PointService pointService;

    @BeforeEach
    public void beforeEach() {
    }

    @Test
    @DisplayName("유저의 포인트사용을 잘 하는지 확인")
    void use() {
        // given
        long userId = 123L;
        long amount = 5_000L;
        long totalAmount = 0L;

        UserPoint initialUserPoint = new UserPoint(userId, amount, System.currentTimeMillis());
        UserPoint updatedUserPoint = new UserPoint(userId, totalAmount, System.currentTimeMillis());

        when(userPointRepository.findById(userId)).thenReturn(initialUserPoint);
        when(userPointRepository.saveOrUpdate(userId, totalAmount)).thenReturn(updatedUserPoint);

        // when
        UserPoint userPoint = pointService.useUserPoint(userId, amount);

        // then
        assertThat(userPoint.id()).isEqualTo(userId);
        assertThat(userPoint.point()).isEqualTo(totalAmount);
        verify(userPointRepository).findById(userId);
        verify(userPointRepository).saveOrUpdate(userId, totalAmount);
    }

    @Test
    @DisplayName("유저의 포인트를 2번 사용할 수 있는지 확인")
    void useTwice() {
        // given
        long userId = 456L;
        long chargeAmount = 5_000L;
        long cost = 1_000L;
        long remainingAmount = 3_000L;

        UserPoint initialUserPoint = new UserPoint(userId, chargeAmount, System.currentTimeMillis());
        UserPoint afterFirstUse = new UserPoint(userId, 4_000L, System.currentTimeMillis());
        UserPoint afterSecondUse = new UserPoint(userId, remainingAmount, System.currentTimeMillis());

        when(userPointRepository.findById(eq(userId))).thenReturn(initialUserPoint, afterFirstUse);
        when(userPointRepository.saveOrUpdate(eq(userId), anyLong()))
                .thenReturn(afterFirstUse, afterSecondUse);

        // when
        pointService.useUserPoint(userId, cost);
        UserPoint userPoint = pointService.useUserPoint(userId, cost);

        // then
        assertThat(userPoint.id()).isEqualTo(userId);
        assertThat(userPoint.point()).isEqualTo(remainingAmount);
        verify(userPointRepository, times(2)).findById(eq(userId));
        verify(userPointRepository, times(2)).saveOrUpdate(eq(userId), anyLong());
    }

    @Test
    @DisplayName("포인트 사용 초과 시 에러 발생 확인")
    void useMorePoint() {
        // given
        long userId = 789L;
        long chargeAmount = 5_000L;
        long excessiveAmount = 6_000L;

        UserPoint initialUserPoint = new UserPoint(userId, chargeAmount, System.currentTimeMillis());

        when(userPointRepository.findById(eq(userId))).thenReturn(initialUserPoint);

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pointService.useUserPoint(userId, excessiveAmount);
        });

        // 검증
        assertThat(exception.getMessage()).contains("잔고부족");
        verify(userPointRepository).findById(eq(userId));
    }
}
