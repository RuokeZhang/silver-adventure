package com.xuecheng.content.api;

import com.xuecheng.content.model.po.CourseTeacher;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public class CourseTeacherController {
    @ApiOperation("新增教师")
    @PostMapping("/courseTeacher")
    public CourseTeacher createCourseTeacher(@RequestBody CourseTeacher courseTeacher){
        //机构id，由于认证系统没有上线暂时硬编码
        Long companyId = 1232141425L;
        return null;
    }
}
