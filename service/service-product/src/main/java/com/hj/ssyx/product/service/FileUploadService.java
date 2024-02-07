package com.hj.ssyx.product.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author HuangJin
 * @date 1:53 2023/9/24
 */
public interface FileUploadService {
    String uploadFile(MultipartFile file);
}
