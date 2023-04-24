package org.bitkernel.client;

import com.sun.istack.internal.NotNull;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bitkernel.commom.Data;
import org.bitkernel.commom.Printer;
import org.bitkernel.commom.User;
import org.bitkernel.tcp.TcpConn;
import org.bitkernel.udp.Udp;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.bitkernel.client.Client.*;
import static org.bitkernel.commom.CmdType.menu;
import static org.bitkernel.commom.Data.parse;
import static org.bitkernel.commom.FileUtil.getAllFileNameString;
import static org.bitkernel.commom.StringUtil.getSocketAddrStr;
import static org.bitkernel.commom.StringUtil.isNumeric;
import static org.bitkernel.tcp.TcpListener.*;

@Slf4j
public class Handler implements Runnable {
    @Getter
    private final Udp udp;

    public Handler() {
        udp = new Udp(user.getUdpPort());
        Thread t1 = new Thread(new Receiver());
        t1.start();
    }

    class Receiver implements Runnable {
        @Override
        public void run() {
            logger.debug("UDP receiver started successfully");
            while (isRunning) {
                String dataStr = udp.receiveString();
                response(dataStr);
            }
            logger.debug("UDP receiver ended successfully");
        }
    }

    private void response(@NotNull String fDataStr) {
        Data data = parse(fDataStr);
        switch (data.getCmdType()) {
            case PRIVATE_MSG:
                Printer.displayLn("%s say: %s", data.getFrom(), data.getMsg());
                break;
            case FILE_TRANSFER:
                break;
            default:
                System.out.println("Invalid selection, please re-enter");
        }
    }

    @NotNull
    public Set<User> getFriendObjects() {
        return new LinkedHashSet<>(userMap.values());
    }

    public void request(@NotNull String fDataStr) {
        Data data = parse(fDataStr);
        logger.debug("Client make request: {}", data);
        switch (data.getCmdType()) {
            case INFO:
                Printer.displayLn(user.detailed());
                break;
            case CONNECT:
                connectReq(data);
                break;
            case DISCONNECT:
                disconnectReq(data);
                break;
            case FRIENDS:
                Printer.displayLn(getFriendString());
                break;
            case PRIVATE_MSG:
                pmReq(data);
                break;
            case FILE_TRANSFER:
                break;
            case ACCEPTED_FILES:
                Printer.displayLn(getAllFileNameString(dir));
                break;
            case HELP:
                menu.forEach(System.out::println);
                break;
            case EXIT:
                exit();
                break;
            default:
                Printer.displayLn("Invalid selection, please re-enter");
        }
    }

    private void pmReq(@NotNull Data data) {
        String to = data.getTo();
        if (!isFriend(to)) {
            Printer.displayLn(String.format("%s is not your friend", to));
            return;
        }
        User toUser = userMap.get(to);
        udp.send(toUser.getIp(), toUser.getUdpPort(), data.toDataString());
        Printer.displayLn("To %s say: %s", to, data.getMsg());
    }

    private void exit() {
        isRunning = false;
        executorService.shutdown();
        Printer.displayLn("Goodbye %s ~~~", user.getName());
        System.exit(-1);
    }

    private void disconnectReq(@NotNull Data data) {
        String to = data.getTo();
        if (remove(to)) {
            Printer.displayLn("Disconnected to " + to);
        } else {
            Printer.displayLn(String.format("%s is not your friend", to));
        }
    }

    private void connectReq(@NotNull Data data) {
        String ip;
        int port;
        if (isNumeric(data.getTo())) {
            ip = localHost;
            port = Integer.parseInt(data.getTo());
        } else {
            if (data.getTo().equals("local")) {
                ip = localHost;
            } else {
                ip = data.getTo();
            }
            port = Integer.parseInt(data.getMsg());
        }
        String socAddr = getSocketAddrStr(ip, port);


        if (isFriend(socAddr)) {
            User u = socAddrMap.get(socAddr);
            Printer.displayLn("User %s %s is already is your friend",
                    u.getName(), socAddr);
            return;
        }

        try {
            TcpConn conn = new TcpConn(ip, port);
            conn.getDout().writeUTF(data.toDataString());
            logger.debug("Start send user information");
            conn.getDout().writeUTF(user.toString());
            conn.getDout().flush();
            logger.debug("Send your own information success");
            String userString = conn.getDin().readUTF();
            logger.debug("Receive friend information success");
            add(userString, conn);
        } catch (IOException e) {
            String error = String.format("Connect to %s:%d failed", ip, port);
            logger.error(error);
            Printer.displayLn(error);
        }
    }

    @Override
    public void run() {

    }
}
