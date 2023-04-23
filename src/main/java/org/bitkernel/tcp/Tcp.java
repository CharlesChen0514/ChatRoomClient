package org.bitkernel.tcp;

import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.Socket;

@Slf4j
public class Tcp {
    public static boolean checkPort(int port) {
        try (Socket socket = new Socket()) {
            socket.bind(new InetSocketAddress(port));
            logger.debug("Tcp port {} is available", port);
            return true;
        } catch (Exception e) {
            logger.debug("Tcp port {} is unavailable", port);
            return false;
        }
    }
}
