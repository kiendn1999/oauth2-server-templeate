package com.example.oauth2servertempleate.controller;

// create Hello Controller

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController // Đánh dấu lớp này là 1 lớp Controller, nó được dùng để rest api
public class HelloController {

    @GetMapping("/") // Đánh dấu hàm này sẽ được gọi khi có request đến đường dẫn "/"
    // Nếu xác thực thành công, máy chủ xác thực sẽ trả về dữ liệu cho Client với dữ liệu là 1 chuỗi "Hello World"
    public String hello() {
        return "Hello World";
    }
}