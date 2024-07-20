package com.xuecheng.media.api;

import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.service.MediaFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@Api(value = "Big File Upload Api", tags = "Big File Upload Api")
@RestController
public class BigFilesController {
    @Autowired
    MediaFileService mediaFileService;

    @ApiOperation(value = "pre-check files before upload")
    @PostMapping("/upload/checkfile")
    public RestResponse<Boolean> checkfile(
            @RequestParam("fileMd5") String fileMd5
    ) throws Exception {
        return mediaFileService.checkFile(fileMd5);
    }


    @ApiOperation(value = "pre-check chunked files before upload")
    @PostMapping("/upload/checkchunk")
    public RestResponse<Boolean> checkchunk(@RequestParam("fileMd5") String fileMd5,
                                            @RequestParam("chunk") int chunk) throws Exception {

        return mediaFileService.checkChunk(fileMd5,chunk);
    }

    @ApiOperation(value = "upload chunk files")
    @PostMapping("/upload/uploadchunk")
    public RestResponse uploadchunk(@RequestParam("file") MultipartFile file,
                                    @RequestParam("fileMd5") String fileMd5,
                                    @RequestParam("chunk") int chunk) throws Exception {
        //create a temporary file
        File tempFile = File.createTempFile("minio", "temp");
        //copy the file to the tempFile
        file.transferTo(tempFile);
        //get a local path
        String absolutePath = tempFile.getAbsolutePath();
        return mediaFileService.uploadChunk(fileMd5,chunk,absolutePath);
    }

    @ApiOperation(value = "merge chunk files")
    @PostMapping("/upload/mergechunks")
    public RestResponse mergechunks(@RequestParam("fileMd5") String fileMd5,
                                    @RequestParam("fileName") String fileName,
                                    @RequestParam("chunkTotal") int chunkTotal) throws Exception {
        return null;

    }


}