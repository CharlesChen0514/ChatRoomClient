package org.bitkernel.tcp;

import com.sun.istack.internal.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.bitkernel.commom.Printer;
import org.bitkernel.commom.User;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.bitkernel.client.Client.isRunning;
import static org.bitkernel.client.Client.user;

@Slf4j
public class TcpListener implements Runnable {
    /** name -> user */
    public static final Map<String, User> userMap = new ConcurrentHashMap<>();
    /** name -> tcp connection */
    public static final Map<String, TcpConn> connMap = new ConcurrentHashMap<>();
    /** tcp Socket address -> user */
    public static final Map<String, User> socAddrMap = new ConcurrentHashMap<>();
    public int port;

    public TcpListener(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("Tcp listener started successfully");
            while (isRunning) {
                Socket socket = serverSocket.accept();
                TcpConn conn = new TcpConn(socket);
                String userString = conn.getBr().readLine();
                conn.getPw().println(user);
                add(userString, conn);
            }
            logger.info("Tcp listener stop successfully");
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    public static void add(@NotNull String userString,
                           @NotNull TcpConn conn) {
        User from = User.parse(userString);
        connMap.put(from.getName(), conn);
        userMap.put(from.getName(), from);
        socAddrMap.put(from.getTcpSocketAddrStr(), from);
        Printer.display("Connected to: " + from.getName());
    }
}
