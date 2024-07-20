package com.xuecheng.media.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;

/**
 * @description 媒资文件管理业务类
 * @author Ruoke Zhang
 * @date 2024/7/20 17:18
 * @version 1.0
 */
public interface MediaFileService {

 /**
  * @description 媒资文件查询方法
  * @param pageParams 分页参数
  * @param queryMediaParamsDto 查询条件
  * @return com.xuecheng.base.model.PageResult<com.xuecheng.media.model.po.MediaFiles>
  * @author Mr.M
  * @date 2022/9/10 8:57
 */
 public PageResult<MediaFiles> queryMediaFiels(Long companyId,PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto);

 /**
  * @param companyId
  * @param uploadFileParamsDto 文件信息
  * @param localFilePath 文件本地路径
  * @return UploadFileResultDto
  */
 public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, String localFilePath);

 public MediaFiles addMediaFilesToDb(Long companyId,String fileMd5,UploadFileParamsDto uploadFileParamsDto,String bucket,String objectName);
 /**
  * @description check if file exists in db and minio
  * @param fileMd5
  * @return com.xuecheng.base.model.RestResponse<java.lang.Boolean> false or true
  * @author Ruoke Zhang
  * @date 2024/7/20 17:18
  */
 public RestResponse<Boolean> checkFile(String fileMd5);

 /**
  * @description check if chunk exists in minio
  * @param fileMd5  file's md5
  * @param chunkIndex  chunk's index
  * @return com.xuecheng.base.model.RestResponse<java.lang.Boolean> false or true
  * @author Ruoke Zhang
  * @date 2024/7/20 17:18
  */
 public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex);

 /**
  * @description upload chunk
  * @param fileMd5
  * @param chunk
  * @param localChunkFilePath
  * @return com.xuecheng.base.model.RestResponse
  * @author Ruoke Zhang
  * @date 2024/7/20 17:38
  */
 public RestResponse uploadChunk(String fileMd5,int chunk,String localChunkFilePath);
}
