package com.xuecheng.content.service;

import com.xuecheng.content.model.po.CourseTeacher;

public interface CourseTeacherService {
    public CourseTeacher saveCourseTeacher(CourseTeacher courseTeacher);
    public CourseTeacher getCourseTeacher(Long teacherId);
}
