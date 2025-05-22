package logic;

import com.gerson.logic.GameManager;
import com.gerson.model.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.*;

class GameManagerTest {
    private GameManager gameManager;
    private PrintStream mockOutput;

    @BeforeEach
    void setUp() {
        gameManager = GameManager.getGamemanagerinstance();
        mockOutput = mock(PrintStream.class);
    }

    @Test
    void testRegisterPlayer() {
        Player player = gameManager.registerPlayer(mockOutput);
        assertNotNull(player);
        assertEquals(mockOutput, player.getOutput());
    }

    @Test
    void testNotifyNewPlayerBroadcastsPlayerList() {
        Player player = gameManager.registerPlayer(mockOutput);
        player.setName("Jugador1");

        gameManager.notifyNewPlayer(player);

        verify(mockOutput, atLeastOnce()).println(startsWith("players:"));
    }

    @Test
    void testProcessClientMessageReadyStartsCountdownIfAllReady() throws InterruptedException {
        Player p1 = gameManager.registerPlayer(mockOutput);
        Player p2 = gameManager.registerPlayer(mockOutput);
        p1.setName("Caballo1");
        p2.setName("Caballo2");

        gameManager.notifyNewPlayer(p1);
        gameManager.notifyNewPlayer(p2);

        gameManager.processClientMessage(p1, "ready");
        gameManager.processClientMessage(p2, "ready");

        Thread.sleep(3500); // espera a que inicie la carrera

        verify(mockOutput, atLeastOnce()).println(contains("countdown:"));
        verify(mockOutput, atLeastOnce()).println("go");
    }

    @Test
    void testProcessClientMessageClickAdvancesPlayer() throws IllegalAccessException, NoSuchFieldException {
        Player player = gameManager.registerPlayer(mockOutput);
        player.setName("Caballo3");

        // Establecer raceStarted = true usando reflexión
        java.lang.reflect.Field field = gameManager.getClass().getDeclaredField("raceStarted");
        field.setAccessible(true);
        field.set(gameManager, true);

        // Ejecutar "click"
        gameManager.processClientMessage(player, "click");

        assertTrue(player.getProgress() > 0, "El jugador debería haber avanzado");
    }

    @Test
    void testRemovePlayer() {
        Player player1 = gameManager.registerPlayer(mockOutput);
        player1.setName("Jugador1");

        // Agregamos otro jugador para generar un estado más realista
        PrintStream otherStream = mock(PrintStream.class);
        Player player2 = gameManager.registerPlayer(otherStream);
        player2.setName("Jugador2");

        gameManager.removePlayer(player1, mockOutput);

        // Ahora verificamos que SE haya enviado un mensaje a otro jugador
        verify(otherStream, atLeastOnce()).println(anyString());
    }

    @BeforeEach
    void resetGameManager() throws Exception {
        // Asegurar inicialización
        gameManager = GameManager.getGamemanagerinstance();

        // Accede y limpia los campos privados
        Field playersField = GameManager.class.getDeclaredField("players");
        playersField.setAccessible(true);
        ((Map<?, ?>) playersField.get(gameManager)).clear();

        Field clientsField = GameManager.class.getDeclaredField("clients");
        clientsField.setAccessible(true);
        ((List<?>) clientsField.get(gameManager)).clear();

        Field readyPlayersField = GameManager.class.getDeclaredField("readyPlayers");
        readyPlayersField.setAccessible(true);
        ((Set<?>) readyPlayersField.get(gameManager)).clear();

        Field nextPlayerIdField = GameManager.class.getDeclaredField("nextPlayerId");
        nextPlayerIdField.setAccessible(true);
        nextPlayerIdField.set(gameManager, 1);

        Field raceStartedField = GameManager.class.getDeclaredField("raceStarted");
        raceStartedField.setAccessible(true);
        raceStartedField.set(gameManager, false);
    }

    @Test
    void testProcessClientMessage_AnnouncesWinnerAt100Percent() throws Exception {
        gameManager = GameManager.getGamemanagerinstance();

        Player player = gameManager.registerPlayer(mockOutput);
        player.setName("Ganador");

        // Forzar estado carrera iniciada y progreso alto
        Field raceStartedField = GameManager.class.getDeclaredField("raceStarted");
        raceStartedField.setAccessible(true);
        raceStartedField.set(gameManager, true);

        player.advance(100); // avanzar directamente al 100

        gameManager.processClientMessage(player, "click");

        verify(mockOutput, atLeastOnce()).println(startsWith("win:"));
    }

    @Test
    void testProcessClientMessageClicksSentSetsReportedClicks() {
        Player player = gameManager.registerPlayer(mockOutput);
        player.setName("CaballoClick");

        String message = "clicks_sent:15";

        gameManager.processClientMessage(player, message);

        assertEquals(15, player.getReportedClicks());
    }

    @Test
    void testStartCountdownBroadcastsCountdownAndGo() throws Exception {
        // Registro de 2 jugadores para que la cuenta atrás tenga sentido
        Player p1 = gameManager.registerPlayer(mockOutput);
        Player p2 = gameManager.registerPlayer(mockOutput);
        p1.setName("A");
        p2.setName("B");

        // Forzamos que todos estén listos
        gameManager.processClientMessage(p1, "ready");
        gameManager.processClientMessage(p2, "ready");

        // Esperamos a que el hilo de cuenta regresiva se ejecute
        Thread.sleep(3500); // 3 segundos de countdown + margen

        verify(mockOutput, atLeastOnce()).println(contains("countdown:"));
        verify(mockOutput, atLeastOnce()).println("go");
    }

    @Test
    void testRemovePlayer_StartsCountdownWhenRemainingPlayersAreAllReady() throws Exception {
        // Configurar 3 jugadores
        PrintStream output1 = mock(PrintStream.class);
        PrintStream output2 = mock(PrintStream.class);
        PrintStream output3 = mock(PrintStream.class);

        Player player1 = gameManager.registerPlayer(output1);
        Player player2 = gameManager.registerPlayer(output2);
        Player player3 = gameManager.registerPlayer(output3);

        // Marcar a todos como listos
        gameManager.processClientMessage(player1, "ready");
        gameManager.processClientMessage(player2, "ready");
        gameManager.processClientMessage(player3, "ready");

        // Eliminar un jugador
        gameManager.removePlayer(player3, output3);

        // Esperar a que complete la cuenta regresiva
        Thread.sleep(3500);

        // Verificación usando ArgumentCaptor
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(output1, atLeast(3)).println(messageCaptor.capture());

        // Verificar que se enviaron los mensajes de countdown
        List<String> messages = messageCaptor.getAllValues();
        assertTrue(messages.stream().anyMatch(msg -> msg.startsWith("countdown:3")));
        assertTrue(messages.stream().anyMatch(msg -> msg.startsWith("countdown:2")));
        assertTrue(messages.stream().anyMatch(msg -> msg.startsWith("countdown:1")));
        assertTrue(messages.contains("go"));

        // Verificar que al jugador eliminado no se le envió nada
        verify(output3, never()).println(any(String.class));
    }
}
