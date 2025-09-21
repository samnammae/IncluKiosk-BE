package com.samnammae.chatbot_service.service;

import com.samnammae.chatbot_service.domain.conversation.Conversation;
import com.samnammae.chatbot_service.domain.conversation.ConversationRepository;
import com.samnammae.chatbot_service.dto.response.ChatResponse;
import com.samnammae.common.exception.CustomException;
import com.samnammae.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ConversationRepository conversationRepository;

    @InjectMocks
    private ChatService chatService;

    @Test
    @DisplayName("매장 권한 검증 - 권한이 있는 경우")
    void validateStoreAccess_Success() {
        // given
        Long storeId = 1L;
        String managedStoreIds = "1,2,3";

        // when & then
        assertDoesNotThrow(() -> chatService.validateStoreAccess(storeId, managedStoreIds));
    }

    @Test
    @DisplayName("매장 권한 검증 - 권한이 없는 경우")
    void validateStoreAccess_Failure() {
        // given
        Long storeId = 4L;
        String managedStoreIds = "1,2,3";

        // when & then
        CustomException exception = assertThrows(
                CustomException.class,
                () -> chatService.validateStoreAccess(storeId, managedStoreIds)
        );
        assertEquals(ErrorCode.STORE_ACCESS_DENIED, exception.getErrorCode());
    }

    @Test
    @DisplayName("채팅 처리 - 기존 대화가 있는 경우")
    void processChat_ExistingConversation() {
        // given
        String sessionId = "test-session";
        String userMessage = "안녕하세요";
        Conversation existingConversation = new Conversation(sessionId);

        when(conversationRepository.findBySessionId(sessionId))
                .thenReturn(Optional.of(existingConversation));
        when(conversationRepository.save(any(Conversation.class)))
                .thenReturn(existingConversation);

        // when
        ChatResponse response = chatService.processChat(sessionId, userMessage);

        // then
        assertNotNull(response);
        assertEquals(sessionId, response.getSessionId());
        assertEquals("요청을 확인했습니다. AI 연동은 다음 이슈에서 진행될 예정입니다.", response.getAiMessage());
        verify(conversationRepository).findBySessionId(sessionId);
        verify(conversationRepository).save(any(Conversation.class));
    }

    @Test
    @DisplayName("채팅 처리 - 새로운 대화인 경우")
    void processChat_NewConversation() {
        // given
        String sessionId = "new-session";
        String userMessage = "처음 질문입니다";

        when(conversationRepository.findBySessionId(sessionId))
                .thenReturn(Optional.empty());
        when(conversationRepository.save(any(Conversation.class)))
                .thenReturn(new Conversation(sessionId));

        // when
        ChatResponse response = chatService.processChat(sessionId, userMessage);

        // then
        assertNotNull(response);
        assertEquals(sessionId, response.getSessionId());
        assertEquals("요청을 확인했습니다. AI 연동은 다음 이슈에서 진행될 예정입니다.", response.getAiMessage());
        verify(conversationRepository).findBySessionId(sessionId);
        verify(conversationRepository).save(any(Conversation.class));
    }
}