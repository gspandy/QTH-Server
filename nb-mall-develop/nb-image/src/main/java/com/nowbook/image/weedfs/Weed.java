package com.nowbook.image.weedfs;

import com.nowbook.common.utils.JsonMapper;
import com.nowbook.image.ImageServer;
import com.nowbook.image.exception.ImageDeleteException;
import com.nowbook.image.exception.ImageUploadException;
import com.fasterxml.jackson.databind.JavaType;
import com.github.kevinsawicki.http.HttpRequest;
import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.io.Files;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.HashMap;
import java.util.Map;


/**
 *
 * 图片API的Weed实现
 *
 * Author:  <a href="mailto:cheng@nowbook.com">xiao</a>
 * Date: 2013-13-27
 */
@Slf4j
public class Weed implements ImageServer {

    private final static JsonMapper jsonMapper = JsonMapper.JSON_NON_DEFAULT_MAPPER;
    private final static JavaType mapType = jsonMapper.createCollectionType(HashMap.class, String.class, String.class);

    //图片上传路径
    @Value("#{app.imageUploadUrl}")
    private String imageUploadUrl;
    //图片读取路径
    @Value("#{app.imageBaseUrl}")
    private String imageBaseUrl;
    //图片最大尺寸
    @Value("#{app.imgSizeMax}")
    private long imgSizeMax;


    /**
     * @param fileName 文件名
     * @param file     文件
     * @return 文件上传后的相对路径
     */
    @Override
    public String write(String fileName, MultipartFile file) throws ImageUploadException {
        String suffix = Files.getFileExtension(file.getOriginalFilename()).toLowerCase();
        if (file.getSize() > imgSizeMax){
            throw new ImageUploadException("Image can not larger than" + imgSizeMax);
        }
        HttpRequest assignRequest = HttpRequest.post(imageUploadUrl+"/dir/assign");
        InputStream is  = null;
        String result = null;

        try {
            is = file.getInputStream();

            if (assignRequest.ok()) {
                String response = assignRequest.body(Charsets.UTF_8.name());
                Map<String, String> json = jsonMapper.fromJson(response, mapType);
                String fid = json.get("fid");
                String publicUrl = json.get("publicUrl");
                HttpRequest uploadRequest = HttpRequest.post("http://"+publicUrl + "/"+fid);
                fid=fid.replace(',', '/');
               // uploadRequest.part("file",is);
                uploadRequest.part("file", file.getOriginalFilename(), file.getContentType(),is);
                String str=uploadRequest.body();
                result = "/" + fid + "." + suffix;
            }
        } catch(Exception e){
            log.error("upload to weedfs failed, exception:",e);
            throw new ImageUploadException(e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    //ignore this fxxk exception
                }
            }
        }

        return result;
    }

    @Override
    public String write(String fileName, File file) throws ImageUploadException {
        HttpRequest assignRequest = HttpRequest.post(imageUploadUrl+"/dir/assign");
        InputStream is  = null;
        String result = null;
        try {
            is =  new FileInputStream(file.getPath());

            if (assignRequest.ok()) {
                String response = assignRequest.body(Charsets.UTF_8.name());
                Map<String, String> json = jsonMapper.fromJson(response, mapType);
                String fid = json.get("fid");
                String publicUrl = json.get("publicUrl");
                HttpRequest uploadRequest = HttpRequest.post("http://"+publicUrl + "/"+fid);
                fid=fid.replace(',', '/');
                // uploadRequest.part("file",is);
                uploadRequest.part("file",file.getName() , "image/png",is);
                String str=uploadRequest.body();
                result = "/" + fid + ".png";
            }
        } catch(Exception e){
            log.error("upload to weedfs failed, exception:",e);
            throw new ImageUploadException(e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    //ignore this fxxk exception
                }
            }
        }

        return result;
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
        return originalName;
    }

    /**
     * 刪除文件
     *
     * @param fileName  文件的相对路径
     * @return 是否刪除成功
     */
    @Override
    public boolean delete(String fileName) throws ImageDeleteException {

        String imgUri = imageBaseUrl + fileName;
        try{
            //invoke weedfs delete api
            HttpRequest request = HttpRequest.delete(imgUri);
            if(!request.ok()){
                return false;
            }
        }catch(Exception e){
            log.error("delete image(fileName={}) of weedfs failed,cause:{}",fileName, Throwables.getStackTraceAsString(e));
            throw new ImageDeleteException(e);
        }

        return true;
    }
}
