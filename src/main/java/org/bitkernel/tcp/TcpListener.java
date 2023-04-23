package org.bitkernel.tcp;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

@Slf4j
public class TcpListener implements Runnable {

    public int port;
    private boolean isRunning = true;
    public TcpListener(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("Tcp listener started successfully");
            while (isRunning) {
                Socket socket = serverSocket.accept();
            }
            logger.info("Tcp listener stop successfully");
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }
}
