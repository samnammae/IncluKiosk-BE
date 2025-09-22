package com.samnammae.chatbot_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.samnammae.chatbot_service.client.GeminiClient;
import com.samnammae.chatbot_service.client.OrderServiceClient;
import com.samnammae.chatbot_service.domain.conversation.Conversation;
import com.samnammae.chatbot_service.domain.conversation.ConversationRepository;
import com.samnammae.chatbot_service.domain.message.Message;
import com.samnammae.chatbot_service.dto.request.GeminiRequest;
import com.samnammae.chatbot_service.dto.request.OrderRequestDto;
import com.samnammae.chatbot_service.dto.response.ChatResponse;
import com.samnammae.chatbot_service.dto.response.GeminiResponse;
import com.samnammae.common.exception.CustomException;
import com.samnammae.common.exception.ErrorCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final GeminiPromptService geminiPromptService;
    private final GeminiClient geminiClient;
    private final OrderServiceClient orderServiceClient;
    private final ObjectMapper objectMapper;

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
    public ChatResponse processChat(Long storeId, String sessionId, String userMessage, String managedStoreIds) {
        log.info("Processing chat for storeId: {}, sessionId: {}, userMessage: {}", storeId, sessionId, userMessage);

        // 1. 대화 기록 조회 또는 생성
        Conversation conversation = conversationRepository.findBySessionId(sessionId)
                .orElseGet(() -> new Conversation(sessionId));

        // 2. 현재 사용자 메시지를 대화 기록에 추가
        conversation.addMessage(Message.of("USER", userMessage));

        // 3. Gemini에 보낼 프롬프트 생성
        String prompt = geminiPromptService.createPrompt(storeId, conversation, managedStoreIds);
        log.debug("Generated prompt: {}", prompt);

        // 4. Gemini API 호출하여 AI의 원본 응답 받기
        GeminiResponse geminiResponse = geminiClient.call(new GeminiRequest(prompt));
        String aiRawResponse = geminiResponse.extractText();
        log.info("Gemini raw response: {}", aiRawResponse);

        // 5. AI 응답 분석 후 최종 메시지 결정
        String finalAiMessage;
        Optional<OrderRequestDto> orderRequestOpt = parseOrderAction(aiRawResponse);

        if (orderRequestOpt.isPresent()) {
            log.info("Order action detected, processing order...");
            // 5-1. 주문 요청인 경우: Order Service 호출
            OrderRequestDto orderRequest = orderRequestOpt.get();
            log.info("Order request details: {}", orderRequest);

            try {
                var orderApiResponse = orderServiceClient.placeOrder(orderRequest);
                log.info("Order service response: {}", orderApiResponse);
                finalAiMessage = "주문이 완료되었습니다. 주문번호는 " + orderApiResponse.getData().getOrderNumber() + "입니다.";
            } catch (Exception e) {
                log.error("Failed to place order", e);
                finalAiMessage = "주문 처리 중 오류가 발생했습니다. 다시 시도해 주세요.";
            }
        } else {
            log.info("Regular conversation, using Gemini response as is");
            // 5-2. 일반 대화인 경우: Gemini 응답 그대로 사용
            finalAiMessage = aiRawResponse;
        }

        // 6. 최종 AI 응답을 대화 기록에 저장
        conversation.addMessage(Message.of("AI", finalAiMessage));
        conversationRepository.save(conversation);

        log.info("Final AI message: {}", finalAiMessage);
        // 7. 클라이언트에 전달할 최종 응답 생성
        return new ChatResponse(conversation.getSessionId(), finalAiMessage);
    }

    // Gemini가 반환한 텍스트가 주문을 위한 JSON 액션인지 파싱하는 헬퍼 메소드
    private Optional<OrderRequestDto> parseOrderAction(String textResponse) {
        log.debug("Parsing order action from response: {}", textResponse);

        // markdown 코드 블록 제거
        String cleanedResponse = textResponse
                .replaceAll("```json\\s*", "")  // ```json 제거
                .replaceAll("```\\s*", "")      // ``` 제거
                .trim();

        log.debug("Cleaned response for JSON parsing: {}", cleanedResponse);

        try {
            GeminiOrderActionDto actionDto = objectMapper.readValue(cleanedResponse, GeminiOrderActionDto.class);
            log.debug("Parsed action DTO: action={}, order_details={}", actionDto.getAction(), actionDto.getOrder_details());

            if ("PLACE_ORDER".equals(actionDto.getAction()) && actionDto.getOrder_details() != null) {
                log.info("Valid order action found");
                return Optional.of(actionDto.getOrder_details());
            } else {
                log.debug("Not a valid order action: action={}", actionDto.getAction());
            }
        } catch (JsonProcessingException e) {
            log.debug("Failed to parse as JSON order action, treating as regular text: {}", e.getMessage());
        }
        return Optional.empty();
    }

    // Gemini의 주문 액션 응답을 파싱하기 위한 내부 DTO
    @Getter
    @NoArgsConstructor
    private static class GeminiOrderActionDto {
        private String action;
        private OrderRequestDto order_details;
    }
}