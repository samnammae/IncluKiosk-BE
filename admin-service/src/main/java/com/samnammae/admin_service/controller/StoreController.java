package com.samnammae.admin_service.controller;

import com.samnammae.admin_service.dto.request.StoreRequest;
import com.samnammae.admin_service.service.StoreService;
import com.samnammae.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/store")
@Tag(name = "Admin", description = "관리자 관련 API")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "매장 등록", description = "매장 정보를 입력하여 새로운 매장을 등록합니다.")
    public ApiResponse<Long> registerStore(
            @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = StoreRequest.class)
                    )
            )
            @ModelAttribute StoreRequest request) {
        Long storeId = storeService.createStore(request);
        return ApiResponse.success(storeId);
    }
}
