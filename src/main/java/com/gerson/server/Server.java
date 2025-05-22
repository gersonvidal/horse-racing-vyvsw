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

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            logger.info("[OK] Servidor iniciado en el puerto " + PORT);
            logger.info("[OK] Servidor en espera de conexiones");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket);
                handler.start();
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Ocurrió un error al cerrar el socket del cliente", e);
        }
    }

}