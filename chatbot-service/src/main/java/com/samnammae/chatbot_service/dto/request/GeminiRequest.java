package com.samnammae.chatbot_service.dto.request;

import lombok.Getter;

import java.util.Collections;
import java.util.List;

@Getter
public class GeminiRequest {

    private final List<Content> contents;

    // 프롬프트 문자열 하나만으로 간단히 요청 객체를 생성하는 생성자
    public GeminiRequest(String prompt) {
        this.contents = Collections.singletonList(new Content(Collections.singletonList(new Part(prompt))));
    }

    @Getter
    private static class Content {
        private final List<Part> parts;

        public Content(List<Part> parts) {
            this.parts = parts;
        }
    }

    @Getter
    private static class Part {
        private final String text;

        public Part(String text) {
            this.text = text;
        }
    }
}