package com.xuecheng.media.service;

import com.xuecheng.media.model.po.MediaProcess;

import java.util.List;

/**
 * @author Ruoke Zhang
 * @version 1.0
 * @description 媒资文件处理业务方法
 * @date 2023/7/31 17:29
 */
public interface MediaFileProcessService {

    /**
     * @description 获取待处理任务
     * @param shardIndex 分片序号
     * @param shardTotal 分片总数
     * @param count 获取记录数
     * @return java.util.List<com.xuecheng.media.model.po.MediaProcess>
     * @author Ruoke Zhang
     * @date 2023/7/31 17:29
     */
    public List<MediaProcess> getMediaProcessList(int shardIndex,int shardTotal,int count);

    /**
     *  开启一个任务
     * @param id 任务id
     * @return true开启任务成功，false开启任务失败
     */
    public boolean startTask(long id);

    /**
     * @description 保存任务结果
     * @param taskId  任务id
     * @param status 任务状态
     * @param fileId  文件id
     * @param url url
     * @param errorMsg 错误信息
     * @return void
     * @author Ruoke Zhang
     * @date 2024/07/31 18:02
     */
    void saveProcessFinishStatus(Long taskId,String status,String fileId,String url,String errorMsg);

}