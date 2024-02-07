package com.hj.ssyx.product.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.common.auth.CredentialsProvider;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.hj.ssyx.product.service.FileUploadService;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.UUID;

/**
 * @author HuangJin
 * @date 1:53 2023/9/24
 */
@Service
public class FileUploadServiceImpl implements FileUploadService {
    @Value("${aliyun.endpoint}")
    String endpoint;

    @Value("${aliyun.keyid}")
    String accessKeyId;

    @Value("${aliyun.keysecret}")
    String accessKeySecret;

    @Value("${aliyun.bucketname}")
    String bucketName;
    @Override
    public String uploadFile(MultipartFile file) {

        // 使用代码嵌入的RAM用户的访问密钥配置访问凭证。
        CredentialsProvider credentialsProvider = new DefaultCredentialProvider(accessKeyId, accessKeySecret);

        // 填写Object完整路径，例如exampledir/exampleobject.txt。Object完整路径中不能包含Bucket名称。
        String objectName = file.getOriginalFilename();
        String uuid = UUID.randomUUID().toString().replaceAll("-","");
        objectName = uuid + objectName;

        // 对上传的文件进行分组，根据当前年/月/日
        // objectName：2023/10/10/uuid01.jpg
        String currentDateTime = new DateTime().toString("yyyy/MM/dd");
        objectName = currentDateTime + "/" + objectName;

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, credentialsProvider);

        try {
            InputStream inputStream = file.getInputStream();
            // 创建PutObjectRequest对象。
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, inputStream);
            // 创建PutObject请求。
            PutObjectResult result = ossClient.putObject(putObjectRequest);
            String url = result.getResponse().getUri();
            return url;
        } catch (Exception ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message:" + ce.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
        return null;
    }
}
