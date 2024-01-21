package com.learnincode.media.config;


import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RefreshScope
public class MinIoConfig {


    /**
     * @author CalmKin
     * @description 从nacos读取配置
     * @version 1.0
     * @date 2024/1/21 11:38
     */
    @Value("${minio.endpoint}")
    private String endpoint;
    @Value("${minio.accessKey}")
    private String accessKey;
    @Value("${minio.secretKey}")
    private String secretKey;

    @Bean
    public MinioClient getClient()
    {
        return  MinioClient.builder().endpoint(endpoint).credentials(accessKey, secretKey).build();
    }


}
