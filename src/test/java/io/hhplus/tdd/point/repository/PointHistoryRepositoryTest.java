package io.hhplus.tdd.point.repository;

import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.TransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PointHistoryRepositoryTest {

    @Autowired
    private PointHistoryRepository pointHistoryRepository;

    @Test
    @DisplayName("최초 유저의 포인트내역이 잘 저장되었는지 확인")
    void save() {
        // given
        long userId = 123;
        long amount = 5000;

        // when
        pointHistoryRepository.save(userId, amount, TransactionType.CHARGE, System.currentTimeMillis());
        List<PointHistory> pointHistories = pointHistoryRepository.findAllById(userId);

        // then
        for (PointHistory pointHistory: pointHistories) {
            assertThat(pointHistory.userId()).isEqualTo(userId);
            assertThat(pointHistory.amount()).isEqualTo(amount);
            assertThat(pointHistory.type()).isEqualTo(TransactionType.CHARGE);
        }
    }
}