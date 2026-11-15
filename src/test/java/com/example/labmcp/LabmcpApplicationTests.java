package com.example.labmcp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Prueba de humo: verifica que el contexto de Spring (JPA + servidor MCP) arranca.
 *
 * <p>Requiere MySQL activo con la base {@code labmcp} y la variable
 * de entorno {@code PWD_UDEA} definida.</p>
 */
@SpringBootTest
class LabmcpApplicationTests {

	/** El contexto carga sin errores. */
	@Test
	void contextLoads() {
	}

}
