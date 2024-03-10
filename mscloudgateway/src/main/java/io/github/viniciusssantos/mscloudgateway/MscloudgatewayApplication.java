package io.github.viniciusssantos.mscloudgateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableEurekaClient // Permite que este aplicativo seja um cliente Eureka
@EnableDiscoveryClient // Ativa a descoberta de serviços para este aplicativo
public class MscloudgatewayApplication {

	public static void main(String[] args) {
		// Inicia a aplicação Spring Boot
		SpringApplication.run(MscloudgatewayApplication.class, args);
	}

	// Configuração das rotas para o gateway
	@Bean
	public RouteLocator routes(RouteLocatorBuilder builder) {
		return builder
				.routes()
				// Define uma rota para qualquer requisição que comece com "/clientes/**"
				// e redireciona para "lib://msclientes"
				.route(r -> r.path("/clientes/**").uri("lb://msclientes"))
				.route(r -> r.path("/cartoes/**").uri("lb://mscartoes"))
				.route(r -> r.path("/avaliacoes-credito/**").uri("lb://msavaliadorcredito"))
				.build(); // Constrói as rotas definidas
	}

}
