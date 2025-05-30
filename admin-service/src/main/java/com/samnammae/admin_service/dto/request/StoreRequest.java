package com.samnammae.admin_service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StoreRequest {

    private String name;
    private String phone;
    private String address;
    private String introduction;

    private MultipartFile mainImg;
    private MultipartFile logoImg;
    private MultipartFile startBackground;

    private String mainColor;
    private String subColor;
    private String textColor;
}
