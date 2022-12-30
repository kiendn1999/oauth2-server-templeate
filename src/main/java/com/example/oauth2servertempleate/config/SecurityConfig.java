package com.example.oauth2servertempleate.config;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;





@Configuration(proxyBeanMethods = false)
// Đây là class cấu hình cho Spring Security
public class SecurityConfig {


	@Autowired
	private  PasswordEncoder passwordEncoder;

	@Bean 
	@Order(Ordered.HIGHEST_PRECEDENCE) // Đánh dấu độ ưu tiên của Bean này là cao nhất
	// Hàm này dùng để cấu hình cho Spring Security
	public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http)
			throws Exception {
		OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http); // Cấu hình mặc định cho Spring Security
		http.getConfigurer(OAuth2AuthorizationServerConfigurer.class) 
			.oidc(Customizer.withDefaults());	// Enable OpenID Connect 1.0
		http
			// Redirect to the login page when not authenticated from the
			// authorization endpoint
			.exceptionHandling((exceptions) -> exceptions.authenticationEntryPoint( new LoginUrlAuthenticationEntryPoint("/login"))
			)
			// Accept access tokens for User Info and/or Client Registration
			.oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt);

		return http.formLogin(Customizer.withDefaults()).build(); // Cho phép sử dụng Form Login
	}


	// Hàm này dùng đăng ký các Client được phép truy cập vào Authorization Server
	@Bean 
	public RegisteredClientRepository registeredClientRepository() {
		RegisteredClient registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
				.clientId("api-client") // Client ID là ID của Máy Chủ Client dùng để xác thực với Authorization Server
				.clientSecret(passwordEncoder.encode("secret")) // Client Secret là khóa bí mật của Client dùng để xác thực với Authorization Server
 				.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)// Phương thức xác thực Client 
				.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE) // Cho phép sử dụng Authorization Code Grant
				.authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN) // Cho phép sử dụng Refresh Token
				.authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS) // Cho phép sử dụng Client Credentials Grant
				.redirectUri("http://127.0.0.1:8080/login/oauth2/code/api-client-oidc") // Địa chỉ của Client để nhận Authorization Code
				.redirectUri("http://127.0.0.1:8080/authorized")   // Địa chỉ của Client để nhận Access Token
				.scope(OidcScopes.OPENID) // Yêu cầu Client cung cấp thông tin OpenID
				.scope("message.read") // Những phạm vi mà Client được phép sử dụng để truy cập vào Resource Server, ở đây là được phép chỉ đọc dữ liệu từ Resource Server
				.clientSettings(ClientSettings.builder().requireAuthorizationConsent(true).build()) // Yêu cầu Client phải xác nhận trước khi được cấp quyền truy cập vào Resource Server
				.build();

		return new InMemoryRegisteredClientRepository(registeredClient); // Lưu thông tin Client vào bộ nhớ trong của server
	}

	@Bean 
	
	public JWKSource<SecurityContext> jwkSource() {
		KeyPair keyPair = generateRsaKey();
		RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
		RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
		RSAKey rsaKey = new RSAKey.Builder(publicKey)
				.privateKey(privateKey)
				.keyID(UUID.randomUUID().toString())
				.build();
		JWKSet jwkSet = new JWKSet(rsaKey);
		return new ImmutableJWKSet<>(jwkSet);
	}

	// Hàm này dùng để tạo khóa RSA
	private static KeyPair generateRsaKey() { 
		KeyPair keyPair;
		try {
			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
			keyPairGenerator.initialize(2048);
			keyPair = keyPairGenerator.generateKeyPair();
		}
		catch (Exception ex) {
			throw new IllegalStateException(ex);
		}
		return keyPair;
	}

	@Bean 
	// Hàm này dùng để giải mã Access Token từ Client
	public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
		return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
	}

	@Bean 
	// Hàm này để tạo ra địa chỉ mới của Authorization Server nếu không muốn sử dụng địa chỉ chỉ mặc định
	public AuthorizationServerSettings authorizationServerSettings() {
		return AuthorizationServerSettings.builder().issuer("http://localhost:9000").build();
	}

}
