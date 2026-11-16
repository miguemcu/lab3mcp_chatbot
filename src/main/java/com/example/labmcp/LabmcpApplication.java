package com.example.labmcp;

import com.example.labmcp.service.SupermercadoToolsService;
import com.example.labmcp.service.TareaToolsService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Punto de entrada del servidor MCP de tareas y del supermercado.
 *
 * <p>Levanta Spring Boot con el starter MCP de Spring AI (Streamable HTTP en
 * {@code http://localhost:8088/mcp}) y registra las herramientas de
 * {@link TareaToolsService} y {@link SupermercadoToolsService}.</p>
 */
@SpringBootApplication
public class LabmcpApplication {

	/**
	 * Arranca la aplicación.
	 *
	 * @param args argumentos de línea de comandos
	 */
	public static void main(String[] args) {
		SpringApplication.run(LabmcpApplication.class, args);
	}

	/**
	 * Expone los métodos {@code @Tool} de los servicios como herramientas MCP.
	 * El autoconfigurador de Spring AI toma este bean y publica cada método
	 * en {@code tools/list}.
	 *
	 * @param tareas       herramientas de tareas
	 * @param supermercado herramientas del supermercado
	 * @return proveedor de callbacks de herramientas
	 */
	@Bean
	public ToolCallbackProvider herramientasMcp(TareaToolsService tareas, SupermercadoToolsService supermercado) {
		return MethodToolCallbackProvider.builder()
				.toolObjects(tareas, supermercado)
				.build();
	}
}
