package com.samnammae.chatbot_service.domain.conversation;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ConversationRepository extends MongoRepository<Conversation, String> {
    Optional<Conversation> findBySessionId(String sessionId);
}