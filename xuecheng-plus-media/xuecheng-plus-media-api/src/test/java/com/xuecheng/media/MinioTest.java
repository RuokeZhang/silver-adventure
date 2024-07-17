package com.xuecheng.media;

import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class MinioTest {

    MinioClient minioClient =
            MinioClient.builder()
                    .endpoint("https://127.0.0.1:58915")
                    .credentials("minioadmin", "minioadmin")
                    .build();

    @Test
    public void test_upload(){
        try {
            //上传文件的参数信息
            UploadObjectArgs testbucket = UploadObjectArgs.builder()
                    .bucket("testbucket")
                    .object("文件名")//添加子目录
                    .filename("/Users/ruoke/Documents/DSCF7456.JPG")//本地文件路径
                    //.contentType("video/mp4")//默认根据扩展名确定文件内容类型，也可以指定
                    .build();
            //上传文件
            minioClient.uploadObject(testbucket);
            System.out.println("上传成功");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("上传失败");
        }
    }
}
