package com.dingmk.consistency;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;

import com.dingmk.consistency.listener.InitListener;

@SpringBootApplication
@MapperScan("com.dingmk.consistency.mapper")
public class ConsistentApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConsistentApplication.class, args);
	}
	
	@Bean
	public ServletListenerRegistrationBean servletListenerRegistrationBean(){
		ServletListenerRegistrationBean servletListenerRegistrationBean = new ServletListenerRegistrationBean();
		servletListenerRegistrationBean.setListener(new InitListener());
		return servletListenerRegistrationBean;
	}
}
