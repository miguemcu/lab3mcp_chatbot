package com.example.chatbot;

import java.time.Duration;
import java.util.List;

import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.LlmAgent;
import com.google.adk.tools.BaseTool;
import com.google.adk.tools.mcp.McpToolset;
import com.google.adk.tools.mcp.StreamableHttpServerParameters;
import com.google.adk.web.AdkWebServer;

/**
 * Chatbot del supermercado a domicilio: un agente Gemini de Google ADK que usa como
 * herramientas las que publica el servidor MCP (Spring AI) en {@value #MCP_URL}.
 *
 * <p>La clave de Gemini se lee de la variable de entorno {@code GOOGLE_API_KEY}
 * (o {@code GEMINI_API_KEY}); nunca va en el código.</p>
 */
public class SupermercadoAgent {

    private static final String MCP_URL = "http://localhost:8088/mcp";
    private static final Duration TIMEOUT_MCP = Duration.ofSeconds(30);
    private static final String MODELO = "gemini-flash-latest";

    private static final String INSTRUCCION = """
            Eres el asistente de un supermercado a domicilio. Hablas en español, de forma breve y amable.
            1. Antes de crear un pedido, identifica al cliente: pide nombre, teléfono y dirección y usa registrarCliente.
            2. Cuando pida productos, búscalos con buscarProductos y pregunta la cantidad de cada uno si no la dijo.
               Si hay varias opciones, muéstralas con su precio y deja que el cliente elija.
            3. Crea el pedido con crearPedido (una sola vez por compra) y agrega cada producto con agregarProducto.
            4. Antes de confirmar, muestra el resumen con verPedido (productos, cantidades, subtotales y total).
            5. Llama a confirmarPedido o cancelarPedido solo después de que el cliente lo confirme explícitamente.
            6. No inventes productos, precios ni existencias: usa solo lo que devuelven las herramientas.
               Si una herramienta devuelve un error (por ejemplo, stock insuficiente), explícaselo al cliente y ofrece alternativas.
            """;

    /** Agente raíz que sirve la interfaz web de ADK. */
    public static final BaseAgent ROOT_AGENT = crearAgente();

    /**
     * Levanta la interfaz web de ADK (puerto 8080 por defecto) con el agente del supermercado.
     *
     * @param args no se usan
     */
    public static void main(String[] args) {
        AdkWebServer.start(ROOT_AGENT);
    }

    /** Descubre las herramientas del servidor MCP y construye el agente con ellas. */
    private static BaseAgent crearAgente() {
        StreamableHttpServerParameters parametros = StreamableHttpServerParameters.builder()
                .url(MCP_URL)
                .timeout(TIMEOUT_MCP)
                .build();
        McpToolset toolset = new McpToolset(parametros);

        List<BaseTool> herramientas;
        try {
            herramientas = toolset.getTools(null).toList().blockingGet();
        } catch (RuntimeException e) {
            throw new IllegalStateException("No fue posible conectar con el servidor MCP en " + MCP_URL
                    + ". ¿Está corriendo el servidor Spring Boot?", e);
        }
        System.out.println("Herramientas MCP descubiertas: " + herramientas.size() + " "
                + herramientas.stream().map(BaseTool::name).toList());

        return LlmAgent.builder()
                .name("SupermercadoAgent")
                .model(MODELO)
                .description("Asistente de pedidos de un supermercado a domicilio")
                .instruction(INSTRUCCION)
                .tools(herramientas)
                .build();
    }
}
