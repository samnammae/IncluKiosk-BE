package com.samnammae.chatbot_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatResponse {
    private String sessionId;
    private String aiMessage;
}
