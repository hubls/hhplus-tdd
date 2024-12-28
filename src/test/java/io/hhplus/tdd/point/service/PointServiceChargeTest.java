package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.UserPointRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointServiceChargeTest {
    @Mock
    private UserPointRepository userPointRepository;

    @Mock
    private PointHistoryRepository pointHistoryRepository;

    @InjectMocks
    private PointService pointService;

    @BeforeEach
    public void beforeEach() {}

    @Test
    void 최초_유저_포인트_충전_성공() {
        // given
        long userId = 123L;
        long amount = 5_000L;
        UserPoint newUserPoint = new UserPoint(userId, 0L, System.currentTimeMillis());
        UserPoint updatedUserPoint = new UserPoint(userId, amount, System.currentTimeMillis());

        // when
        when(userPointRepository.findById(eq(userId))).thenReturn(newUserPoint);
        when(userPointRepository.saveOrUpdate(eq(userId), eq(amount))).thenReturn(updatedUserPoint);

        UserPoint result = pointService.chargeUserPoint(userId, amount);

        // then
        assertThat(result.point()).isEqualTo(amount);
        verify(userPointRepository).findById(eq(userId));
        verify(userPointRepository).saveOrUpdate(eq(userId), eq(amount));
        verify(pointHistoryRepository).save(eq(userId), eq(amount), eq(TransactionType.CHARGE), anyLong());
    }

    @Test
    void 기존_유저_포인트_추가_충전() {
        // given
        long userId = 456L;
        long initialAmount = 5_000L;
        long additionalAmount = 5_000L;
        long totalAmount = 10_000L;

        UserPoint existingUserPoint = new UserPoint(userId, initialAmount, System.currentTimeMillis());
        UserPoint updatedUserPoint = new UserPoint(userId, totalAmount, System.currentTimeMillis());

        // when
        when(userPointRepository.findById(eq(userId))).thenReturn(existingUserPoint);
        when(userPointRepository.saveOrUpdate(eq(userId), eq(totalAmount))).thenReturn(updatedUserPoint);

        // PointHistoryRepository 호출 검증
        pointService.chargeUserPoint(userId, additionalAmount);

        // then
        verify(userPointRepository).findById(eq(userId));
        verify(userPointRepository).saveOrUpdate(eq(userId), eq(totalAmount));
        verify(pointHistoryRepository).save(eq(userId), eq(additionalAmount), eq(TransactionType.CHARGE), anyLong());
    }

    @Test
    void 포인트_초과_충전_실패() {
        // given
        long userId = 789L;
        long maxPoint = 10_000L;
        long exceedingAmount = 1_000_001L;

        UserPoint existingUserPoint = new UserPoint(userId, maxPoint, System.currentTimeMillis());

        // when
        when(userPointRepository.findById(eq(userId))).thenReturn(existingUserPoint);

        // then
        assertThatThrownBy(() -> pointService.chargeUserPoint(userId, exceedingAmount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("충전가능한 최대 포인트");

        verify(userPointRepository).findById(eq(userId));
        verify(pointHistoryRepository, never()).save(anyLong(), anyLong(), any(TransactionType.class), anyLong());
    }
}