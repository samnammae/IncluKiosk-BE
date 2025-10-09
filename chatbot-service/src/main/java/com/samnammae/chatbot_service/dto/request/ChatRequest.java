package com.samnammae.chatbot_service.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChatRequest {
    private String sessionId;
    private String message;
    private Long storeId;
    private String storeName;
}