package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.UserPoint;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.UserPointRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
}
