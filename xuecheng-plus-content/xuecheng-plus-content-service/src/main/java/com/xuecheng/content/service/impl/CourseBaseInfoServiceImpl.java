package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseCategory;
import com.xuecheng.content.model.po.CourseMarket;
import com.xuecheng.content.service.CourseBaseInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class CourseBaseInfoServiceImpl implements CourseBaseInfoService {
    @Autowired
    CourseBaseMapper courseBaseMapper;
    @Autowired
    CourseMarketMapper courseMarketMapper;
    @Autowired
    CourseCategoryMapper courseCategoryMapper;

    @Override
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto courseParamsDto){

        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();
        //根据名称模糊查询,在sql中拼接 course_base.name like '%值%'
        queryWrapper.like(org.apache.commons.lang3.StringUtils.isNotEmpty(courseParamsDto.getCourseName()),CourseBase::getName,courseParamsDto.getCourseName());
        //根据课程审核状态查询 course_base.audit_status = ?
        queryWrapper.eq(StringUtils.isNotEmpty(courseParamsDto.getAuditStatus()), CourseBase::getAuditStatus,courseParamsDto.getAuditStatus());
        //todo:按课程发布状态查询

        //创建page分页参数对象
        Page<CourseBase> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        //开始进行分页查询
        Page<CourseBase> pageResult = courseBaseMapper.selectPage(page, queryWrapper);
        //数据列表
        List<CourseBase> items = pageResult.getRecords();
        //总记录数
        long total = pageResult.getTotal();

        //List<T> items, long counts, long page, long pageSize
        PageResult<CourseBase> courseBasePageResult = new PageResult<CourseBase>(items,total,pageParams.getPageNo(), pageParams.getPageSize());
        return courseBasePageResult;
    }

    @Transactional
    @Override
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto dto) {

        //合法性校验
        if (StringUtils.isBlank(dto.getName())) {
            throw new XueChengPlusException("课程名称为空");
        }

        if (StringUtils.isBlank(dto.getMt())) {
            throw new XueChengPlusException("课程分类为空");
        }

        if (StringUtils.isBlank(dto.getSt())) {
            throw new XueChengPlusException("课程分类为空");
        }

        if (StringUtils.isBlank(dto.getGrade())) {
            throw new XueChengPlusException("课程等级为空");
        }

        if (StringUtils.isBlank(dto.getTeachmode())) {
            throw new XueChengPlusException("教育模式为空");
        }

        if (StringUtils.isBlank(dto.getUsers())) {
            throw new XueChengPlusException("适应人群");
        }

        if (StringUtils.isBlank(dto.getCharge())) {
            throw new XueChengPlusException("收费规则为空");
        }

        //向课程基本信息表course_base写入数据
        CourseBase courseBaseNew = new CourseBase();
        //将传入的页面的参数放到里面
        BeanUtils.copyProperties(dto, courseBaseNew);//只要属性名字一致就可以拷贝
        courseBaseNew.setCompanyId(companyId);
        courseBaseNew.setCreateDate(LocalDateTime.now());
        //审核，发布状态默认为未提交
        courseBaseNew.setAuditStatus("202002");
        courseBaseNew.setStatus("203001");
        //插入数据库
        int insert=courseBaseMapper.insert(courseBaseNew);
        if(insert<0){
            throw new RuntimeException("添加课程失败");
        }
        //向课程营销表course_market写入数据
        CourseMarket courseMarketNew = new CourseMarket();
        //将页面输入的数据拷贝到courseMarketNew
        BeanUtils.copyProperties(dto, courseMarketNew);
        //课程的ID
        Long courseId = courseBaseNew.getId();
        courseMarketNew.setId(courseId);
        //保存到数据库里
        saveCourseMarket(courseMarketNew);
        return getCourseBaseInfo(courseId);
    }
    //保存课程营销信息
    private int saveCourseMarket(CourseMarket courseMarketNew){
        //收费规则
        String charge = courseMarketNew.getCharge();
        if(StringUtils.isBlank(charge)){
            throw new RuntimeException("收费规则没有选择");
        }
        //收费规则为收费
        if(charge.equals("201001")){
            if(courseMarketNew.getPrice() ==null || courseMarketNew.getPrice().floatValue()<=0){
                throw new XueChengPlusException("课程的价格不能为空并且必须大于0");
            }
        }
        //根据id从课程营销表查询
        CourseMarket courseMarketObj = courseMarketMapper.selectById(courseMarketNew.getId());
        if(courseMarketObj == null){
            return courseMarketMapper.insert(courseMarketNew);
        }else{
            BeanUtils.copyProperties(courseMarketNew,courseMarketObj);
            courseMarketObj.setId(courseMarketNew.getId());
            return courseMarketMapper.updateById(courseMarketObj);
        }
    }
    //查询课程信息
    public CourseBaseInfoDto getCourseBaseInfo(Long courseId){
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if(courseBase == null){
            return null;
        }
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBase,courseBaseInfoDto);
        if(courseMarket != null){
            BeanUtils.copyProperties(courseMarket,courseBaseInfoDto);
        }

        //查询分类名称
        CourseCategory courseCategoryBySt = courseCategoryMapper.selectById(courseBase.getSt());
        courseBaseInfoDto.setStName(courseCategoryBySt.getName());
        CourseCategory courseCategoryByMt = courseCategoryMapper.selectById(courseBase.getMt());
        courseBaseInfoDto.setMtName(courseCategoryByMt.getName());

        return courseBaseInfoDto;

    }
    @Transactional
    @Override
    public CourseBaseInfoDto updateCourseBase(Long companyId, EditCourseDto editCourseDto) {
        //拿到课程 id
        Long courseId = editCourseDto.getId();
        //查询课程信息
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if(courseBase == null){
            XueChengPlusException.cast("课程不存在");
        }
        //本机构只能修改本机构的课程
        if(!companyId.equals(courseBase.getCompanyId())){
            XueChengPlusException.cast("本机构只能修改本机构的课程");
        }
        //封装数据
        BeanUtils.copyProperties(editCourseDto,courseBase);
        //修改时间
        courseBase.setChangeDate(LocalDateTime.now());
        //更新数据库
        int i=courseBaseMapper.updateById(courseBase);
        if(i<=0){
            XueChengPlusException.cast("修改课程失败");
        }
        //查询课程信息
        CourseBaseInfoDto courseBaseInfo = this.getCourseBaseInfo(courseId);
        return courseBaseInfo;
    }

}
