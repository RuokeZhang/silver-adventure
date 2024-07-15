package com.xuecheng.content.service;

import com.xuecheng.content.model.po.CourseTeacher;

import java.util.List;

public interface CourseTeacherService {


    public CourseTeacher addCourseTeacher(CourseTeacher courseTeacher);
    public CourseTeacher getCourseTeacher(Long teacherId);
    CourseTeacher updateCourseTeacher(CourseTeacher courseTeacher);
    public List<CourseTeacher> getTeacherList(Long courseId);
    public void deleteCourseTeacher(Long courseId, Long Id);
}
