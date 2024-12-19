package io.hhplus.tdd.point.repository;

import io.hhplus.tdd.point.UserPoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class UserPointRepositoryTest {

    @Autowired
    private UserPointRepository userPointRepository;

    @Test
    @DisplayName("최초 유저의 포인트가 잘 저장되었는지 확인")
    void save() {
        // given
        long userId = 123;
        long amount = 5000;

        // when
        UserPoint userPoint =  userPointRepository.saveOrUpdate(userId, amount);

        // then
        assertThat(userPoint.id()).isEqualTo(userId);
        assertThat(userPoint.point()).isEqualTo(amount);
    }
}