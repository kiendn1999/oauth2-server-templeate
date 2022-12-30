package com.example.oauth2servertempleate.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
// Lombok là 1 thư viện dùng các annotation để tự động tạo ra các phương thức getter, setter, constructor, toString, hashcode, equals, ...
import lombok.Data;
import lombok.Setter; 


@Entity // Đánh dấu lớp này là 1 lớp Entity, nó được dùng để tương tác với database
@Data 
@Setter // Lombok sẽ tự động tạo ra các phương thức setter
public class Userr { // Lớp này sẽ tương tác với bảng userr trong database, tên lớp này sẽ cùng tên với tên bảng trong database

    @Id // Đánh dấu trường này là 1 trường id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Đánh dấu trường này là 1 trường id tự tăng
    private Long id;
    private String firstName;
    private String lastName;

    @Column(length = 60) // Đánh dấu trường này là 1 trường có độ dài là 60
    private String email;

    @Column(length = 225) // Đánh dấu trường này là 1 trường có độ dài là 225
    private String password;

    private String role; // Quyền của user, có 2 quyền là USER và ADMIN
    private boolean enabled = false; // Đánh dấu trường này là 1 trường có giá trị mặc định là false
}
