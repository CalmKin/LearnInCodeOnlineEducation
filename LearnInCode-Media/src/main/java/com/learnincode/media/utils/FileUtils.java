package com.learnincode.media.utils;

import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.http.MediaType;

import java.io.FileInputStream;
import java.text.SimpleDateFormat;

@Slf4j
public class FileUtils {

    private FileUtils() {
    }

    /**
     * 根据文件路径获取对应ContentType
     *
     * @param
     * @return
     */
    public String getMimeType(String filePath) {
        // 获取文件扩展名
        String fileExtension = filePath.substring(filePath.lastIndexOf('.'));

        // 防止空指针
        if (fileExtension == null) fileExtension = "";

        //根据扩展名取出mimeType
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(fileExtension);
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;//通用mimeType，字节流
        if (extensionMatch != null) {
            mimeType = extensionMatch.getMimeType();
        }
        return mimeType;
    }


    /**
     * 获取文件对象的完整名
     * 年/月/日/md5.扩展名
     *
     * @param filePath 文件路径
     * @return
     */
    public String getBucketObjectName(String filePath) {

        String md5Hex = getMd5Hex(filePath);

        // 获取当前日期拼接文件路径
        SimpleDateFormat datePath = new SimpleDateFormat("yyyy/MM/dd/");

        // 获取文件扩展名
        String extName = filePath.substring(filePath.lastIndexOf('.'));

        return datePath + md5Hex + extName;
    }


    /**
     * 根据文件路径获取md5值
     *
     * @param filePath
     * @return
     */
    public String getMd5Hex(String filePath) {
        String md5Hex = "";
        try (FileInputStream fis = new FileInputStream(filePath)) {
            md5Hex = DigestUtils.md5Hex(fis);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("文件异常,请稍后重试");
        }
        return md5Hex;
    }


    /**
     * @author CalmKin
     * @description 获取大文件在minio里面的存储路径
     * @version 1.0
     * @date 2024/1/22 15:57
     */
    public String getBigFilePath(String md5Hex)
    {

        return md5Hex.substring(0,1) + "/" + md5Hex.substring(1,1) + "/" + md5Hex;
    }


    static class UtilFactory {
        private static final FileUtils UTILS = new FileUtils();
    }

    public static FileUtils getFileUtils() {
        return UtilFactory.UTILS;
    }


}
