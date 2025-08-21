package com.ss.springboot1.book.mapper;

import com.ss.springboot1.pojo.Bookk;
import org.apache.ibatis.annotations.Mapper;


import java.util.List;

@Mapper
public interface BookMapper {

   int insertBook(Bookk book);

   int deleteById(Integer id);

   int update(Bookk book);

   Bookk findById(Integer id);

   List<Bookk> findAll();

   int findByName(String name);
}
