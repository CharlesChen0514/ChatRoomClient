package org.bitkernel.tcp;

import com.sun.istack.internal.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.bitkernel.commom.Data;
import org.bitkernel.commom.Printer;
import org.bitkernel.commom.User;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.bitkernel.client.Client.isRunning;
import static org.bitkernel.client.Client.user;

@Slf4j
public class TcpListener implements Runnable {
    /**
     * name -> user
     */
    public static final Map<String, User> userMap = new ConcurrentHashMap<>();
    /**
     * name -> tcp connection
     */
    public static final Map<String, TcpConn> connMap = new ConcurrentHashMap<>();
    /**
     * tcp Socket address -> user
     */
    public static final Map<String, User> socAddrMap = new ConcurrentHashMap<>();
    public int port;

    public TcpListener(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.debug("Tcp listener started successfully");
            while (isRunning) {
                Socket socket = serverSocket.accept();
                TcpConn conn = new TcpConn(socket);
                new Thread(new Process(conn)).start();
            }
            logger.debug("Tcp listener stop successfully");
            connMap.values().forEach(TcpConn::close);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    class Process implements Runnable {
        private TcpConn conn;
        public Process(@NotNull TcpConn conn) {
            this.conn = conn;
        }
        @Override
        public void run() {
            try {
                String dataStr = conn.getDin().readUTF();
                Data data = Data.parse(dataStr);
                switch (data.getCmdType()) {
                    case CONNECT:
                        newConnection(conn);
                        break;
                    case FILE_TRANSFER:
                        conn.acceptFile(data.getFrom());
                        break;
                    default:
                        logger.error("Error command format: {}", dataStr);
                }
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        }
    }

    private void newConnection(@NotNull TcpConn conn) throws IOException {
        logger.debug("Start send user information");
        conn.getDout().writeUTF(user.toString());
        conn.getDout().flush();
        logger.debug("Send your own information success");
        String userString = conn.getDin().readUTF();
        logger.debug("Receive friend information success");
        add(userString, conn);
    }

    public static void add(@NotNull String userString,
                           @NotNull TcpConn conn) {
        conn.startHeartBeat();
        User from = User.parse(userString);
        conn.setTo(from);
        connMap.put(from.getName(), conn);
        userMap.put(from.getName(), from);
        socAddrMap.put(from.getTcpSocketAddrStr(), from);
        Printer.displayLn("Connected to: " + from.detailed());
    }

    @NotNull
    public static String getFriendString() {
        Set<String> fs = getFriends();
        if (fs.isEmpty()) {
            return "You have no friends yet";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Your friends: ");
        fs.forEach(f -> sb.append(f).append(", "));
        sb.deleteCharAt(sb.length() - 1);
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    @NotNull
    public static Set<String> getFriends() {
        return userMap.keySet();
    }

    /**
     * @param str user name or tcp socket address
     */
    public static boolean isFriend(@NotNull String str) {
        return userMap.containsKey(str) || socAddrMap.containsKey(str);
    }

    public static boolean remove(@NotNull String name) {
        if (!isFriend(name)) {
            return false;
        }
        User u = userMap.get(name);
        socAddrMap.remove(u.getTcpSocketAddrStr());
        connMap.get(name).close();
        connMap.remove(name);
        userMap.remove(name);
        return true;
    }
}
