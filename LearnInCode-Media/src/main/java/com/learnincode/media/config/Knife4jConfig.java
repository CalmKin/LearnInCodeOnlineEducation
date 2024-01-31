package com.learnincode.media.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
@Profile({"dev", "test"})
public class Knife4jConfig {
    @Bean
    public Docket defaultApi2() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(new ApiInfoBuilder()
                        .title("媒资管理接口文档")
                        .description("媒资管理模块")
                        .version("1.0")
                        .build())
                .select()
                // 指定 Controller 扫描包路径(这里要修改成自己项目的controller路径)
                .apis(RequestHandlerSelectors.basePackage("com.learnincode.media.controller"))
                .paths(PathSelectors.any())
                .build();
    }
}