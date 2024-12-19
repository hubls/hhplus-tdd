package io.hhplus.tdd;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;


@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureMockMvc
class TddApplicationTest { // E2E 테스트
    @Autowired
    private MockMvc mockMvc;

    private static final long USER_ID = 123L;
    private static final long AMOUNT = 1000L;

    @Test
    @DisplayName("유저 포인트 충전 요청 시 성공적으로 처리된다.")
    void chargeUserPointSuccessfully() throws Exception {
        mockMvc.perform(patch("/point/{id}/charge", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(AMOUNT)))
                .andDo(print()) // 응답 본문 출력
                .andExpect(MockMvcResultMatchers.status().isOk()) // 응답 상태가 성공인지 확인
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(USER_ID)) // 예상한 ID가 맞는지 확인
                .andExpect(MockMvcResultMatchers.jsonPath("$.point").value(AMOUNT)); // 예상한 포인트가 맞는지 확인
    }

    @Test
    @DisplayName("유저 포인트 사용 요청 시 성공적으로 처리된다.")
    void useUserPointsSuccessfully() throws Exception {
        // 충전 후 사용 테스트
        mockMvc.perform(patch("/point/{id}/charge", USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.valueOf(AMOUNT)));

        mockMvc.perform(patch("/point/{id}/use", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(50L)))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk()) // 결과가 성공인지 검증
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(USER_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.point").value(950L));
    }

    @Test
    @DisplayName("유저의 포인트 충전/사용 내역을 조회할 수 있다.")
    void returnPointHistory() throws Exception {
        mockMvc.perform(patch("/point/{id}/charge", USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.valueOf(AMOUNT)));

        mockMvc.perform(patch("/point/{id}/use", USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.valueOf(AMOUNT)));

        mockMvc.perform(patch("/point/{id}/charge", USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.valueOf(AMOUNT)));

        mockMvc.perform(patch("/point/{id}/use", USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.valueOf(AMOUNT)));

        mockMvc.perform(get("/point/{id}/histories", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print()) // 응답 본문 출력
                .andExpect(MockMvcResultMatchers.status().isOk()) // 응답 상태가 성공인지 확인
                .andExpect(MockMvcResultMatchers.jsonPath("$", hasSize(4))) // 응답 내역이 4개인지 확인
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].type").value("CHARGE")) // 첫 번째 항목이 충전인지 확인
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].amount").value(AMOUNT)) // 첫 번째 항목의 금액이 예상과 일치하는지 확인
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].type").value("USE")) // 두 번째 항목이 사용인지 확인
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].amount").value(AMOUNT)) // 두 번째 항목의 금액이 예상과 일치하는지 확인
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].type").value("CHARGE")) // 세 번째 항목이 충전인지 확인
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].amount").value(AMOUNT)) // 세 번째 항목의 금액이 예상과 일치하는지 확인
                .andExpect(MockMvcResultMatchers.jsonPath("$[3].type").value("USE")) // 네 번째 항목이 사용인지 확인
                .andExpect(MockMvcResultMatchers.jsonPath("$[3].amount").value(AMOUNT)); // 네 번째 항목의 금액이 예상과 일치하는지 확인
    }

    @Test
    @DisplayName("조회 가능한 유저의 포인트를 조회한다.")
    void returnUserPointViewableUsePoint() throws Exception {
        mockMvc.perform(get("/point/{id}", USER_ID)) // 확인할 URI 호출
                .andDo(print()) // 응답 본문 출력
                .andExpect(MockMvcResultMatchers.status().isOk()) // 응답 상태가 성공인지 확인
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(USER_ID)) // 예상한 ID가 맞는지 확인
                .andExpect(MockMvcResultMatchers.jsonPath("$.point").value(0L)); // 예상한 포인트가 0인지 확인
    }

}