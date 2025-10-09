package com.samnammae.chatbot_service.service;

import com.samnammae.chatbot_service.client.MenuServiceClient;
import com.samnammae.chatbot_service.domain.conversation.Conversation;
import com.samnammae.chatbot_service.domain.message.Message;
import com.samnammae.chatbot_service.dto.response.MenuWithOptionsResponseDto;
import com.samnammae.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiPromptService {

    private final MenuServiceClient menuServiceClient;

    public String createPrompt(Long storeId, Conversation conversation, String managedStoreIds) {
        log.info("Creating prompt for storeId: {}", storeId);

        String systemPrompt = createSystemPrompt();
        String menuData = fetchAndFormatMenuData(storeId);
        String history = formatHistory(conversation.getMessages());

        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append(systemPrompt);
        promptBuilder.append("\n\n# MENU DATA\n");
        promptBuilder.append(menuData);
        promptBuilder.append("\n\n# CONVERSATION HISTORY & CURRENT QUESTION\n");
        promptBuilder.append(history);

        log.debug("Generated prompt length: {}", promptBuilder.length());
        return promptBuilder.toString();
    }

    private String createSystemPrompt() {
        return """
        # ROLE & VERY IMPORTANT RULES
        ## Persona and Goal
        You are 'inclukiosk', a friendly and helpful voice-based kiosk assistant designed primarily for visually impaired users. Your primary goal is to help users build and confirm an order verbally, based on the provided `MENU DATA`.
        
        ## Rules for Voice (TTS) Responses
        
        All of your conversational responses are converted to speech by a TTS system. You MUST follow these rules for all conversational replies:
        
        1.  **No Markdown:** NEVER use any Markdown formatting (like `*`, `-`, `#`, etc.). All responses must be plain text.
        2.  **Natural Language:** Your responses must be complete, natural-sounding sentences that are easy to understand when heard.
        3.  **Conciseness:** Keep responses clear and to the point. For broad questions like "메뉴 전체 알려줘", do not list every single item. Instead, summarize by listing the menu categories. For example, respond with "네, 저희 매장에는 커피, 디저트, 그리고 디카페인 음료 카테고리가 있습니다. 어떤 종류를 안내해 드릴까요?"
        
        ## Rules for Order Accuracy
        
        1.  **Check Required Options:** Before finalizing an order, you MUST check every item in the user's request.
        2.  **Ask if Missing:** If an item has an option category marked with `"required": true"` in the `MENU DATA` that the user has not yet selected, you MUST ask the user to choose that option. For example, ask, "아메리카노의 사이즈는 어떻게 해드릴까요?"
        3.  **Ask Order Type:** After all menu items and required options are confirmed, you MUST ask about order type: "매장에서 드시겠어요, 아니면 포장해 드릴까요?"
        4.  **Ask Payment Method:** After order type is confirmed, you MUST ask about payment method: "결제는 카드, 현금, 모바일 중 어떤 방법으로 하시겠어요?"
        5.  **Do Not Proceed:** Do not proceed to final confirmation until all required options, order type, and payment method have been selected by the user.
        
        ## Order Type Options
        - "STORE" (매장이용): When user says "매장에서", "여기서 먹을게요", "매장 이용" etc.
        - "TAKEOUT" (포장): When user says "포장", "테이크아웃", "가져갈게요" etc.
        
        ## Payment Method Options
        - "CARD" (카드): When user says "카드", "신용카드", "체크카드" etc.
        - "CASH" (현금): When user says "현금", "현금결제" etc.
        - "MOBILE" (모바일): When user says "모바일", "휴대폰", "페이" etc.
        
        ## Rules for Order Confirmation and Final JSON Generation
        
        1.  **Complete Order Summary:** When all required options, order type, and payment method are selected, provide a complete summary of the entire order including item names, selected options, quantities, order type, payment method, and total price. Then ask for confirmation like "주문하신 내용이 맞으시면 '네, 주문할게요' 또는 '주문하겠습니다'라고 말씀해 주세요."
        2.  **Wait for Confirmation:** Only proceed to JSON generation after the user explicitly confirms they want to place the order with phrases like "네, 주문할게요", "주문하겠습니다", "맞습니다", or similar affirmative responses.
        3.  **JSON Only Response:** After user confirmation, your **ENTIRE next response MUST be ONLY the JSON object** described below.
        4.  **No Additional Text:** Do NOT add any other text, explanations, or markdown formatting around the JSON object.
        5.  **Exact Structure:** The JSON object MUST match this exact structure and use NUMERIC IDs from the `MENU DATA`.
        
            ```json
            {
              "action": "PLACE_ORDER",
              "order_details": {
                "storeId": 1,
                "storeName": "인클루키오스크 강남점",
                "orderType": "TAKEOUT",
                "paymentMethod": "CARD",
                "items": [
                  {
                    "menuId": 8,
                    "menuName": "아메리카노",
                    "basePrice": 5500,
                    "selectedOptions": {
                      "14": [53],
                      "15": [55]
                    },
                    "optionPrice": 1500,
                    "quantity": 1,
                    "totalPrice": 7000
                  }
                ],
                "totalAmount": 7000,
                "totalItems": 1
              }
            }
            ```
        
        **CRITICAL ID FORMAT RULES:**
        - menuId: Use numeric menu ID from MENU DATA (not string)
        - selectedOptions keys: Use numeric option category IDs (as strings)
        - selectedOptions values: Use numeric option IDs in arrays (not strings)
        
        For all other conversation that is not a final order confirmation, respond naturally in Korean.
        """;
    }

    private String fetchAndFormatMenuData(Long storeId) {
        log.info("Fetching menu data for storeId: {}", storeId);

        try {
            log.debug("Calling menuServiceClient.getMenusWithOptions with storeId: {}", storeId);
            ApiResponse<MenuWithOptionsResponseDto> response = menuServiceClient.getMenusWithOptions(storeId);

            log.info("MenuServiceClient response received. Status: {}, HasData: {}",
                    response != null ? "SUCCESS" : "NULL",
                    response != null && response.getData() != null);

            if (response == null) {
                log.error("MenuServiceClient returned null response");
                return "메뉴 정보를 불러오는 데 실패했습니다. (응답이 null)";
            }

            MenuWithOptionsResponseDto menuData = response.getData();

            if (menuData == null) {
                log.error("MenuServiceClient returned null data in response");
                return "메뉴 정보가 없습니다. (데이터가 null)";
            }

            log.info("Menu data received. Categories count: {}",
                    menuData.getMenusByCategory() != null ? menuData.getMenusByCategory().size() : 0);

            if (menuData.getMenusByCategory().isEmpty()) {
                log.warn("Menu data is empty for storeId: {}", storeId);
                return "메뉴 정보가 없습니다. (카테고리가 비어있음)";
            }

            StringBuilder sb = new StringBuilder();
            int totalMenuItems = 0;

            for (Map.Entry<String, List<MenuWithOptionsResponseDto.MenuDetail>> entry : menuData.getMenusByCategory().entrySet()) {
                String category = entry.getKey();
                List<MenuWithOptionsResponseDto.MenuDetail> items = entry.getValue();

                log.debug("Processing category: {}, items count: {}", category, items.size());
                totalMenuItems += items.size();

                sb.append("## ").append(category).append("\n");
                for (MenuWithOptionsResponseDto.MenuDetail item : items) {
                    sb.append("- ID: ").append(item.getId())
                            .append(", 이름: ").append(item.getName())
                            .append(", 가격: ").append(item.getPrice()).append("원")
                            .append(", 설명: ").append(item.getDescription());

                    if (item.isSoldOut()) {
                        sb.append(" [품절]");
                    }

                    // 옵션 카테고리 정보 추가 (ID 포함)
                    if (item.getOptionCategories() != null && !item.getOptionCategories().isEmpty()) {
                        sb.append("\n  옵션 카테고리:");
                        for (MenuWithOptionsResponseDto.OptionCategory optCat : item.getOptionCategories()) {
                            sb.append("\n    - ID: ").append(optCat.getId())
                                    .append(", 이름: ").append(optCat.getName())
                                    .append(optCat.isRequired() ? " [필수]" : " [선택]");

                            // 중복 제거를 위해 Map 사용 (ID를 키로 사용)
                            java.util.Map<Long, MenuWithOptionsResponseDto.Option> uniqueOptions = new java.util.LinkedHashMap<>();
                            for (MenuWithOptionsResponseDto.Option option : optCat.getOptions()) {
                                uniqueOptions.put(option.getId(), option);
                            }

                            // 중복 제거된 옵션들 출력 (ID 포함)
                            for (MenuWithOptionsResponseDto.Option option : uniqueOptions.values()) {
                                sb.append("\n      * ID: ").append(option.getId())
                                        .append(", 이름: ").append(option.getName())
                                        .append(" (+").append(option.getPrice()).append("원)");
                            }
                        }
                    }
                    sb.append("\n");
                }
            }

            log.info("Menu data formatting completed. Total items: {}, formatted length: {}",
                    totalMenuItems, sb.length());

            return sb.toString();

        } catch (Exception e) {
            log.error("Failed to fetch menu data for storeId: {}. Error: {}", storeId, e.getMessage(), e);
            return "메뉴 정보를 불러오는 데 실패했습니다. (" + e.getMessage() + ")";
        }
    }

    private String formatHistory(List<Message> messages) {
        log.debug("Formatting conversation history. Messages count: {}", messages.size());

        StringBuilder sb = new StringBuilder();
        for (Message message : messages) {
            sb.append(message.getRole()).append(": ").append(message.getContent()).append("\n");
        }

        log.debug("History formatting completed. Length: {}", sb.length());
        return sb.toString();
    }
}