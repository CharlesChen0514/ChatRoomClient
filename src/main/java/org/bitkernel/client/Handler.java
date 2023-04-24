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
    }

    class Receiver implements Runnable {
        @Override
        public void run() {
            logger.info("UDP receiver started successfully");
            while (isRunning) {
                String dataStr = udp.receiveString();
                response(dataStr);
            }
        }
    }

    private void response(@NotNull String fDataStr) {
        Data data = parse(fDataStr);
        switch (data.getCmdType()) {
            case PRIVATE_MSG:
                break;
            case FILE_TRANSFER:
                break;
            default:
                System.out.println("Invalid selection, please re-enter");
        }
    }

    private void connectRsp(@NotNull Data data) {
        User user = User.parse(data.getMsg());
        userMap.put(user.getName(), user);
        Printer.display("Connected to ");
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
                Printer.display(user.detailed());
                break;
            case CONNECT:
                connectReq(data);
                break;
            case DISCONNECT:
                disconnectReq(data);
                break;
            case FRIENDS:
                Printer.display(getFriendString());
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
                Printer.display("Invalid selection, please re-enter");
        }
    }

    private void disconnectReq(@NotNull Data data) {
        String to = data.getTo();
        if (remove(to)) {
            Printer.display("Disconnected to " + to);
        } else {
            Printer.display(String.format("%s is not your friend", to));
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
            Printer.display(String.format("User %s %s is already is your friend",
                    u.getName(), socAddr));
            return;
        }

        try {
            TcpConn conn = new TcpConn(ip, port);
            conn.getPw().println(user);
            String userString = conn.getBr().readLine();
            add(userString, conn);
        } catch (IOException e) {
            String error = String.format("Connect to %s:%d failed", ip, port);
            logger.error(error);
            System.out.println(error);
        }
    }

    @Override
    public void run() {

    }
}
