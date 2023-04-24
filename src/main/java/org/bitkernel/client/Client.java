package org.bitkernel.client;

import com.sun.istack.internal.NotNull;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bitkernel.commom.Data;
import org.bitkernel.commom.Printer;
import org.bitkernel.commom.User;
import org.bitkernel.tcp.TcpConn;
import org.bitkernel.tcp.TcpListener;
import org.bitkernel.udp.Udp;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

import static org.bitkernel.commom.CmdType.*;
import static org.bitkernel.commom.Data.*;
import static org.bitkernel.commom.FileUtil.createFolder;
import static org.bitkernel.commom.FileUtil.getAllFileNameString;
import static org.bitkernel.commom.StringUtil.count;
import static org.bitkernel.tcp.TcpListener.*;

@Slf4j
@NoArgsConstructor
public class Client {
    private final Scanner in = new Scanner(System.in);
    public static final String localHost;
    public static boolean isRunning = true;
    public static User user;
    private static String dir;
    private Handler handler;

    static {
        try {
            localHost = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        logger.debug("Start chat room client");
        Client c = new Client();
        c.login();
        c.startLocalServer();
        c.chat();
    }

    private void login() {
        logger.debug("Start input user message");
        System.out.println("Welcome to chat room, please login");
        System.out.print("Input username: ");
        String name = in.next();
//        String name = "chen";

        while (true) {
            // Ip defaults to local host
            System.out.print("UDP port: ");
            int udpPort = in.nextInt();
//            int udpPort = 9996;
            System.out.print("Tcp listen port: ");
            int listenerPort = in.nextInt();
//            int listenerPort = 9997;

            if (!Udp.checkPort(udpPort) || !TcpConn.checkPort(listenerPort)) {
                System.out.println("Input port unavailable, please re-entered");
                continue;
            }
            user = new User(name, localHost, udpPort, listenerPort);

            dir = System.getProperty("user.dir") + File.separator + "file" +
                    File.separator + name + File.separator;
            if (createFolder(dir)) {
                System.out.printf("Create user folder %s successfully.%n", dir);
            } else {
                System.out.printf("Create user folder %s failed.%n", dir);
            }
            logger.debug("Create user: [{}].", user.detailed());
            System.out.printf("Create user: [%s].%n", user.detailed());
            break;
        }
        logger.debug("End input user message");
    }

    private void chat() {
        System.out.println("Command guide:");
        menu.forEach(System.out::println);
        in.nextLine();
        while (isRunning) {
            String cmdLine = in.nextLine();
            String dataStr = user.getName() + sym + cmdLine;
            String fDataStr = formalize(dataStr);
            if (!check(fDataStr)) {
                System.out.println("Command error, please re-entered");
                continue;
            }
            request(fDataStr);
        }
        logger.info("Exit chat menu");
    }

    private void request(@NotNull String fDataStr) {
        Data data = parse(fDataStr);
        logger.debug("Client make request: {}", data);
        switch (data.getCmdType()) {
            case INFO:
                Printer.display(user.detailed());
                break;
            case CONNECT:
                connectReq(data);
                break;
            case FRIENDS:
                Printer.display(handler.getFriendString());
                break;
            case PRIVATE_MSG:
                break;
            case FILE_TRANSFER:
                break;
            case ACCEPTED_FILES:
                Printer.display(getAllFileNameString(dir));
                break;
            case HELP:
                menu.forEach(System.out::println);
                break;
            case EXIT:
                break;
            default:
                System.out.println("Invalid selection, please re-enter");
        }
    }

    private void connectReq(@NotNull Data data) {
        int port = Integer.parseInt(data.getTo());
        try {
            TcpConn conn = new TcpConn(localHost, port);
            conn.getPw().println(user);
            String userString = conn.getBr().readLine();
            add(userString, conn);
        } catch (IOException e) {
            String error = String.format("Connect to %s:%d failed", localHost, port);
            logger.error(error);
            System.out.println(error);
        }
    }

    private boolean check(@NotNull String dataStr) {
        return count(dataStr, sym.toCharArray()[0]) == 3 && checkDataStr(dataStr);
    }

    private void startLocalServer() {
        Thread t1 = new Thread(new TcpListener(user.getTcpListenPort()));
        handler = new Handler();
        t1.start();
    }
}
