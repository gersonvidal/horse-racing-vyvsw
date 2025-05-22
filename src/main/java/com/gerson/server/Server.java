package com.gerson.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.gerson.network.ClientHandler;

public class Server {
    private static final int PORT = 1818;
    private static final Logger logger = Logger.getLogger(Server.class.getName());
    private static volatile boolean running = true;
    private static ServerSocket serverSocket;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            logger.info("[OK] Servidor iniciado en el puerto " + PORT);
            logger.info("[OK] Servidor en espera de conexiones");

            while (running) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket);
                handler.start();
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Ocurrió un error al cerrar el socket del cliente", e);
        }
    }

    // Método para detener el servidor desde las pruebas
    public static void stopServer() {
        running = false;
        try {
            if (serverSocket != null) {
                serverSocket.close(); // Forzará que accept() lance IOException
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error al cerrar el servidor", e);
        }
    }

}