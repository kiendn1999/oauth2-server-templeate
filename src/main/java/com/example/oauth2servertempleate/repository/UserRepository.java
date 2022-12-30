package com.example.oauth2servertempleate.repository;


import com.example.oauth2servertempleate.entity.Userr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository // Đánh dấu lớp này là 1 lớp Repository, nó là 1 Bean được Spring quản lý và tự động tạo ra instance của lớp này để sử dụng trong các lớp khác 
// JPA là 1 lớp dùng để truy vấn dữ liệu từ database, nó sẽ tự động tạo ra các câu lệnh SQL để truy vấn dữ liệu
// Lớp UserRepository kế thừa từ JpaRepository, nó cũng sẽ tự động tạo ra các câu lệnh SQL để truy vấn dữ liệu
public interface UserRepository extends JpaRepository<Userr,Long> { 
    Userr findByEmail(String email); // Hàm này dùng để tìm kiếm user theo email
}
