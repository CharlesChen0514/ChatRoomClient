package org.bitkernel.udp;

import lombok.extern.slf4j.Slf4j;

import java.net.DatagramSocket;

@Slf4j
public class Udp {
    public static boolean checkPort(int port) {
        try  (DatagramSocket socket = new DatagramSocket(port)){
            logger.debug("Udp port {} is available", port);
            return true;
        } catch (Exception e) {
            logger.debug("Udp port {} is unavailable", port);
            return false;
        }
    }
}
