package com.ss.springboot1.book.service.imp;

import com.ss.springboot1.book.mapper.BookMapper;
import com.ss.springboot1.pojo.Bookk;
import com.ss.springboot1.book.service.BookService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional
public class BookServiceImpl implements BookService {

    @Resource
    private BookMapper bookMapper;


    @Override
    public Integer insert(Bookk book) {
        log.info("开始处理添加书籍业务，书名{}", book);
        int nameCount=bookMapper.findByName(book.getName());
        if (nameCount == 1) {
            log.warn("[insert] 添加失败：书名「{}」已存在", book.getName());
            throw new IllegalArgumentException("书名已存在");
        }
        return bookMapper.insertBook(book);
    }

    @Override
    public Integer deleteById(Integer id) {
        log.info("开始处理删除书籍业务，ID：{}", id);
        if (bookMapper.findById(id) == null) {
            log.warn("[deleteById] 删除失败：id={}的书籍不存在", id);
            throw new IllegalArgumentException("id找不到");
        }
        return bookMapper.deleteById(id);
    }

    @Override
    public Integer update(Bookk book) {
        log.info("开始处理更新书籍业务，ID：{}", book.getId());
        Bookk existingBook = bookMapper.findById(book.getId());
        if (existingBook == null) {
            log.warn("[update] 修改失败：id={}的书籍不存在", book.getId());
            throw new IllegalArgumentException("id找不到");
        }
        if (book.getName() != null) existingBook.setName(book.getName());
        if (book.getPrice() != null) existingBook.setPrice(book.getPrice());
        if (book.getNum() != null) existingBook.setNum(book.getNum());
        return bookMapper.update(existingBook);
    }

    @Override
    public Bookk findById(Integer id) {
        log.info("开始处理查询书籍业务，ID：{}", id);
        return bookMapper.findById(id);
    }

    @Override
    public List<Bookk> findAll() {
        log.info("开始处理查询所有书籍业务");
        return bookMapper.findAll();
    }

    @Override
    public Integer findByName(String name) {
        log.info("开始处理查询书籍业务，Name：{}", name);
        return bookMapper.findByName(name);
    }
}