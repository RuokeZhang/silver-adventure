package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.service.MediaFileService;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * @author Ruoke Zhang
 * @version 1.0
 * @description TODO
 * @date 2024/07/18 16:48
 */
@Slf4j
@Service
public class MediaFileServiceImpl implements MediaFileService {

    @Autowired
    MediaFilesMapper mediaFilesMapper;

    @Autowired
    MinioClient minioClient;

    @Autowired
    MediaFileService currentProxy;
    //ensure it's a proxy, so we can set its function as a transaction

    //存储普通文件，从配置里面拿到 bucket name
    @Value("${minio.bucket.files}")
    private String bucket_mediafiles;

    //存储视频
    @Value("${minio.bucket.videofiles}")
    private String bucket_video;

    @Override
    public PageResult<MediaFiles> queryMediaFiels(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {

        //构建查询条件对象
        LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();

        //分页对象
        Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<MediaFiles> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        PageResult<MediaFiles> mediaListResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
        return mediaListResult;

    }

    //获取文件默认存储目录路径 年/月/日
    private String getDefaultFolderPath() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String folder = sdf.format(new Date()).replace("-", "/")+"/";
        return folder;
    }
    //获取文件的md5
    private String getFileMd5(File file) {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            String fileMd5 = DigestUtils.md5Hex(fileInputStream);
            return fileMd5;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //根据扩展名获得 mimeType
    public String getMimeType(String extension){
        if (extension==null){
            extension="";
        }
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch("extension");
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;//通用mimeType，字节流
        if(extensionMatch!=null){
            mimeType = extensionMatch.getMimeType();
        }
        return mimeType;
    }

    //将文件上传到 minio
    public boolean upLoadFileToMinIO(String localFilePath,String mimeType,String bucket, String objectName){
        //上传文件的参数信息
        try {
            UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)//添加子目录
                    .filename(localFilePath)//本地文件路径
                    //.contentType("image/jpg")//默认根据扩展名确定文件内容类型，也可以指定
                    .contentType(mimeType)
                    .build();
            //上传文件
            minioClient.uploadObject(uploadObjectArgs);
            log.debug("successfully uploaded file to MinIO, bucket:{}, objectName:{}", bucket, objectName);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            log.error("上传文件出错, bucket:{}, objectName:{}, 错误信息:{}", bucket, objectName, e.getMessage());
        }
        return false;
    }

    @Override
    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, String localFilePath) {
        //得到文件名
        String fileName= uploadFileParamsDto.getFilename();
        //得到扩展名
        String extension= fileName.substring(fileName.lastIndexOf("."));

        String mimeType = getMimeType(extension);

        String defaultFolderPath = getDefaultFolderPath();
        //md5值
        String fileMd5 = getFileMd5(new File(localFilePath));
        String objectName=defaultFolderPath+fileMd5+extension;
        boolean result=upLoadFileToMinIO(localFilePath, mimeType, bucket_mediafiles, objectName);
        if(!result){
            XueChengPlusException.cast("file upload to minio fail");
        }
        //save the file to db, bc this step needs internet, and it may need much time,
        // so it's not efficient to add @Transcation to the whole func
        MediaFiles file =currentProxy.addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucket_mediafiles, objectName);
        if (file==null){
            XueChengPlusException.cast("after uploading file to minio, saving file info to db fails");
        }
        //prepare the return object
        UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
        BeanUtils.copyProperties(uploadFileParamsDto, uploadFileResultDto);
        return uploadFileResultDto;

    }


    @Override
    @Transactional
    public MediaFiles addMediaFilesToDb(Long companyId,String fileMd5,UploadFileParamsDto uploadFileParamsDto,String bucket,String objectName){
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if(mediaFiles == null){
            mediaFiles = new MediaFiles();
            BeanUtils.copyProperties(uploadFileParamsDto,mediaFiles);
            //文件id
            mediaFiles.setId(fileMd5);
            //机构id
            mediaFiles.setCompanyId(companyId);
            //桶
            mediaFiles.setBucket(bucket);
            //file_path
            mediaFiles.setFilePath(objectName);
            //file_id
            mediaFiles.setFileId(fileMd5);
            //url
            mediaFiles.setUrl("/"+bucket+"/"+objectName);
            //上传时间
            mediaFiles.setCreateDate(LocalDateTime.now());
            //状态
            mediaFiles.setStatus("1");
            //审核状态
            mediaFiles.setAuditStatus("002003");
            //插入数据库
            int insert = mediaFilesMapper.insert(mediaFiles);
            if(insert<=0){
                log.debug("fail to save file info to db");
                return null;
            }
            return mediaFiles;

        }
        return mediaFiles;

    }

    @Override
    public RestResponse<Boolean> checkFile(String fileMd5) {
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        // If it's in the DB, check if it's in MinIO
        if (mediaFiles != null) {
            String bucket = mediaFiles.getBucket();
            String filePath = mediaFiles.getFilePath();
            InputStream stream = null;
            try {
                stream = minioClient.getObject(
                        GetObjectArgs.builder()
                                .bucket(bucket)
                                .object(filePath)
                                .build());

                if (stream != null) {
                    // File exists in MinIO
                    return RestResponse.success(true);
                }
            } catch (Exception e) {
                // Log the exception (optional)
                // e.printStackTrace();
                return RestResponse.success(false);
            } finally {
                // Ensure the InputStream is closed properly
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e) {
                        // Log the exception (optional)
                        // e.printStackTrace();
                    }
                }
            }
        }
        // It doesn't exist in MinIO
        return RestResponse.success(false);
    }

    @Override
    public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex) {
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        //get the directory
        String chunkPath=getChunkFileFolderPath(fileMd5);
        //get the path
        String chunkFilePath = chunkPath + chunkIndex;
        InputStream fileInputStream = null;
        try {
            fileInputStream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket_video)
                            .object(chunkFilePath)
                            .build());

            if (fileInputStream != null) {
                return RestResponse.success(true);
            }
        } catch (Exception e) {

        }
        return RestResponse.success(false);
    }

    @Override
    public RestResponse uploadChunk(String fileMd5, int chunk, String localChunkFilePath) {
        //get the directory
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        //get the path
        String chunkFilePath = chunkFileFolderPath + chunk;
        //mimeType
        String mimeType = getMimeType(null);
        boolean b = upLoadFileToMinIO(localChunkFilePath, mimeType, bucket_video, chunkFilePath);
        if (!b) {
            log.debug("upload chunk to minio fails:{}", chunkFilePath);
            return RestResponse.validfail(false, "upload chunk to minio fails");
        }
        log.debug("upload chunk to minio successes:{}",chunkFilePath);
        return RestResponse.success(true);
    }

    //the subdirectory has the structure of md5 value's first two chars
    private String getChunkFileFolderPath(String fileMd5) {
        return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + "chunk" + "/";
    }

}
