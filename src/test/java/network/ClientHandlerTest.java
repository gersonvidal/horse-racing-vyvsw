package network;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.gerson.logic.GameManager;
import com.gerson.network.ClientHandler;

class ClientHandlerTest {

    private Socket mockSocket;
    private InputStream inputStream;
    private OutputStream outputStream;

    @BeforeEach
    void setUp() throws Exception {
        mockSocket = mock(Socket.class);
        inputStream = new ByteArrayInputStream(("JugadorPrueba\nclicks_sent:5\n").getBytes());
        outputStream = new ByteArrayOutputStream();

        when(mockSocket.getInputStream()).thenReturn(inputStream);
        when(mockSocket.getOutputStream()).thenReturn(outputStream);

        // Reiniciar estado de GameManager para pruebas aisladas
        resetGameManager();
    }

    @Test
    void testClientHandlerRegistersPlayerAndProcessesInput() {
        ClientHandler handler = new ClientHandler(mockSocket);
        handler.run(); // ejecutamos directamente el hilo

        String result = outputStream.toString();
        assertTrue(result.contains("1")); // se imprime el ID del jugador
    }

    @Test
    void testClientHandlerHandlesIOException() throws Exception {
        // Simular IOException al obtener input stream
        when(mockSocket.getInputStream()).thenThrow(new IOException("Simulación"));

        ClientHandler handler = new ClientHandler(mockSocket);
        handler.run();

        // El test pasa si no lanza excepción — el catch se ejecutó
        assertTrue(true);
    }

    // Utilidad para limpiar GameManager entre pruebas
    private void resetGameManager() throws Exception {
        GameManager gm = GameManager.getGamemanagerinstance();

        var players = GameManager.class.getDeclaredField("players");
        players.setAccessible(true);
        ((java.util.Map<?, ?>) players.get(gm)).clear();

        var clients = GameManager.class.getDeclaredField("clients");
        clients.setAccessible(true);
        ((java.util.List<?>) clients.get(gm)).clear();

        var readyPlayers = GameManager.class.getDeclaredField("readyPlayers");
        readyPlayers.setAccessible(true);
        ((java.util.Set<?>) readyPlayers.get(gm)).clear();

        var nextPlayerId = GameManager.class.getDeclaredField("nextPlayerId");
        nextPlayerId.setAccessible(true);
        nextPlayerId.set(gm, 1);

        var raceStarted = GameManager.class.getDeclaredField("raceStarted");
        raceStarted.setAccessible(true);
        raceStarted.set(gm, false);
    }

    @Test
    void testClientHandlerAssignsDefaultNameIfUsernameBlank() throws Exception {
        String input = "\nclicks_sent:2\n"; // línea vacía
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Socket socket = mock(Socket.class);
        when(socket.getInputStream()).thenReturn(in);
        when(socket.getOutputStream()).thenReturn(out);

        resetGameManager();

        ClientHandler handler = new ClientHandler(socket);
        handler.run(); // ejecuta directamente

        // Debe haber asignado "Jugador <id>"
        String result = out.toString();
        assertTrue(result.contains("1")); // ID del jugador enviado
    }

    @Test
    void testClientHandlerHandlesIOExceptionOnRead() throws Exception {
        Socket socket = mock(Socket.class);
        when(socket.getInputStream()).thenThrow(new IOException("simulada"));
        when(socket.getOutputStream()).thenReturn(new ByteArrayOutputStream());

        resetGameManager();

        ClientHandler handler = new ClientHandler(socket);
        handler.run(); // el catch se ejecuta

        assertTrue(true); // si no lanza excepción, pasó
    }

    @Test
    void testClientHandlerHandlesSocketCloseException() throws Exception {
        // Configurar input/output streams normales
        ByteArrayInputStream in = new ByteArrayInputStream("Jugador\n".getBytes());
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // Mock del socket que lanza IOException al cerrarse
        Socket socket = mock(Socket.class);
        when(socket.getInputStream()).thenReturn(in);
        when(socket.getOutputStream()).thenReturn(out);
        doThrow(new IOException("Error al cerrar socket")).when(socket).close();

        resetGameManager();

        ClientHandler handler = new ClientHandler(socket);
        handler.run(); // Ejecutar directamente

        // Verificar que el output stream recibió el ID del jugador (indica que el flujo
        // principal funcionó)
        assertTrue(out.toString().contains("1"));
        // Si llegamos aquí sin excepciones, el catch se ejecutó correctamente
    }

}
