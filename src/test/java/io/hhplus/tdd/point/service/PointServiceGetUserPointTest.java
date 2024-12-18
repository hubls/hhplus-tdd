package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.UserPointRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class) // Mockito 확장을 통해 Mockito가 테스트에서 사용할 목업 객체를 주입해줄 수 있도록 설정
public class PointServiceGetUserPointTest {
    @Mock
    private UserPointRepository userPointRepository;

    @Mock
    private PointHistoryRepository pointHistoryRepository;

    @InjectMocks
    private PointService pointService;

    @Test
    void 포인트조회_성공케이스() {
        // given
        long userId = 123L;
        long currentPoint = 5000L;
        UserPoint userPoint = new UserPoint(userId, currentPoint, System.currentTimeMillis());

        // when
        when(userPointRepository.findById(eq(userId))).thenReturn(userPoint); // Mock을 통해 포인트 정보 반환
        UserPoint result = pointService.getUserPoint(userId);

        // then (포인트 제대로 반환 되었는지 검증)
        assertThat(result.point()).isEqualTo(currentPoint);

        verify(userPointRepository).findById(eq(userId));
        verify(userPointRepository, times(1)).findById(eq(userId));
    }

    @Test
    void 포인트내역_조회_성공케이스() {
        // given
        long cursor = 1;
        long userId = 123L;
        long chargingPoint = 5000L;
        long usePoint = 1000L;
        List<PointHistory> pointHistories = new ArrayList<>();
        PointHistory chargingPointHistory = new PointHistory(cursor++, userId, chargingPoint, TransactionType.CHARGE, System.currentTimeMillis());
        PointHistory usingPointHistory = new PointHistory(cursor, userId, usePoint, TransactionType.USE, System.currentTimeMillis());
        pointHistories.add(chargingPointHistory);
        pointHistories.add(usingPointHistory);

        // when
        when(pointHistoryRepository.findAllById(eq(userId))).thenReturn(pointHistories);
        List<PointHistory> result = pointService.getUserPointHistories(userId);

        // then (포인트내역 제대로 반환 되었는지 검증)
        assertThat(result.get(0)).isEqualTo(chargingPointHistory);
        assertThat(result.get(1)).isEqualTo(usingPointHistory);

        // verify 추가: pointHistoryRepository.findAllById가 userId를 가지고 한 번 호출되었는지 확인
        verify(pointHistoryRepository).findAllById(eq(userId));
        verify(pointHistoryRepository, times(1)).findAllById(eq(userId));
    }

    @Test
    void 포인트내역_조회_실패케이스_사용내역존재하지않음() {
        long userId = 123L;

        // given
        when(pointHistoryRepository.findAllById(eq(userId))).thenReturn(List.of()); // 비어 있는 리스트 반환

        // when
        List<PointHistory> result = pointService.getUserPointHistories(userId);

        // then
        assertThat(result).isEmpty(); // 결과가 비어 있어야 함
        verify(pointHistoryRepository, times(1)).findAllById(eq(userId)); // 호출 검증
    }
}
