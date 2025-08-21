package com.ss.springboot1.book.controller;

import com.ss.springboot1.pojo.Bookk;
import com.ss.springboot1.book.service.BookService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/books")
@Slf4j
public class BookController {

    @Resource
    private BookService bookService;

    @GetMapping("/findAll")
    public List<Bookk> findAll() {
        log.info("[findAll] 收到查询所有书籍请求，开始处理");
        List<Bookk> result = bookService.findAll();
        log.info("[findAll] 查询完成，返回书籍总数：{}", result != null ? (result).size() : "0");
        return result;
    }


    @PostMapping("/findById")
    public Bookk findById(@RequestBody Bookk bookk) {
        Integer id = bookk.getId();
        log.info("[findById] 收到查询书籍请求，入参：id={}", id);
        Bookk book = bookService.findById(id);
        if (book == null) {
            log.warn("[findById] 未查询到id={}的书籍", id);
        } else {
            log.info("[findById] 查询成功，书籍信息：{}", book);
        }
        return book;
    }


    @PostMapping("/update")
    public Integer update(@RequestBody Bookk book) {
        log.info("[update] 收到修改书籍请求，入参：{}", book);
        int message = bookService.update(book);
        if (message == 1) {
            log.info("[update] 修改成功，受影响的书籍id：{}", book.getId());
            return book.getId();
        } else {
            log.warn("[update] 修改失败，数据库未执行更新，book={}", book);
            return -1;
        }

    }


    @DeleteMapping("/deleteById")
    public Integer deleteById(@RequestBody Bookk book) {
        Integer id = book.getId();
        log.info("[deleteById] 收到删除书籍请求，入参：id={}", book.getId());
        int message = bookService.deleteById(id);
        if (message == 1) {
            log.info("[deleteById] 删除成功，已删除id={}的书籍", id);
            return id;
        } else {
            log.warn("[deleteById] 删除失败，数据库未执行删除，id={}", id);
            return -1;
        }

    }


    @PostMapping("/insert")
    public Bookk insert(@RequestBody Bookk book) {
        log.info("[insert] 收到添加书籍请求，入参：{}", book);
        int result = bookService.insert(book);
        if (result == 1) {
            log.info("[insert] 添加成功，新书籍信息：{}", book);
            return book;
        } else {
            log.warn("[insert] 添加失败，数据库未执行插入，book={}", book);
            return null;
        }
    }

}