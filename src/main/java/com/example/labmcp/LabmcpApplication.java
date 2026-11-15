package com.example.labmcp;

import com.example.labmcp.service.TareaToolsService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Punto de entrada del servidor MCP de tareas.
 *
 * <p>Levanta Spring Boot con el starter MCP de Spring AI (Streamable HTTP en
 * {@code http://localhost:8088/mcp}) y registra las herramientas de
 * {@link TareaToolsService}.</p>
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
	 * Expone los métodos {@code @Tool} del servicio como herramientas MCP.
	 * El autoconfigurador de Spring AI toma este bean y publica cada método
	 * en {@code tools/list}.
	 *
	 * @param tools servicio con las herramientas de tareas
	 * @return proveedor de callbacks de herramientas
	 */
	@Bean
	public ToolCallbackProvider tareaTools(TareaToolsService tools) {
		return MethodToolCallbackProvider.builder()
				.toolObjects(tools)
				.build();
	}
}
