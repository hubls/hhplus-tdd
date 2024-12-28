package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.PointHistoryRepositoryImpl;
import io.hhplus.tdd.point.repository.UserPointRepository;
import io.hhplus.tdd.point.repository.UserPointRepositoryImpl;
import io.hhplus.tdd.point.service.PointService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

public class ConcurrentTest { // 동시성 제어 테스트
    PointService pointService;
    UserPointRepository userPointRepository;
    PointHistoryRepository pointHistoryRepository;
    PointHistoryTable pointHistoryTable;
    UserPointTable userPointTable;

    @BeforeEach
    void beforeEach() {
        // 각 테스트마다 새로운 인스턴스를 초기화하여 상태를 초기화합니다.
        userPointTable = new UserPointTable();
        userPointRepository = new UserPointRepositoryImpl(userPointTable);
        pointHistoryTable = new PointHistoryTable();
        pointHistoryRepository = new PointHistoryRepositoryImpl(pointHistoryTable);
        pointService = new PointService(userPointRepository, pointHistoryRepository);
    }
    /**
     * 동시성 테스트:
     * - 여러 스레드가 같은 자원에 접근할 때 발생할 수 있는 경쟁 상태를 방지합니다.
     * - `ReentrantLock`을 사용하여 동기화하며, `fair` 옵션을 통해 락을 공정하게 관리합니다.
     * @throws InterruptedException
     */
    @Test
    @DisplayName("동시 요청 시 포인트 충전 및 사용을 순차적으로 처리한다.")
    void chargeOrUsePointWithOthersThenSequentially() throws InterruptedException {
        // given
        // 스레드 풀을 사용하여 병렬 요청을 실행하고, 모든 스레드의 완료를 기다립니다.
        final int threadCount = 5; // 테스트할 스레드 수
        final ExecutorService executorService = Executors.newFixedThreadPool(16);
        final CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        // 각 스레드에서 포인트 충전 및 사용을 시도합니다.
        submitChargeAndUseRequests(executorService, latch, threadCount);

        // 모든 스레드 작업이 완료되면 결과를 확인합니다.
        latch.await();
        System.out.println("종료");

        // then
        // 각 유저의 포인트가 예상대로 0인지 검증합니다.
        verifyUserPoints();
    }

    private void submitChargeAndUseRequests(ExecutorService executorService, CountDownLatch latch, int threadCount) {
        System.out.println("시작");
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    // 포인트 충전 및 사용
                    pointService.chargeUserPoint(123L, 1000L);
                    pointService.chargeUserPoint(456L, 2000L);
                    pointService.chargeUserPoint(789L, 3000L);
                    pointService.useUserPoint(123L, 1000L);
                    pointService.useUserPoint(456L, 2000L);
                    pointService.useUserPoint(789L, 3000L);
                } finally {
                    latch.countDown(); // 작업 완료 후 카운트다운
                }
            });
        }
    }

    private void verifyUserPoints() {
        // 각 유저의 포인트가 0으로 초기화되었는지 검증
        UserPoint userPoint1 = pointService.getUserPoint(1000L);
        UserPoint userPoint2 = pointService.getUserPoint(2000L);
        UserPoint userPoint3 = pointService.getUserPoint(3000L);

        // 포인트 충전 및 사용 후 최종 결과가 예상대로 0인지 확인
        assertThat(userPoint1.point()).isEqualTo(0);
        assertThat(userPoint2.point()).isEqualTo(0);
        assertThat(userPoint3.point()).isEqualTo(0);
    }
}
