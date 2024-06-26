package com.example.EBook_Management_BE.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.security.reactive.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.example.EBook_Management_BE.filters.JwtTokenFilter;

import lombok.RequiredArgsConstructor;

import java.util.List;

@SuppressWarnings("deprecation")
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableWebMvc
@RequiredArgsConstructor
public class WebSecurityConfig {
	private final JwtTokenFilter jwtTokenFilter;

	@Value("${api.prefix}")
	private String apiPrefix;

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
				.authorizeHttpRequests(requests -> {
					requests.requestMatchers(
							String.format("%s/user/register", apiPrefix),
							String.format("%s/user/login", apiPrefix),
							String.format("%s/user/login", apiPrefix),

							// healthcheck
							String.format("%s/healthcheck/**", apiPrefix)

				).permitAll().requestMatchers(HttpMethod.GET, String.format("%s/role**", apiPrefix)).permitAll()

							.requestMatchers(HttpMethod.GET, String.format("%s/category", apiPrefix)).permitAll()

							.requestMatchers(HttpMethod.GET, String.format("%s/book/**", apiPrefix)).permitAll()

							.requestMatchers(HttpMethod.GET, String.format("%s/book/images/*", apiPrefix)).permitAll()

							.requestMatchers(HttpMethod.GET, String.format("%s/order/**", apiPrefix)).permitAll()

							.requestMatchers(HttpMethod.GET, String.format("%s/order_detail/**", apiPrefix)).permitAll()

							.anyRequest().authenticated();
				}).csrf(AbstractHttpConfigurer::disable);

		http.securityMatcher(String.valueOf(EndpointRequest.toAnyEndpoint()));
		return http.build();
	}

}
