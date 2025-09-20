package com.samnammae.chatbot_service.domain.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    private String role;
    private String content;
    private LocalDateTime timestamp;

    // 정적 팩토리 메소드
    public static Message of(String role, String content) {
        return new Message(role, content, LocalDateTime.now());
    }
}