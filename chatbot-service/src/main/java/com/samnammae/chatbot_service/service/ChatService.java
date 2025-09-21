package com.samnammae.chatbot_service.service;

import com.samnammae.chatbot_service.domain.conversation.Conversation;
import com.samnammae.chatbot_service.domain.conversation.ConversationRepository;
import com.samnammae.chatbot_service.domain.message.Message;
import com.samnammae.chatbot_service.dto.response.ChatResponse;
import com.samnammae.common.exception.CustomException;
import com.samnammae.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ConversationRepository conversationRepository;

    // 매장 권한 검증 메서드
    public void validateStoreAccess(Long storeId, String managedStoreIds) {
        List<Long> accessibleStoreIds = Arrays.stream(managedStoreIds.split(","))
                .map(Long::parseLong)
                .toList();

        if (!accessibleStoreIds.contains(storeId)) {
            throw new CustomException(ErrorCode.STORE_ACCESS_DENIED);
        }
    }

    @Transactional
    public ChatResponse processChat(String sessionId, String userMessage) {

        // 1. sessionId로 기존 대화 조회 또는 새로 생성
        Conversation conversation = conversationRepository.findBySessionId(sessionId)
                .orElseGet(() -> new Conversation(sessionId));

        // 2. 사용자의 메시지를 대화 기록에 추가
        conversation.addMessage(Message.of("USER", userMessage));

        // 3. (핵심) 임시 AI 고정 응답 생성 및 기록에 추가
        String aiResponseContent = "요청을 확인했습니다. AI 연동은 다음 이슈에서 진행될 예정입니다.";
        conversation.addMessage(Message.of("AI", aiResponseContent));

        // 4. 변경된 대화 내용을 MongoDB에 저장 (update or insert)
        conversationRepository.save(conversation);

        // 5. API 명세에 맞는 최종 응답 생성
        return new ChatResponse(conversation.getSessionId(), aiResponseContent);
    }
}