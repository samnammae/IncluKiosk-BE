package com.samnammae.chatbot_service.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) // 응답의 모든 필드를 매핑하지 않을 것이므로 이 어노테이션 추가
public class GeminiResponse {

    private List<Candidate> candidates;

    /**
     * 복잡한 응답 객체에서 실제 텍스트 답변만 쉽게 추출하는 편의 메소드
     * @return AI가 생성한 텍스트 응답
     */
    public String extractText() {
        try {
            if (candidates != null && !candidates.isEmpty()) {
                return candidates.get(0).getContent().getParts().get(0).getText();
            }
        } catch (Exception e) {
            // 로깅을 추가하면 더 좋습니다. e.g., log.error("Failed to parse Gemini response", e);
        }
        return "죄송합니다. 답변을 생성하는 데 문제가 발생했습니다.";
    }

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Candidate {
        private Content content;
    }

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Content {
        private List<Part> parts;
    }

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Part {
        private String text;
    }
}