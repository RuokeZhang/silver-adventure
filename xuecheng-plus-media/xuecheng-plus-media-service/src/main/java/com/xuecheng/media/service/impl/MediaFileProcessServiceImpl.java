package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessHistoryMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.model.po.MediaProcessHistory;
import com.xuecheng.media.service.MediaFileProcessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class MediaFileProcessServiceImpl implements MediaFileProcessService {

    @Autowired
    MediaProcessMapper mediaProcessMapper;

    @Autowired
    MediaFilesMapper mediaFilesMapper;

    @Autowired
    MediaProcessHistoryMapper mediaProcessHistoryMapper;

    @Override
    public List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count) {
        List<MediaProcess> mediaProcesses = mediaProcessMapper.selectListByShardIndex(shardTotal, shardIndex, count);
        return mediaProcesses;
    }

    public boolean startTask(long id) {
        int result = mediaProcessMapper.startTask(id);
        return result<=0?false:true;
    }

    @Transactional
    @Override
    public void saveProcessFinishStatus(Long taskId, String status, String fileId, String url, String errorMsg) {
        //get that task from db, if not exists, return
        MediaProcess mediaProcess = mediaProcessMapper.selectById(taskId);
        if(mediaProcess == null){
            return ;
        }
        //relate task id and process id
        LambdaQueryWrapper<MediaProcess> queryWrapperById = new LambdaQueryWrapper<MediaProcess>().eq(MediaProcess::getId, taskId);
        //if task fails
        if(status.equals("3")){
            MediaProcess mediaProcess_u = new MediaProcess();
            mediaProcess_u.setStatus("3");
            mediaProcess_u.setErrormsg(errorMsg);
            mediaProcess_u.setFailCount(mediaProcess.getFailCount()+1);
            mediaProcessMapper.update(mediaProcess_u,queryWrapperById);
            log.debug("update the task status as fail，the message is:{}",mediaProcess_u);
            return ;
        }
        //task successes
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileId);
        if(mediaFiles!=null){
            //update the url in media db
            mediaFiles.setUrl(url);
            mediaFilesMapper.updateById(mediaFiles);
        }
        //update url and status to the process db
        mediaProcess.setUrl(url);
        mediaProcess.setStatus("2");
        mediaProcess.setFinishDate(LocalDateTime.now());
        mediaProcessMapper.updateById(mediaProcess);

        //add this process to the history db
        MediaProcessHistory mediaProcessHistory = new MediaProcessHistory();
        BeanUtils.copyProperties(mediaProcess, mediaProcessHistory);
        mediaProcessHistoryMapper.insert(mediaProcessHistory);
        //delete this process from the process db
        mediaProcessMapper.deleteById(mediaProcess.getId());

    }

}
