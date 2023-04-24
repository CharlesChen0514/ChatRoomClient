package org.bitkernel.client;

import com.sun.istack.internal.NotNull;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bitkernel.commom.Printer;
import org.bitkernel.commom.User;
import org.bitkernel.tcp.HeartBeatDetector;
import org.bitkernel.tcp.TcpConn;
import org.bitkernel.tcp.TcpListener;
import org.bitkernel.udp.Udp;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.bitkernel.commom.CmdType.menu;
import static org.bitkernel.commom.Data.*;
import static org.bitkernel.commom.FileUtil.createFolder;
import static org.bitkernel.commom.StringUtil.count;

@Slf4j
@NoArgsConstructor
public class Client {
    private final Scanner in = new Scanner(System.in);
    public static final String localHost;
    public static boolean isRunning = true;
    public static User user;
    public static String dir;
    public static ExecutorService executorService = Executors.newFixedThreadPool(4);
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
        Printer.displayLn("Welcome to chat room, please login");
        Printer.display("Input username: ");
        String name = in.next();
//        String name = "chen";

        while (true) {
            // Ip defaults to local host
            Printer.display("Input UDP port: ");
            int udpPort = in.nextInt();
//            int udpPort = 9996;
            Printer.display("Input Tcp listen port: ");
            int listenerPort = in.nextInt();
//            int listenerPort = 9997;

            if (!Udp.checkPort(udpPort) || !TcpConn.checkPort(listenerPort)) {
                Printer.displayLn("Input port unavailable, please re-entered");
                continue;
            }
            user = new User(name, localHost, udpPort, listenerPort);

            dir = System.getProperty("user.dir") + File.separator + "file" +
                    File.separator + name + File.separator;
            if (createFolder(dir)) {
                Printer.displayLn("Create user folder %s successfully.", dir);
            } else {
                Printer.displayLn("Create user folder %s failed.", dir);
            }
            logger.debug("Create user: [{}].", user.detailed());
            Printer.displayLn("Create user: [%s].", user.detailed());
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
                Printer.displayLn("Command error, please re-entered");
                continue;
            }
            handler.request(fDataStr);
        }
        logger.info("Exit chat menu");
    }

    private boolean check(@NotNull String dataStr) {
        return count(dataStr, sym.toCharArray()[0]) == 3 && checkDataStr(dataStr);
    }

    private void startLocalServer() {
        Thread t1 = new Thread(new TcpListener(user.getTcpListenPort()));
        Thread t2 = new Thread(new HeartBeatDetector());
        executorService.execute(t1);
        executorService.execute(t2);
        handler = new Handler();
    }
}
