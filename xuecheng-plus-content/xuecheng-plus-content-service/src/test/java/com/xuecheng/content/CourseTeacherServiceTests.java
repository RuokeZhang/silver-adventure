package com.xuecheng.content;

import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class CourseTeacherServiceTests {
    @Autowired
    private CourseTeacherService courseTeacherService;

    @Test
    public void testGetTeacher() {
        CourseTeacher courseTeacher=courseTeacherService.getCourseTeacher(8L);
        Assertions.assertNotNull(courseTeacher);
        System.out.println(courseTeacher);
    }
    @Test
    public void testAddTeacher() {
        CourseTeacher courseTeacher=new CourseTeacher();
        courseTeacher.setTeacherName("hou");
        courseTeacher.setPosition("大王");
        CourseTeacher newTeacher=courseTeacherService.saveCourseTeacher(courseTeacher);
        CourseTeacher returnedTeacher=courseTeacherService.getCourseTeacher(newTeacher.getId());
        Assertions.assertNotNull(returnedTeacher);
        System.out.println(returnedTeacher);

    }
    @Test
    public void testUpdateTeacher() {
        CourseTeacher courseTeacher=new CourseTeacher();
        courseTeacher.setTeacherName("zhang");
        courseTeacher.setPosition("大荒地");
        courseTeacher.setId(8L);
        CourseTeacher newTeacher=courseTeacherService.saveCourseTeacher(courseTeacher);
        CourseTeacher returnedTeacher=courseTeacherService.getCourseTeacher(newTeacher.getId());
        Assertions.assertNotNull(returnedTeacher);
        System.out.println(returnedTeacher);
    }
}
