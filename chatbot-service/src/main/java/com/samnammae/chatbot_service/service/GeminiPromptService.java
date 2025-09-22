package com.samnammae.chatbot_service.service;

import com.samnammae.chatbot_service.client.MenuServiceClient;
import com.samnammae.chatbot_service.domain.conversation.Conversation;
import com.samnammae.chatbot_service.domain.message.Message;
import com.samnammae.chatbot_service.dto.response.MenuResponseDto;
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
        String menuData = fetchAndFormatMenuData(storeId, managedStoreIds);
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
            # ROLE & VERY IMPORTANT RULE
            You are a helpful kiosk ordering assistant for 'inclukiosk'.
            Your primary goal is to help users build an order based on the provided MENU DATA.

            When the user finalizes their order, you MUST follow this rule:
            1. Your ENTIRE response MUST be ONLY a JSON object.
            2. Do NOT add any other text, explanations, or markdown formatting.
            3. The JSON object MUST match this exact structure:
               {
                 "action": "PLACE_ORDER",
                 "order_details": {
                   "storeId": 1,
                   "storeName": "현재 매장 이름",
                   "orderType": "TAKEOUT",
                   "paymentMethod": "CARD",
                   "items": [
                     {
                       "menuId": 101,
                       "menuName": "아메리카노",
                       "basePrice": 4500,
                       "selectedOptions": { "1": [10] },
                       "optionPrice": 500,
                       "quantity": 1,
                       "totalPrice": 5000
                     }
                   ],
                   "totalAmount": 5000,
                   "totalItems": 1
                 }
               }
            
            For all other conversation that is not a final order confirmation, respond naturally in Korean.
            """;
    }

    private String fetchAndFormatMenuData(Long storeId, String managedStoreIds) {
        log.info("Fetching menu data for storeId: {}", storeId);

        try {
            log.debug("Calling menuServiceClient.getMenusByStore with storeId: {}", storeId);
            ApiResponse<MenuResponseDto> response = menuServiceClient.getMenusByStore(storeId, managedStoreIds);

            log.info("MenuServiceClient response received. Status: {}, HasData: {}",
                    response != null ? "SUCCESS" : "NULL",
                    response != null && response.getData() != null);

            if (response == null) {
                log.error("MenuServiceClient returned null response");
                return "메뉴 정보를 불러오는 데 실패했습니다. (응답이 null)";
            }

            MenuResponseDto menuData = response.getData();

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

            for (Map.Entry<String, List<MenuResponseDto.MenuItemDto>> entry : menuData.getMenusByCategory().entrySet()) {
                String category = entry.getKey();
                List<MenuResponseDto.MenuItemDto> items = entry.getValue();

                log.debug("Processing category: {}, items count: {}", category, items.size());
                totalMenuItems += items.size();

                sb.append("## ").append(category).append("\n");
                for (MenuResponseDto.MenuItemDto item : items) {
                    sb.append("- 이름: ").append(item.getName())
                            .append(", 가격: ").append(item.getPrice()).append("원")
                            .append(", 설명: ").append(item.getDescription()).append("\n");
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