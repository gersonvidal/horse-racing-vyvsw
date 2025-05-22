package com.gerson.logic;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.gerson.model.Player;

/**
 * Clase principal que gestiona la lógica del juego.
 * Controla el registro de jugadores, el inicio de la carrera,
 * la recepción de mensajes de los clientes y la sincronización del estado.
 */
public class GameManager {
  private static final GameManager gameManagerInstance = new GameManager();
  private static final Logger logger = Logger.getLogger(GameManager.class.getName());

  private final Map<Integer, Player> players = new HashMap<>();
  private final List<PrintStream> clients = new ArrayList<>();
  private final Set<Integer> readyPlayers = new HashSet<>();
  private static final int MINIMUM_PLAYER_SIZE = 2;
  private boolean raceStarted = false;
  private int nextPlayerId = 1;

  private GameManager() {
  }

  /**
   * Obtiene la instancia singleton del GameManager.
   * 
   * @return instancia compartida de GameManager
   */
  public static GameManager getGamemanagerinstance() {
    return gameManagerInstance;
  }

  /**
   * Registra un nuevo jugador con su canal de salida asociado.
   * 
   * @param output canal de salida para enviar mensajes al jugador
   * @return el nuevo objeto Player registrado
   */
  public synchronized Player registerPlayer(PrintStream output) {
    int id = nextPlayerId++;
    Player player = new Player(id, output);
    players.put(id, player);
    clients.add(output);
    return player;
  }

  /**
   * Notifica a todos los clientes sobre la llegada de un nuevo jugador
   * y actualiza el estado del juego en consecuencia.
   * 
   * @param newPlayer jugador recién conectado
   */
  public synchronized void notifyNewPlayer(Player newPlayer) {
    logger.info("[:D] " + newPlayer.getName() + " (Caballo " + newPlayer.getId() + ") conectado.");
    broadcastPlayerList();
    notifyStartStatus();
  }

  /**
   * Procesa los mensajes recibidos desde un cliente.
   * Puede marcar al jugador como listo, avanzar en la carrera o
   * actualizar el número de clics reportados.
   * 
   * @param player  jugador que envió el mensaje
   * @param message contenido del mensaje
   */
  public synchronized void processClientMessage(Player player, String message) {
    if (message.equalsIgnoreCase("ready")) {
      readyPlayers.add(player.getId());

      int totalReady = readyPlayers.size();
      int totalConnected = players.size();

      if (logger.isLoggable(Level.INFO)) {
        logger.info(String.format(
            "[READY] %s listo (%d/%d)",
            player.getName(),
            totalReady,
            totalConnected));
      }
      if (readyPlayers.size() == players.size()) {
        startCountdown();
      }
    } else if (message.equalsIgnoreCase("click") && raceStarted) {
      player.advance(10);
      logger.info("[>>>] Caballo " + player.getId() + " avanzó a " + player.getProgress() + "%");
      broadcastMessage(player.getId() + ":" + player.getProgress());
      if (player.getProgress() >= 100) {
        announceWinner(player);
      }
    } else if (message.startsWith("clicks_sent:")) {
      int clicksReported = Integer.parseInt(message.split(":")[1]);
      player.setReportedClicks(clicksReported);
      if (logger.isLoggable(Level.INFO)) {
        logger.info(String.format(
            "[>] Caballo %d -> Clics reportados: %d",
            player.getId(),
            clicksReported));
      }
    }
  }

  /**
   * Elimina a un jugador y su canal de salida del sistema.
   * 
   * @param player jugador a eliminar
   * @param output canal de salida asociado al jugador
   */
  public synchronized void removePlayer(Player player, PrintStream output) {
    if (player != null) {
      players.remove(player.getId());
      readyPlayers.remove(player.getId());
    }
    clients.remove(output);
    notifyStartStatus();

    if (!raceStarted && readyPlayers.size() == players.size() && !players.isEmpty()) {
      logger.info("[INFO] Todos los jugadores restantes están listos. Iniciando carrera.");
      startCountdown();
    }
  }

  /**
   * Envía la lista actual de jugadores conectados a todos los clientes.
   */
  private void broadcastPlayerList() {
    StringBuilder message = new StringBuilder("players:");
    for (Player player : players.values()) {
      message.append(player.getId()).append("-").append(player.getName()).append(",");
    }
    broadcastMessage(message.toString());
  }

  /**
   * Notifica si la carrera puede comenzar o si faltan jugadores.
   */
  private void notifyStartStatus() {
    String status = players.size() >= MINIMUM_PLAYER_SIZE ? "can_start" : "wait_players";
    if (logger.isLoggable(Level.INFO)) {
      logger.info(String.format("[!] Enviando estado de inicio: %s", status));
    }
    broadcastMessage(status);
  }

  /**
   * Inicia la cuenta regresiva en un hilo nuevo antes de comenzar la carrera.
   * Captura interrupciones y registra advertencias si ocurren errores.
   */
  private void startCountdown() {
    new Thread(() -> {
      try {
        for (int i = 3; i > 0; i--) {
          broadcastMessage("countdown:" + i);
          Thread.sleep(1000);
        }
        raceStarted = true;
        broadcastMessage("go");
        logger.info("[GO] ¡Carrera iniciada!");
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        logger.warning("Hilo interrumpido: " + e.getMessage());
      }
    }).start();
  }

  /**
   * Envía un mensaje de victoria a todos los clientes y reinicia el estado de la
   * carrera.
   * 
   * @param player jugador que ganó la carrera
   */
  private void announceWinner(Player player) {
    String winnerMessage = "win:" + player.getName();
    broadcastMessage(winnerMessage);
    logger.info(
        "[!!!] Caballo "
            + player.getId()
            + " ("
            + player.getName()
            + ") ganó la carrera.");
    raceStarted = false;
  }

  /**
   * Envía un mensaje a todos los canales de salida conectados.
   * 
   * @param message mensaje a enviar
   */
  private void broadcastMessage(String message) {
    for (PrintStream out : clients) {
      out.println(message);
    }
  }
}