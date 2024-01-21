package com.learnincode.media;

import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import io.minio.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterInputStream;

@SpringBootTest
class MediaApplicationTests {


    MinioClient minioClient =
            MinioClient.builder()
                    .endpoint("http://192.168.101.65:9000")
                    .credentials("minioadmin", "minioadmin")
                    .build();


    /**
     * @author CalmKin
     * @description 测试上传文件
     * @version 1.0
     * @date 2024/1/21 10:13
     */
    @Test
    void upload() throws Exception {

        //根据扩展名取出mimeType
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(".mp4");
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;//通用mimeType，字节流
        if (extensionMatch != null) {
            mimeType = extensionMatch.getMimeType();
        }


        UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                .bucket("testbucket")   // 指定桶
                .object("2022-6-30-50.webp")    //指定对象名（可以随便取）
                .filename("C:\\Users\\86158\\Desktop\\图片\\壁纸\\2022-6-30-50.webp")   // 指定本地文件路径
                .contentType(mimeType)  // 指定文件格式
                .build();

        minioClient.uploadObject(uploadObjectArgs);

    }


    @Test
    public void getFile() throws Exception{
        GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                .bucket("testbucket")
                .object("2022-6-30-50.webp")
                .build();
        try(

                FilterInputStream inputStream = minioClient.getObject(getObjectArgs);
                FileOutputStream outputStream = new FileOutputStream("C:\\Users\\86158\\Desktop\\图片\\壁纸\\2024-6-30-50.webp");
        ) {
            // 将输入流写入到输出流中
            IOUtils.copy(inputStream,outputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }


        // 获取本地文件摘要
        String localHash = DigestUtils.md5Hex(new FileInputStream("C:\\Users\\86158\\Desktop\\图片\\壁纸\\2024-6-30-50.webp"));

        StatObjectArgs args = StatObjectArgs.builder().bucket("testbucket").object("2022-6-30-50.webp").build();

        // 获取minio上面的文件元数据
        String cloudHash = minioClient.statObject(args).etag();

        if(cloudHash.equals(localHash))
        {
            System.out.println("文件下载成功");
        }else {
            System.out.println("文件损坏");
        }
    }

    @Test
    void remove() {
        RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder()
                .bucket("testbucket")
                .object("2022-6-30-50.webp")
                .build();
        try {
            minioClient.removeObject(removeObjectArgs);
            System.out.println("删除文件成功");
        } catch (Exception e) {
            System.out.println("删除文件失败");
        }

    }


}
