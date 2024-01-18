package com.learnincode.content.controller;


import com.learnincode.content.model.dto.CourseCategoryTreeDto;
import com.learnincode.content.service.CourseCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/course-category")
public class CourseCategoryController {

    @Autowired
    private CourseCategoryService categoryService;

    @GetMapping("/tree-nodes")
    public List<CourseCategoryTreeDto> getCourseCategories(){

        return categoryService.queryTreeNodes("1");
    }

}
