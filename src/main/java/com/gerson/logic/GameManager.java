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

    public static GameManager getGamemanagerinstance() {
        return gameManagerInstance;
    }

    public synchronized Player registerPlayer(PrintStream output) {
        int id = nextPlayerId++;
        Player player = new Player(id, output);
        players.put(id, player);
        clients.add(output);
        return player;
    }

    public synchronized void notifyNewPlayer(Player newPlayer) {
        logger.info("[:D] " + newPlayer.getName() + " (Caballo " + newPlayer.getId() + ") conectado.");
        broadcastPlayerList();
        notifyStartStatus();
    }

    public synchronized void processClientMessage(Player player, String message) {
        if (message.equalsIgnoreCase("ready")) {
            readyPlayers.add(player.getId());

            int totalReady = readyPlayers.size();
            int totalConnected = players.size();

            if (logger.isLoggable(Level.INFO)) {
                logger.info(String.format("[READY] %s listo (%d/%d)", player.getName(), totalReady, totalConnected));
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
                logger.info(String.format("[>] Caballo %d -> Clics reportados: %d", player.getId(), clicksReported));
            }
        }
    }

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

    private void broadcastPlayerList() {
        StringBuilder message = new StringBuilder("players:");
        for (Player player : players.values()) {
            message.append(player.getId()).append("-").append(player.getName()).append(",");
        }
        broadcastMessage(message.toString());
    }

    private void notifyStartStatus() {
        String status = players.size() >= MINIMUM_PLAYER_SIZE ? "can_start" : "wait_players";
        if (logger.isLoggable(Level.INFO)) {
            logger.info(String.format("[!] Enviando estado de inicio: %s", status));
        }
        broadcastMessage(status);
    }

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

    private void announceWinner(Player player) {
        String winnerMessage = "win:" + player.getName();
        broadcastMessage(winnerMessage);
        logger.info("[!!!] Caballo " + player.getId() + " (" + player.getName() + ") ganó la carrera.");
        raceStarted = false;
    }

    private void broadcastMessage(String message) {
        for (PrintStream out : clients) {
            out.println(message);
        }
    }
}