package com.example.oauth2servertempleate.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
// Lớp này để xác thực user từ Database để Máy chủ xác thực có thể cấp token cho Client
public class CustomAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder; // Lớp này để mã hóa password

    @Override
    // Hàm này để xác thực user từ Database để Máy chủ xác thực có thể cấp token cho Client
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName(); // Lấy username từ client
        String password = authentication.getCredentials().toString(); // Lấy password từ client
        UserDetails user= customUserDetailsService.loadUserByUsername(username); // Lấy thông tin user từ Database
        return checkPassword(user,password); // Kiểm tra password
    }

    // Hàm này để kiểm tra password
    private Authentication checkPassword(UserDetails user, String rawPassword) {
        if(passwordEncoder.matches(rawPassword, user.getPassword())) { // Kiểm tra password đã mã hóa từ Database với password đã mã hóa từ client
            return new UsernamePasswordAuthenticationToken(user.getUsername(), 
                    user.getPassword(),
                    user.getAuthorities());
        }
        else {
            throw new BadCredentialsException("Bad Credentials"); // Nếu password không đúng thì trả về lỗi
        }
    }

    @Override
    // Hàm này để kiểm tra xem AuthenticationProvider có hỗ trợ kiểu Authentication này không
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
