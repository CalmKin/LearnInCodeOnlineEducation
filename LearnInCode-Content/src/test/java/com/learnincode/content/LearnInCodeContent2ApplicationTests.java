package com.learnincode.content;

import com.learnincode.content.service.CourseBaseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class LearnInCodeContent2ApplicationTests {

    @Autowired
    CourseBaseService courseBaseService;

    @Test
    void testPageContent() {

    }

}
