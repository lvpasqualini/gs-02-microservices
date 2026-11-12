package com.github.lvpasqualini.ms_triagem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class MsTriagemApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsTriagemApplication.class, args);
	}

}
