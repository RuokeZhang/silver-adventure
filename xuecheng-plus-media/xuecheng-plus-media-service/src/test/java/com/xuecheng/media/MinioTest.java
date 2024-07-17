package com.xuecheng.media;

import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.UploadObjectArgs;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.http.MediaType;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterInputStream;


public class MinioTest {

    MinioClient minioClient =
            MinioClient.builder()
                    .endpoint("http://127.0.0.1:9000")
                    .credentials("minioadmin", "minioadmin")
                    .build();

    @Test
    public void test_upload(){
        try {
            //根据扩展名取出mimeType
            ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(".jpg");
            String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;//通用mimeType，字节流
            if(extensionMatch!=null){
                mimeType = extensionMatch.getMimeType();
            }
            //上传文件的参数信息
            UploadObjectArgs testbucket = UploadObjectArgs.builder()
                    .bucket("testbucket")
                    .object("test/文件名")//添加子目录
                    .filename("/Users/ruoke/Documents/DSCF7456.JPG")//本地文件路径
                    //.contentType("image/jpg")//默认根据扩展名确定文件内容类型，也可以指定
                    .contentType("mimeType")
                    .build();
            //上传文件
            minioClient.uploadObject(testbucket);
            System.out.println("上传成功");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("上传失败");
        }
    }
    @Test
    public void test_delete(){
        try {
            //删除文件的参数信息
            RemoveObjectArgs testRemove=RemoveObjectArgs.builder()
                    .bucket("testbucket")
                    .object("文件名")
                    .build();
            //上传文件
            minioClient.removeObject(testRemove);
            System.out.println("删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("删除失败");
        }
    }

    //查询文件
    @Test
    public void test_find(){
        try {
            //查询文件的参数信息
            GetObjectArgs testGet=GetObjectArgs.builder()
                    .object("test/文件名")
                            .bucket("testbucket")
                                    .build();
            //查询文件
            FilterInputStream filterInputStream=minioClient.getObject(testGet);
            FileOutputStream  fileOutputStream=new FileOutputStream(new File("/Users/ruoke/Documents//downloaded.JPG"));
            IOUtils.copy(filterInputStream,fileOutputStream);

            ///校验文件的完整性对文件的内容进行md5
            FileInputStream fileInputStream1 = new FileInputStream(new File("/Users/ruoke/Documents/DSCF7456.JPG"));
            String source_md5 = DigestUtils.md5Hex(fileInputStream1);
            FileInputStream fileInputStream = new FileInputStream(new File("/Users/ruoke/Documents//downloaded.JPG"));
            String local_md5 = DigestUtils.md5Hex(fileInputStream);
            if(source_md5.equals(local_md5)){
                System.out.println("下载成功");
            }

            System.out.println("查询成功");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("查询失败");
        }
    }
}
