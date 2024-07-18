package com.xuecheng.media;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BigFileTest {
    @Test
    public void testChunk() throws IOException {
        File sourceFile=new File("/Users/ruoke/Documents/DSCF7456.JPG");
        String chunkFilePath = "/Users/ruoke/Documents/java_web/chunk/";
        // 1MB
        int chunkSize=1024*1024*1;
        int chunkNum=(int)Math.ceil(sourceFile.length()*1.0/chunkSize);
        //use stream to read data from the source file, and then write data to the chunks
        RandomAccessFile raf_r=new RandomAccessFile(sourceFile, "r");
        //buffer
        byte[] buffer=new byte[1024];
        for(int i=0;i<chunkNum;i++){
            File chunkFile=new File(chunkFilePath+i);
            // write stream
            RandomAccessFile raf_rw=new RandomAccessFile(chunkFile, "rw");
            int len=-1;
            while((len=raf_r.read(buffer))!=-1){
                raf_rw.write(buffer, 0,len);
                if(chunkFile.length()>=chunkSize){
                    break;
                }
            }
            raf_rw.close();
        }
        raf_r.close();
    }
    //merge those chunks
    @Test
    public void testMerge() throws IOException {
        File chunkFolder=new File("/Users/ruoke/Documents/java_web/chunk/");
        File sourceFile=new File("/Users/ruoke/Documents/DSCF7456.JPG");
        File mergedFile=new File("/Users/ruoke/Documents/merged7456.JPG");

        File[] files=chunkFolder.listFiles();
        //convert it to a list
        List<File> fileList= Arrays.asList(files);

        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return Integer.parseInt(o1.getName()) - Integer.parseInt(o2.getName());
            }
        });
        RandomAccessFile raf_rw=new RandomAccessFile(mergedFile, "rw");
        //iterate each chunked files, write data to the merged file
        for (File file : fileList) {
            RandomAccessFile raf_r=new RandomAccessFile(file, "r");
            int len=-1;
            byte[] buffer=new byte[1024];
            while((len=raf_r.read(buffer))!=-1){
                raf_rw.write(buffer, 0,len);
            }
            raf_r.close();
        }
        raf_rw.close();
        //to see if the source file is the same as the merged file
        FileInputStream fis=new FileInputStream(mergedFile);
        String md5_merged=DigestUtils.md5Hex(fis);
        String md5_source=DigestUtils.md5Hex(new FileInputStream(sourceFile));
        if (md5_merged.equals(md5_source)) {
            System.out.println("合并文件成功");
        } else {
            System.out.println("合并文件失败");
        }
    }
}
