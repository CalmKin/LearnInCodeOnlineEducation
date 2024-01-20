package com.learnincode.content;


import com.learnincode.content.mapper.CourseCategoryMapper;
import com.learnincode.content.model.dto.CourseCategoryTreeDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class CourseCategoryMapperTest {

    @Autowired
    CourseCategoryMapper mapper;

    @Test
    public void testMapper()
    {
        List<CourseCategoryTreeDto> treeDtoList = mapper.selectTreeNodes("1");
        System.out.println(treeDtoList);
    }


}
