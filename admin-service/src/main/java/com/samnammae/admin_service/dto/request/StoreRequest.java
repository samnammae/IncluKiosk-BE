package com.samnammae.admin_service.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreRequest {

    private String name;
    private String phone;
    private String address;
    private String introduction;

    @Schema(type = "string", format = "binary")
    private MultipartFile mainImg;
    @Schema(type = "string", format = "binary")
    private MultipartFile logoImg;
    @Schema(type = "string", format = "binary")
    private MultipartFile startBackground;

    private String mainColor;
    private String subColor;
    private String textColor;
}
