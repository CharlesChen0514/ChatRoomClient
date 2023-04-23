package org.bitkernel.client;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bitkernel.commom.User;
import org.bitkernel.tcp.Tcp;
import org.bitkernel.udp.Udp;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

import static org.bitkernel.commom.FileUtil.createFolder;

@Slf4j
@NoArgsConstructor
public class Client {
    private final Scanner in = new Scanner(System.in);
    public static final String localHost;
    private User user;
    private String dir;

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

            if (!Udp.checkPort(udpPort) || !Tcp.checkPort(listenerPort)) {
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
}
