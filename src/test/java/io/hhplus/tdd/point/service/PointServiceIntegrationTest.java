package io.hhplus.tdd.point.service;

import io.hhplus.tdd.TddApplication;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.UserPointRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = TddApplication.class)
@ExtendWith(SpringExtension.class)
public class PointServiceIntegrationTest {
    @Autowired
    PointService pointService;

    @Autowired
    UserPointRepository userPointRepository;

    @Autowired
    PointHistoryRepository pointHistoryRepository;

    @BeforeEach
    public void beforeEach() {
    }

    @Test
    public void 포인트충전_성공케이스() {
        // given
        long userId = 123L;
        long currentPoint = 1000L;
        long chargePoint = 500L;

        // 미리 유저 포인트를 저장
        userPointRepository.saveOrUpdate(userId, currentPoint);

        // when (chargePoint 충전)
        UserPoint userPoint = pointService.chargeUserPoint(userId, chargePoint);

        // then
        // 반환된 userPoint 확인
        assertEquals(currentPoint + chargePoint, userPoint.point());

        // 실제 DB에 저장된 포인트 확인
        UserPoint resultUserPoint = userPointRepository.findById(userId);
        assertEquals(currentPoint + chargePoint, resultUserPoint.point());
    }

    @Test
    public void 충전금액이_0_이하일때_예외케이스() {
        // given
        long userId = 123L;
        long currentPoint = 1000L;
        long chargePoint = 0L; // 유효하지 않은 충전 금액

        // 미리 유저 포인트 저장
        userPointRepository.saveOrUpdate(userId, currentPoint);

        // when
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            pointService.chargeUserPoint(userId, chargePoint);
        });

        // then
        assertThat(exception.getMessage()).contains("충전할 포인트가 0보다 작습니다.");
    }

    @Test
    public void 충전가능금액_초과시_예외케이스() {
        // given
        long userId = 123L;
        long currentPoint = 1000L;
        long chargePoint = 1_000_000L; // 최대 중전 금액

        // 미리 유저 포인트 저장
        userPointRepository.saveOrUpdate(userId, currentPoint);

        // when
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            pointService.chargeUserPoint(userId, chargePoint);
        });

        // then
        assertThat(exception.getMessage()).contains("충전가능한 최대 포인트");
    }

    @Test
    public void 포인트사용_성공케이스() {
        // given
        long userId = 123L;
        long currentPoint = 1000L;
        long usingPoint = 300L;

        // 미리 유저 포인트 저장
        userPointRepository.saveOrUpdate(userId, currentPoint);

        // when (usingPoint 사용)
        UserPoint userPoint = pointService.useUserPoint(userId, usingPoint);

        // 반환된 userPoint 확인
        assertEquals(currentPoint - usingPoint, userPoint.point());

        // 실제 DB에 저장된 포인트 확인
        UserPoint resultUserPoint = userPointRepository.findById(userId);
        assertEquals(currentPoint - usingPoint, resultUserPoint.point());
    }

    @Test
    public void 사용금액이_0_이하일때_예외케이스() {
        // given
        long userId = 123L;
        long currentPoint = 1000L;
        long usingPoint = 0L; // 유효하지 않은 충전 금액

        // 미리 유저 포인트 저장
        userPointRepository.saveOrUpdate(userId, currentPoint);

        // when
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            pointService.useUserPoint(userId, usingPoint);
        });

        // then
        assertThat(exception.getMessage()).contains("사용할 포인트가 0보다 커야합니다.");
    }

    @Test
    public void 사용금액이_현재금액을_초과할때_예외케이스() {
        // given
        long userId = 123L;
        long currentPoint = 1000L;
        long usingPoint = 2000L; // 유효하지 않은 충전 금액

        // 미리 유저 포인트 저장
        userPointRepository.saveOrUpdate(userId, currentPoint);

        // when
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            pointService.useUserPoint(userId, usingPoint);
        });

        // then
        assertThat(exception.getMessage()).contains("잔고부족: 보유한 포인트");
    }

    @Test
    void 포인트조회_성공케이스() {
        // given
        long userId = 123L;
        long currentPoint = 1000L;

        // 미리 유저 포인트 저장
        userPointRepository.saveOrUpdate(userId, currentPoint);

        // when(유저 포인트를 조회했을 때)
        UserPoint result = pointService.getUserPoint(userId);

        // then (포인트 제대로 반환 되었는지 검증)
        assertThat(result.point()).isEqualTo(currentPoint);
    }

    @Test
    void 포인트내역_조회_성공케이스() {
        // given
        long userId = 123L;
        long chargingPoint = 5000L;
        long usingPoint = 1000L;
        pointHistoryRepository.save(userId, chargingPoint, TransactionType.CHARGE, System.currentTimeMillis());
        pointHistoryRepository.save(userId, usingPoint, TransactionType.USE, System.currentTimeMillis());

        // when
        List<PointHistory> result = pointService.getUserPointHistories(userId);

        // then (포인트내역 제대로 반환 되었는지 검증)
        assertThat(result.get(0).userId()).isEqualTo(userId);
        assertThat(result.get(0).amount()).isEqualTo(chargingPoint);
        assertThat(result.get(0).type()).isEqualTo(TransactionType.CHARGE);

        assertThat(result.get(1).userId()).isEqualTo(userId);
        assertThat(result.get(1).amount()).isEqualTo(usingPoint);
        assertThat(result.get(1).type()).isEqualTo(TransactionType.USE);
    }

    @Test
    void 포인트내역_조회_실패케이스_사용내역존재하지않음() {
        // given (저장된적 없는 user)
        long userId = 456L;

        // when
        List<PointHistory> result = pointService.getUserPointHistories(userId);

        // then
        assertThat(result).isEqualTo(List.of());
    }
}
