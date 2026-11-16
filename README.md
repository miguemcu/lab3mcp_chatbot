# Servidor MCP de tareas y supermercado

Laboratorio de Arquitectura de Software (UdeA). Un servidor MCP con Spring Boot 3.4.7 y Spring AI 1.1.4 (Streamable HTTP en `http://localhost:8088/mcp`) sobre MySQL 8.4 publica 16 herramientas. Las consumen Claude Desktop y un chatbot propio con Google ADK.

| Grupo | Herramientas |
| --- | --- |
| Tareas (9) | `listarTareas`, `listarPendientes`, `listarCompletadas`, `contarTareas`, `buscarTareas`, `crearTarea`, `actualizarTarea`, `marcarCompletada`, `eliminarTarea` |
| Supermercado (7) | `buscarProductos`, `registrarCliente`, `crearPedido`, `agregarProducto`, `verPedido`, `confirmarPedido`, `cancelarPedido` |

Un pedido nace en `BORRADOR`. Al confirmarlo se descuenta el stock y al cancelarlo se repone. Los errores de negocio (stock insuficiente, estado inválido) llegan al cliente como `isError: true`.

## Requisitos

JDK 21, MySQL 8.4 y Node.js (para MCP Inspector y `mcp-remote`).

## Ejecución

```powershell
# 1. Base de datos (una vez)
Get-Content src/main/resources/schema.sql | mysql -u root -p
Get-Content src/main/resources/supermercado.sql | mysql -u root -p labmcp

# 2. Servidor MCP (puerto 8088); la contraseña de MySQL va en una variable de entorno
$env:PWD_UDEA = "<clave MySQL>"
.\mvnw.cmd spring-boot:run

# 3. Chatbot con Google ADK (otra terminal, puerto 8080)
$env:GOOGLE_API_KEY = "<clave de Google AI Studio>"
.\mvnw.cmd -f chatbot\pom.xml compile exec:java
```

Al arrancar, el chatbot imprime cuántas herramientas MCP descubrió. Luego se usa en `http://localhost:8080`, eligiendo el agente `SupermercadoAgent`.

## Otros clientes

- **MCP Inspector:** `npx @modelcontextprotocol/inspector`, transporte Streamable HTTP, URL `http://localhost:8088/mcp`.
- **Claude Desktop**, en `claude_desktop_config.json` (el servidor debe estar corriendo antes de abrir la app):

  ```json
  { "mcpServers": { "labmcp": { "command": "npx", "args": ["-y", "mcp-remote", "http://localhost:8088/mcp", "--allow-http"] } } }
  ```

## Pruebas

```powershell
.\mvnw.cmd clean test
```

Las pruebas de las herramientas son unitarias con Mockito y no necesitan MySQL. `contextLoads` sí necesita MySQL y `PWD_UDEA`.
