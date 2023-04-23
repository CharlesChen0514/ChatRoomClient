package org.bitkernel.client;

import com.sun.istack.internal.NotNull;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bitkernel.commom.Data;
import org.bitkernel.commom.User;
import org.bitkernel.tcp.TcpConn;
import org.bitkernel.tcp.TcpListener;
import org.bitkernel.udp.Udp;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

import static org.bitkernel.commom.Data.*;
import static org.bitkernel.commom.FileUtil.createFolder;
import static org.bitkernel.commom.CmdType.menu;

@Slf4j
@NoArgsConstructor
public class Client {
    private final Scanner in = new Scanner(System.in);
    public static final String localHost;
    private User user;
    private String dir;
    private boolean isRunning = true;

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

        while (true) {
            // Ip defaults to local host
            System.out.print("UDP port: ");
            int udpPort = in.nextInt();
            System.out.print("Tcp listen port: ");
            int listenerPort = in.nextInt();

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
//        in.nextLine();
        while (isRunning) {
            String cmdLine = in.nextLine();
            String dataStr = user.getName() + sym + cmdLine;
            String fDataStr = formalize(dataStr);
            if (!check(fDataStr)) {
                System.out.println("Command error, please re-entered");
                continue;
            }
            process(fDataStr);
        }
        logger.info("Exit chat menu");
    }

    private void process(@NotNull String fDataStr) {
        Data data = parse(fDataStr);
        switch (data.getCmdType()) {
            case FRIENDS:
                break;
            case CONNECT:
                break;
            case PRIVATE_MSG:
                break;
            case FILE_TRANSFER:
                break;
            case ACCEPTED_FILES:
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

    private boolean check(@NotNull String dataStr) {
        return countDelimiter(dataStr) == 3 && checkDataStr(dataStr);
    }

    private void startLocalServer() {
        Thread t1 = new Thread(new TcpListener(user.getTcpListenPort()));
        t1.start();
    }
}
