package io.hhplus.tdd.point;

import io.hhplus.tdd.point.service.PointService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * 기능: **@WebMvcTest**는 웹 애플리케이션의 MVC 계층을 테스트하기 위해 사용됩니다. 주로 웹 컨트롤러(Controller)와 관련된 빈들만을 로드하고 테스트 환경을 설정합니다.
 * 출처: https://ybe-teamcook7.hashnode.dev/tdd
 */

@WebMvcTest(PointController.class)
class PointControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PointService pointService;

    @BeforeEach
    public void beforeEach() {}

    @Test
    @DisplayName("특정 유저의 포인트 조회할 수 있다.")
    void getUserPoint_shouldReturnUserPoint() throws Exception {
        // given (임의로 유저 저장)
        long userId = 123L;
        UserPoint userPoint = new UserPoint(userId, 5000L, System.currentTimeMillis());
        when(pointService.getUserPoint(userId)).thenReturn(userPoint);

        // when & then
        mockMvc.perform(get("/point/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is((int) userId)))
                .andExpect(jsonPath("$.point", is(5000)));
    }

    @Test
    @DisplayName("포인트 조회할 유저 아이디가 long이 아닌 경우 조회에 실패한다")
    void failToReturnPointIfUserIdIsNotNumber() throws Exception {
        mockMvc.perform(get("/point/{id}", "STRING")) // 검증할 uri 호출 (포인트 조회)
                .andExpect(status().isBadRequest()) // 결과가 400 BAD REQUEST 인지 검증
                .andExpect(jsonPath("$.message").value(containsString("잘못된 요청 값입니다.")));
    }

    @Test
    @DisplayName("특정 유저의 히스토리를 조회할 수 있다.")
    void getUserPointHistories_shouldReturnPointHistories() throws Exception {
        // given (임의로 히스토리 저장)
        long userId = 123L;
        PointHistory history1 = new PointHistory(1L, userId, 5000L, TransactionType.CHARGE, System.currentTimeMillis());
        PointHistory history2 = new PointHistory(2L, userId, 1000L, TransactionType.USE, System.currentTimeMillis());
        List<PointHistory> histories = List.of(history1, history2);
        when(pointService.getUserPointHistories(userId)).thenReturn(histories);

        // when & then
        mockMvc.perform(get("/point/{id}/histories", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].userId", is((int) userId)))
                .andExpect(jsonPath("$[0].amount", is(5000)))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].amount", is(1000)));
    }

    @Test
    @DisplayName("유저의 포인트 충전 요청")
    void chargeUserPoint_shouldReturnUpdatedUserPoint() throws Exception {
        // given
        long userId = 123L;
        long amount = 10_000L;

        // 예상하는 충전 후 유저 포인트 객체 생성
        UserPoint userPoint = new UserPoint(userId, amount, 0);
        when(pointService.chargeUserPoint(userId, amount)).thenReturn(userPoint);

        // when & then
        mockMvc.perform(patch("/point/{id}/charge", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(amount)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is((int) userId)))
                .andExpect(jsonPath("$.point", is(10000)));
    }

    @Test
    @DisplayName("유저의 포인트 사용 요청")
    void useUserPoint_shouldReturnUpdatedUserPoint() throws Exception {
        // given
        long userId = 123L;
        long amount = 1000L;
        UserPoint updatedPoint = new UserPoint(userId, 4000L, System.currentTimeMillis());
        when(pointService.useUserPoint(userId, amount)).thenReturn(updatedPoint);

        // when & then
        mockMvc.perform(patch("/point/{id}/use", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(amount)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is((int) userId)))
                .andExpect(jsonPath("$.point", is(4000)));
    }
}