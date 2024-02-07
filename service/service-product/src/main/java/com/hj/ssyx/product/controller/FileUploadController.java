package com.hj.ssyx.product.controller;

import com.hj.ssyx.common.result.Result;
import com.hj.ssyx.product.service.FileUploadService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author HuangJin
 * @date 1:09 2023/9/24
 */
@Api(tags = "")
@RestController
@RequestMapping("admin/product")
public class FileUploadController {
    @Autowired
    private FileUploadService fileUploadService;

    // 图片上传
    @ApiOperation("图片上传")
    @PostMapping("fileUpload")
    public Result fileUpload(MultipartFile file) {
        String url = fileUploadService.uploadFile(file);
        return Result.ok(url);
    }
}
