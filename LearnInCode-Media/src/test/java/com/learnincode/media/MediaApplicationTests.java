package com.learnincode.media;

import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import io.minio.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

import java.io.*;

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

    /**
     * @author CalmKin
     * @description 测试文件分块功能
     * @version 1.0
     * @date 2024/1/22 10:48
     */
    @Test
    void testChunck() throws Exception {
        // 源文件路径
        String sourceFile = "C:\\Users\\86158\\Desktop\\文件中转站\\点餐.mp4";
        // 分块文件存放目录
        String chunkDir = "C:\\Users\\86158\\Desktop\\文件中转站\\chunk\\";
        File chunkDirFile = new File(chunkDir);
        // 如果目录不存在，创建新目录
        if(!chunkDirFile.exists())
        {
            chunkDirFile.mkdirs();
        }

        // 读取源文件
        RandomAccessFile raf_r = new RandomAccessFile(sourceFile, "r");
        long length = raf_r.length();

        // 分块大小
        int chunkSize = 1024 * 1024 * 1;
        // 分块数量
        long chunkCount = (length + chunkSize - 1) / chunkSize;

        // 缓冲区，一次只读1KB
        byte[] buf = new byte[1024];

        for(int i=0; i<chunkCount; i++)
        {
            // 先创建对应分块
            File chunkFile = new File(chunkDir + i );
            // 如果文件存在，先删除原来的文件
            if(chunkFile.exists())
            {
                chunkDirFile.delete();
            }

            // 创建文件
            boolean success = chunkFile.createNewFile();
            // 文件创建不成功，没必要执行下去
            if(!success) return;

            // 文件读取指针
            int size = -1;
            // 要往分块里面写数据，所以是rw权限
            RandomAccessFile raf_rw = new RandomAccessFile(chunkFile, "rw");
            // 源文件没有读完（只要源文件还剩下哪怕1B，都会按照剩下的大小读掉，只有源文件全读完了，再读才会返回-1）
            while( (size = raf_r.read(buf) ) != -1 )
            {
                raf_rw.write(buf,0, size);
                // 读满了1MB，那么结束当前chunk
                if(raf_rw.length() >= chunkSize)
                {
                    break;
                }
            }
            // 记得关闭文件读取
            raf_rw.close();
            System.out.println("完成分块" + i);
        }
        // 整个源文件读完了，把读入流关闭
        raf_r.close();
    }








}
