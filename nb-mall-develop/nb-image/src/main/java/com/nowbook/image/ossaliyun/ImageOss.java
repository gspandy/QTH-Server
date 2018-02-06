package com.nowbook.image.ossaliyun;

import com.aliyun.oss.OSSClient;
import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.nowbook.image.ImageServer;
import com.nowbook.image.exception.ImageDeleteException;
import com.nowbook.image.exception.ImageUploadException;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Author:  <a href="mailto:jl@nowbook.com">jl</a>
 * Date: 2014-02-11
 */
public class ImageOss implements ImageServer {

    private final static Logger log = LoggerFactory.getLogger(ImageOss.class);

    private final DateTimeFormatter dtf = DateTimeFormat.forPattern("/yyyy/MM/dd/");

    public static final char SEPARATOR = '_';
    private final static Splitter splitter = Splitter.on(SEPARATOR).limit(2).omitEmptyStrings().trimResults();
    private final static HashFunction md5 = Hashing.md5();

    @Value("#{app.endpoint}")
    private String endpoint;

    @Value("#{app.accessKeyId}")
    private String accessKeyId;

    @Value("#{app.accessKeySecret}")
    private String accessKeySecret;

    @Value("#{app.bucketName}")
    private String bucketName;

    /**
     * @param fileName 文件名
     * @param file     文件
     * @return 文件上传后的相对路径
     */
    @Override
    public String write(String fileName, MultipartFile file) throws ImageUploadException {
        String url="";
        String key="nb-image/";
        try {
            OSSClient ossClient = new OSSClient(endpoint, accessKeyId, accessKeySecret);
            String mykey=key+fileName;
            ossClient.putObject(bucketName,mykey, file.getInputStream());
            ossClient.shutdown();
            url=mykey;
            return url;
        } catch (Exception e) {
            log.error("upload to oss file server failed, exception:", e);
            throw new ImageUploadException(e);
        } finally {

        }
    }

    /**
     * @author dpzh
     * @create 2017/7/20 10:16
     * @param fileName 文件名
     * @param file  文件
     * @return: 文件上传后的相对路径
     **/
    @Override
    public String write(String fileName, File file) throws ImageUploadException {
        String url="";
        String key="nb-image/";
        try {
            OSSClient ossClient = new OSSClient(endpoint, accessKeyId, accessKeySecret);
            String mykey=key+fileName;
            ossClient.putObject(bucketName,mykey, new FileInputStream(file));
            ossClient.shutdown();

            url=mykey;
            return url;
        } catch (Exception e) {
            log.error("upload to oss file server failed, exception:", e);
            throw new ImageUploadException(e);
        } finally {

        }
    }

    /**
     * 处理原始文件名, 并返回新的文件名
     *
     * @param originalName 原始文件名
     * @param imageData    原始文件的字节数组
     * @return 新的文件名
     */
    @Override
    public String handleFileName(String originalName, byte[] imageData) {
        String ext = Files.getFileExtension(originalName);
        List<String> parts = splitter.splitToList(originalName);
        if(parts.size()==2) {
            String userId = parts.get(0);
            String originName = parts.get(1)+ UUID.randomUUID();
            return userId + SEPARATOR + md5.hashString(originName, Charsets.UTF_8).toString()+"."+ext;
        }else{
            return md5.hashString(originalName, Charsets.UTF_8).toString()+"."+ext;
        }
    }

    /**
     * 刪除文件
     * @param fileName 文件名
     * @return 是否刪除成功
     */
    @Override
    public boolean delete(String fileName) throws ImageDeleteException {
        return false;
    }
}
