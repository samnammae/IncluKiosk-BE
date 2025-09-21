package com.samnammae.chatbot_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.samnammae.chatbot_service.dto.response.ChatResponse;
import com.samnammae.chatbot_service.exception.GlobalExceptionHandler;
import com.samnammae.chatbot_service.service.ChatService;
import com.samnammae.common.exception.CustomException;
import com.samnammae.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ChatControllerTest {

    @Mock
    private ChatService chatService;

    @InjectMocks
    private ChatController chatController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(chatController)
                .setControllerAdvice(new GlobalExceptionHandler()) // 예외 핸들러 추가
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("채팅 처리 성공")
    void handleChat_Success() throws Exception {
        // given
        Long storeId = 1L;
        String managedStoreIds = "1,2,3";

        String requestBody = """
            {
                "sessionId": "test-session",
                "message": "안녕하세요"
            }
            """;

        ChatResponse expectedResponse = new ChatResponse("test-session", "요청을 확인했습니다. AI 연동은 다음 이슈에서 진행될 예정입니다.");

        doNothing().when(chatService).validateStoreAccess(storeId, managedStoreIds);
        given(chatService.processChat("test-session", "안녕하세요"))
                .willReturn(expectedResponse);

        // when & then
        mockMvc.perform(post("/api/chat/api/chatbot/{storeId}/chat", storeId)
                        .header("X-MANAGED-STORE-IDS", managedStoreIds)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sessionId").value("test-session"))
                .andExpect(jsonPath("$.data.aiMessage").value("요청을 확인했습니다. AI 연동은 다음 이슈에서 진행될 예정입니다."));
    }

    @Test
    @DisplayName("매장 권한 없음으로 실패")
    void handleChat_StoreAccessDenied() throws Exception {
        // given
        Long storeId = 4L;
        String managedStoreIds = "1,2,3";

        String requestBody = """
            {
                "sessionId": "test-session",
                "message": "안녕하세요"
            }
            """;

        doThrow(new CustomException(ErrorCode.STORE_ACCESS_DENIED))
                .when(chatService).validateStoreAccess(storeId, managedStoreIds);

        // when & then
        mockMvc.perform(post("/api/chat/api/chatbot/{storeId}/chat", storeId)
                        .header("X-MANAGED-STORE-IDS", managedStoreIds)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("채팅 처리 성공 - 다른 세션")
    void handleChat_DifferentSession() throws Exception {
        // given
        Long storeId = 2L;
        String managedStoreIds = "1,2,3";

        String requestBody = """
            {
                "sessionId": "session-456",
                "message": "메뉴 추천해주세요"
            }
            """;

        ChatResponse expectedResponse = new ChatResponse("session-456", "요청을 확인했습니다. AI 연동은 다음 이슈에서 진행될 예정입니다.");

        doNothing().when(chatService).validateStoreAccess(storeId, managedStoreIds);
        given(chatService.processChat("session-456", "메뉴 추천해주세요"))
                .willReturn(expectedResponse);

        // when & then
        mockMvc.perform(post("/api/chat/api/chatbot/{storeId}/chat", storeId)
                        .header("X-MANAGED-STORE-IDS", managedStoreIds)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sessionId").value("session-456"));
    }

    @Test
    @DisplayName("잘못된 요청 형식")
    void handleChat_InvalidRequestFormat() throws Exception {
        // given
        Long storeId = 1L;
        String managedStoreIds = "1,2,3";

        String invalidRequestBody = """
            {
                "sessionId": "",
                "message": ""
            }
            """;

        doNothing().when(chatService).validateStoreAccess(storeId, managedStoreIds);

        // when & then
        mockMvc.perform(post("/api/chat/api/chatbot/{storeId}/chat", storeId)
                        .header("X-MANAGED-STORE-IDS", managedStoreIds)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestBody))
                .andDo(print())
                .andExpect(status().isOk()); // 현재 서비스에서는 빈 값도 처리
    }
}