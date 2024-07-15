package com.xuecheng.content.api;

import com.xuecheng.base.exception.ValidationGroups;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
public class CourseTeacherController {
    @Autowired
    private CourseTeacherService courseTeacherService;

    @ApiOperation("新增教师")
    @PostMapping("/courseTeacher")
    public CourseTeacher createCourseTeacher(@RequestBody @Validated({ValidationGroups.Insert.class}) CourseTeacher courseTeacher){
        courseTeacherService.addCourseTeacher(courseTeacher);
        return courseTeacher;
    }

    @ApiOperation("修改教师")
    @PutMapping("/courseTeacher")
    public CourseTeacher updateCourseTeacher(@RequestBody @Validated({ValidationGroups.Update.class}) CourseTeacher courseTeacher){
        courseTeacherService.updateCourseTeacher(courseTeacher);
        return courseTeacher;
    }

    @ApiOperation("查询教师")
    @GetMapping("/courseTeacher/list/{courseId}")
    public List<CourseTeacher> getTeacherList(@PathVariable Long courseId){
        return courseTeacherService.getTeacherList(courseId);
    }

    @ApiOperation("删除教师")
    @DeleteMapping("courseTeacher/course/{courseId}/{Id}")
    public void deleteCourseTeacher(@PathVariable Long courseId, @PathVariable Long Id){
        courseTeacherService.deleteCourseTeacher(courseId, Id);
    }

}
