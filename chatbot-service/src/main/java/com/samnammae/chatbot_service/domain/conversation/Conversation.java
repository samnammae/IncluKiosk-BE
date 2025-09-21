package com.samnammae.chatbot_service.domain.conversation;

import com.samnammae.chatbot_service.domain.message.Message;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Document(collection = "conversations")
public class Conversation {

    @Id
    private String id;

    @Indexed(unique = true)
    private String sessionId;

    private List<Message> messages = new ArrayList<>();

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // sessionId만으로 새 대화를 시작할 수 있도록 생성자 추가
    public Conversation(String sessionId) {
        this.sessionId = sessionId;
    }

    // 대화 기록을 추가하는 편의 메소드
    public void addMessage(Message message) {
        this.messages.add(message);
    }
}
