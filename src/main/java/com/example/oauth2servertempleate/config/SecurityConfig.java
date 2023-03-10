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
// ????y l?? class c???u h??nh cho Spring Security
public class SecurityConfig {


	@Autowired
	private  PasswordEncoder passwordEncoder;

	@Bean 
	@Order(Ordered.HIGHEST_PRECEDENCE) // ????nh d???u ????? ??u ti??n c???a Bean n??y l?? cao nh???t
	// H??m n??y d??ng ????? c???u h??nh cho Spring Security
	public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http)
			throws Exception {
		OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http); // C???u h??nh m???c ?????nh cho Spring Security
		http.getConfigurer(OAuth2AuthorizationServerConfigurer.class) 
			.oidc(Customizer.withDefaults());	// Enable OpenID Connect 1.0
		http
			// Redirect to the login page when not authenticated from the
			// authorization endpoint
			.exceptionHandling((exceptions) -> exceptions.authenticationEntryPoint( new LoginUrlAuthenticationEntryPoint("/login"))
			)
			// Accept access tokens for User Info and/or Client Registration
			.oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt);

		return http.formLogin(Customizer.withDefaults()).build(); // Cho ph??p s??? d???ng Form Login
	}


	// H??m n??y d??ng ????ng k?? c??c Client ???????c ph??p truy c???p v??o Authorization Server
	@Bean 
	public RegisteredClientRepository registeredClientRepository() {
		RegisteredClient registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
				.clientId("messaging-client") // Client ID l?? ID c???a M??y Ch??? Client d??ng ????? x??c th???c v???i Authorization Server
				.clientSecret(passwordEncoder.encode("secret")) // Client Secret l?? kh??a b?? m???t c???a Client d??ng ????? x??c th???c v???i Authorization Server
 				.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)// Ph????ng th???c x??c th???c Client 
				.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE) // Cho ph??p s??? d???ng Authorization Code Grant
				.authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN) // Cho ph??p s??? d???ng Refresh Token
				.authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS) // Cho ph??p s??? d???ng Client Credentials Grant
				.redirectUri("http://127.0.0.1:8080/login/oauth2/code/messaging-client-oidc") // ?????a ch??? c???a Client ????? nh???n Authorization Code
				.redirectUri("http://127.0.0.1:8080/authorized")   // ?????a ch??? c???a Client ????? nh???n Access Token
				.scope(OidcScopes.OPENID) // Y??u c???u Client cung c???p th??ng tin OpenID
				// .scope(OidcScopes.PROFILE) // Y??u c???u Client cung c???p th??ng tin Profile
				.scope("message.read") // Nh???ng ph???m vi m?? Client ???????c ph??p s??? d???ng ????? truy c???p v??o Resource Server, ??? ????y l?? ???????c ph??p ch??? ?????c d??? li???u t??? Resource Server
				.clientSettings(ClientSettings.builder().requireAuthorizationConsent(true).build()) // Y??u c???u Client ph???i x??c nh???n tr?????c khi ???????c c???p quy???n truy c???p v??o Resource Server
				.build();

		return new InMemoryRegisteredClientRepository(registeredClient); // L??u th??ng tin Client v??o b??? nh??? trong c???a server
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

	// H??m n??y d??ng ????? t???o kh??a RSA
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
	// H??m n??y d??ng ????? gi???i m?? Access Token t??? Client
	public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
		return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
	}

	@Bean 
	// H??m n??y ????? t???o ra ?????a ch??? m???i c???a Authorization Server n???u kh??ng mu???n s??? d???ng ?????a ch??? ch??? m???c ?????nh
	public AuthorizationServerSettings authorizationServerSettings() {
		return AuthorizationServerSettings.builder().issuer("http://localhost:9000").build();
	}

}
