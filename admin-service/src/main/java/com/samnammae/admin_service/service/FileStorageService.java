package com.samnammae.admin_service.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String upload(MultipartFile file);
    void delete(String fileName);
}
