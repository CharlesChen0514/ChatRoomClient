package org.bitkernel.tcp;

import com.sun.istack.internal.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.bitkernel.commom.Printer;

import java.util.HashSet;
import java.util.Set;

import static org.bitkernel.client.Client.isRunning;
import static org.bitkernel.tcp.TcpListener.connMap;
import static org.bitkernel.tcp.TcpListener.removeUser;

@Slf4j
public class HeartBeatDetector implements Runnable {

    /** Heartbeat detection time interval (ms) */
    public static final int TIME_INTERVAL = 1000;
    /** Heartbeat packet string */
    public static final String HEART_BEAT = "Heart beat\n";
    /** Heartbeat packet response string */
    public static final String ALIVE = "Alive\n";

    /**
     * Clean up unresponsive TCP connections.
     */
    private void cleanConnection() {
        Set<String> offlineUsers = new HashSet<>();
        connMap.forEach((name, conn) -> {
            if (!isOnline(conn)) {
                offlineUsers.add(name);
            }
        });
        for (String u : offlineUsers) {
            removeUser(u);
            logger.debug("{} disconnected", u);
            Printer.displayLn("%s offline", u);
        }
    }

    /**
     * Determine whether the connection is online.
     * @return online or not
     */
    private boolean isOnline(@NotNull TcpConn conn) {
        try {
            conn.getDout().writeUTF(HEART_BEAT);
            conn.getDout().flush();
            String rsp = conn.getDin().readUTF();
            if (rsp.equals(ALIVE)) {
                return true;
            }
        } catch (Exception ex) {
            return false;
        }
        return false;
    }

    @Override
    public void run() {
        logger.debug("Heart beat detector started successfully");
        while (isRunning) {
            try {
                Thread.sleep(TIME_INTERVAL);
                cleanConnection();
            } catch (InterruptedException e) {
                logger.error("Heart beat detector error");
            }
        }
        logger.debug("Heart beat detector ended successfully");
    }
}
