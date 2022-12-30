package com.example.oauth2servertempleate.service;

import com.example.oauth2servertempleate.entity.Userr;
import com.example.oauth2servertempleate.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service 
@Transactional // Đây là transactional của JPA, nó sẽ tự động commit khi thực hiện xong các thao tác với database
// Lơp này là Bean, nó sẽ được Spring Security sử dụng để load user từ database để xác thực và nó cũng tạo ra user mặc định để lưu vào database nếu chưa có user nào trong database
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository; // Lớp này sẽ được Spring tự động tạo ra instance để sử dụng, nó sẽ được sử dụng để truy vấn dữ liệu từ database

    @Bean
    // Hàm này sẽ được Spring tự động tạo ra instance để sử dụng, nó sẽ được sử dụng để mã hóa password
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(11); // Mã hóa password bằng thuật toán BCrypt với độ dài 11
    }

    @Override
    // Hàm này sẽ được Spring Security sử dụng để load user từ database để xác thực
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Userr user = userRepository.findByEmail(email); // Truy vấn dữ liệu từ database thông qua email
        if(user == null) { // Nếu không tìm thấy user thì sẽ báo lỗi
            throw  new UsernameNotFoundException("No Userr Found"); // Báo lỗi
        }
        return new org.springframework.security.core.userdetails.User( // Trả về user đã được xác thực
                user.getEmail(),
                user.getPassword(),
                user.isEnabled(),
                true, 
                true,
                true,
                getAuthorities(List.of(user.getRole())) // Lấy danh sách quyền của user
        );
    }

    // Hàm này sẽ được sử dụng để lấy danh sách quyền của user
    private Collection<? extends GrantedAuthority> getAuthorities(List<String> roles) {
        List<GrantedAuthority>  authorities = new ArrayList<>(); // Khởi tạo danh sách quyền
        for(String role: roles) {
            authorities.add(new SimpleGrantedAuthority(role)); // Thêm quyền vào danh sách
        }
        return authorities;
    }


    @Bean 
    // Hàm này sẽ được Spring tự động tạo ra instance để sử dụng, nó sẽ được sử dụng để tạo ra user mặc định để lưu vào database nếu chưa có user nào trong database
	public void userDetailsService() {
        if(userRepository.count() == 0) { // Nếu chưa có user nào trong database thì sẽ tạo ra user mặc định
            Userr user = new Userr();
            user.setPassword(passwordEncoder().encode("12345")); // Mã hóa password và đặt password cho user
        
            user.setRole("USER"); // Đặt quyền cho user
    
            user.setEmail( "minh@gmail.com"); // Đặt email cho user
            
            userRepository.save(user); // Lưu user vào database
        }

      
	}
}
