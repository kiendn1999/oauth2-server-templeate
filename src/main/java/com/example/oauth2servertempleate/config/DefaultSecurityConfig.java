package  com.example.oauth2servertempleate.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import com.example.oauth2servertempleate.service.CustomAuthenticationProvider;

@EnableWebSecurity // Đánh dấu lớp này là 1 lớp cấu hình bảo mật
@Configuration(proxyBeanMethods = false) // Đánh dấu lớp này là 1 lớp cấu hình, nó là 1 Bean được Spring quản lý và tự động tạo ra instance của lớp này để sử dụng trong các lớp khác
public class DefaultSecurityConfig {

    @Autowired
    private CustomAuthenticationProvider customAuthenticationProvider; // Lớp này dùng để xác thực user từ Database

    @Bean 
	// @Order(2)
	// Hàm này dùng để cấu hình bảo mật cho máy chủ xác thực, nó dùng để cấu hình các trang mà máy chủ xác thực sẽ cho phép truy cập mà không cần xác thực
	public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http)
			throws Exception {
		http
			.authorizeHttpRequests((authorize) -> authorize
				.anyRequest().authenticated() // Tất cả các trang đều cần xác thực
			)
			// Form login handles the redirect to the login page from the
			// authorization server filter chain
			.formLogin(Customizer.withDefaults()); // Cấu hình cho máy chủ xác thực sử dụng form login để xác thực user

		return http.build(); // Trả về 1 SecurityFilterChain

	}

	// Hàm này để xác thực user từ Database để Máy chủ xác thực có thể cấp token cho Client
    @Autowired // Đánh dấu hàm này là 1 hàm được Spring tự động gọi để thực thhi
    public void bindAuthenticationProvider(AuthenticationManagerBuilder authenticationManagerBuilder) {
        authenticationManagerBuilder.authenticationProvider(customAuthenticationProvider); // Đăng ký lớp xác thực user vào AuthenticationManagerBuilder
    }
}
