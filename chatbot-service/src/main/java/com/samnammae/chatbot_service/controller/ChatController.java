package com.samnammae.chatbot_service.controller;


import com.samnammae.chatbot_service.dto.request.ChatRequest;
import com.samnammae.chatbot_service.dto.response.ChatResponse;
import com.samnammae.chatbot_service.service.ChatService;
import com.samnammae.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@Tag(name = "Chatbot", description = "챗봇 관련 API")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/api/chatbot/{storeId}/chat")
    public ApiResponse<ChatResponse> handleChat(
            @PathVariable Long storeId,
            @RequestBody ChatRequest request,
            @RequestHeader("X-MANAGED-STORE-IDS") String managedStoreIds) {

        //  매장 권한 검증
        chatService.validateStoreAccess(storeId, managedStoreIds);

        ChatResponse response = chatService.processChat(request.getSessionId(), request.getMessage());

        return ApiResponse.success(response);
    }
}
