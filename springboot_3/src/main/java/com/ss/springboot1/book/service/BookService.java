package com.ss.springboot1.book.service;


import com.ss.springboot1.pojo.Bookk;


import java.util.List;

public interface BookService {
    Integer insert(Bookk book);
    Integer deleteById(Integer id);
    Integer update(Bookk  book );
    Bookk findById(Integer id);
    List<Bookk> findAll();
    Integer findByName(String name);
}
