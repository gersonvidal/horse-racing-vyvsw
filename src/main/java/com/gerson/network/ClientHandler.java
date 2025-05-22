package com.gerson.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.gerson.logic.GameManager;
import com.gerson.model.Player;

/**
 * Hilo encargado de manejar la comunicación con un cliente.
 * Registra al jugador, recibe su nombre, procesa sus mensajes y gestiona su
 * desconexión.
 */
public class ClientHandler extends Thread {
  private final Socket socket;
  private Player player;
  private PrintStream output;

  private static final Logger logger = Logger.getLogger(ClientHandler.class.getName());

  /**
   * Constructor que recibe el socket del cliente conectado.
   * 
   * @param socket conexión de red del cliente
   */
  public ClientHandler(Socket socket) {
    this.socket = socket;
  }

  /**
   * Ejecuta el hilo. Registra al jugador, recibe su nombre,
   * escucha mensajes del cliente y procesa su desconexión.
   */
  @Override
  public void run() {
    if (logger.isLoggable(Level.INFO)) {
      logger.info(String.format("[THREAD] Cliente conectado en hilo %d", Thread.currentThread().getId()));
    }
    try (BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
      output = new PrintStream(socket.getOutputStream());

      player = GameManager.getGamemanagerinstance().registerPlayer(output);
      output.println(player.getId());

      String username = input.readLine().trim();
      if (username.isBlank()) {
        username = "Jugador " + player.getId();
      }
      player.setName(username);

      GameManager.getGamemanagerinstance().notifyNewPlayer(player);

      String line;
      while ((line = input.readLine()) != null) {
        GameManager.getGamemanagerinstance().processClientMessage(player, line);
      }

    } catch (IOException e) {
      if (logger.isLoggable(Level.INFO)) {
        logger.info(
            String.format("[X] Cliente desconectado: Caballo %s", (player != null ? player.getId() : "?")));
      }
    } finally {
      GameManager.getGamemanagerinstance().removePlayer(player, output);
      try {
        socket.close();
      } catch (IOException e) {
        if (logger.isLoggable(Level.SEVERE)) {
          logger.severe("[ERROR] No se pudo cerrar el socket del cliente.");
        }
      }
    }
  }
}