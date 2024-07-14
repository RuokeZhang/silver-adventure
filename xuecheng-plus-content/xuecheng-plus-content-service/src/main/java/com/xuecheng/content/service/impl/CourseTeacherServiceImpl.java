package com.xuecheng.content.service.impl;

import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.CourseTeacherMapper;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CourseTeacherServiceImpl implements CourseTeacherService {
    @Autowired
    private CourseTeacherMapper courseTeacherMapper;

    @Override
    public CourseTeacher saveCourseTeacher(CourseTeacher courseTeacher) {
        //合法性检验
        if (StringUtils.isBlank(courseTeacher.getTeacherName())) {
            throw new XueChengPlusException("教师姓名不能为空");
        }
        if (StringUtils.isBlank(courseTeacher.getPosition())) {
            throw new XueChengPlusException("教师职位不能为空");
        }
        //如果是新增加老师
        if (courseTeacher.getId()==null){
            CourseTeacher courseTeacher1 = new CourseTeacher();
            //将传入的页面的参数放到里面
            BeanUtils.copyProperties(courseTeacher, courseTeacher1);
            //插入数据库
            int insert=courseTeacherMapper.insert(courseTeacher1);
            if(insert<0){
                throw new RuntimeException("添加课程失败");
            }
            //接着从数据库里面拿到这个新建好的教师
            return getCourseTeacher(courseTeacher1.getId());
        } else {
            //如果是修改老师信息
            //拿到老师id
            Long teacherId = courseTeacher.getId();
            //查询老师信息
            CourseTeacher courseTeacher1 = courseTeacherMapper.selectById(teacherId);

            if (courseTeacher1 == null) {
                XueChengPlusException.cast("老师不存在");
            }
            //封装数据
            BeanUtils.copyProperties(courseTeacher, courseTeacher1);
            //更新数据库
            int i = courseTeacherMapper.updateById(courseTeacher1);
            if (i <= 0) {
                XueChengPlusException.cast("修改老师失败");
            }
            //接着从数据库里面拿到这个新建好的教师
            return getCourseTeacher(courseTeacher1.getId());
        }

    }

    @Override
    public CourseTeacher getCourseTeacher(Long teacherId){
        return courseTeacherMapper.selectById(teacherId);
    }

}
