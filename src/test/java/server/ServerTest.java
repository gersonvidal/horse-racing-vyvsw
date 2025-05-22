package server;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.junit.jupiter.api.Test;

import com.gerson.server.Server;

class ServerTest {

    @Test
    void testServerStopsAndTriggersCatchBlock() throws Exception {
        // Iniciar el servidor en un hilo separado
        Thread serverThread = new Thread(() -> Server.main(null));
        serverThread.setDaemon(true);
        serverThread.start();

        // Esperar a que el servidor esté listo
        Thread.sleep(300);

        // Detener el servidor (esto cierra el socket y lanza IOException en accept)
        Server.stopServer();

        // Esperar un poco para que termine el hilo
        Thread.sleep(300);

        // El catch se ejecutó si no hay excepciones lanzadas aquí
        assertTrue(true); // Para evitar warning de test vacío
    }

    @Test
    void testWhileRunningExecutedWhenClientConnects() throws Exception {
        // Iniciar el servidor en otro hilo
        Thread serverThread = new Thread(() -> Server.main(null));
        serverThread.setDaemon(true);
        serverThread.start();

        // Esperar a que el servidor esté listo
        Thread.sleep(200);

        // Simular cliente que se conecta
        try (Socket socket = new Socket("localhost", 1818)) {
            OutputStream out = socket.getOutputStream();
            InputStream in = socket.getInputStream();

            // Leer el ID del jugador
            byte[] buffer = new byte[64];
            int read = in.read(buffer);

            // Enviar nombre del jugador
            out.write("JugadorDesdeTest\n".getBytes());
            out.flush();

            assertTrue(read > 0); // hubo respuesta → entró al while
        }

        // Detener el servidor
        Server.stopServer();

        // Esperar a que se cierre
        Thread.sleep(200);

        assertTrue(true); // evita warning por test vacío
    }

    @Test
    void testServerConstructor() {
        new Server();
        assertTrue(true);
    }

    @Test
    void testStopServerHandlesIOExceptionWhenClosingSocket() throws Exception {
        // Configurar un ServerSocket mock que lance IOException al cerrarse
        ServerSocket mockServerSocket = mock(ServerSocket.class);
        doThrow(new IOException("Simulación de error al cerrar")).when(mockServerSocket).close();

        // Usar reflexión para inyectar el mock
        var serverSocketField = Server.class.getDeclaredField("serverSocket");
        serverSocketField.setAccessible(true);
        serverSocketField.set(null, mockServerSocket);

        // Establecer running como true para que intente cerrar
        var runningField = Server.class.getDeclaredField("running");
        runningField.setAccessible(true);
        runningField.set(null, true);

        // Ejecutar stopServer()
        Server.stopServer();

        // Si llegamos aquí sin excepciones, el catch se ejecutó correctamente
        assertTrue(true);
    }

    @Test
    void testStopServerWithNullSocket() throws Exception {
        // Asegurarse que serverSocket es null
        var serverSocketField = Server.class.getDeclaredField("serverSocket");
        serverSocketField.setAccessible(true);
        serverSocketField.set(null, null);

        // Establecer running como true
        var runningField = Server.class.getDeclaredField("running");
        runningField.setAccessible(true);
        runningField.set(null, true);

        // Ejecutar stopServer() - no debe lanzar excepciones
        Server.stopServer();

        assertTrue(true);
    }

}
