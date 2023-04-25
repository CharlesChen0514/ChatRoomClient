package org.bitkernel.client;

import com.sun.istack.internal.NotNull;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bitkernel.commom.Data;
import org.bitkernel.commom.Printer;
import org.bitkernel.commom.User;
import org.bitkernel.tcp.DownLoader;
import org.bitkernel.tcp.TcpConn;
import org.bitkernel.tcp.TcpListener;
import org.bitkernel.udp.Udp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.bitkernel.client.Client.*;
import static org.bitkernel.commom.CmdType.menu;
import static org.bitkernel.commom.Data.parse;
import static org.bitkernel.commom.FileUtil.*;
import static org.bitkernel.commom.StringUtil.getSocketAddrStr;
import static org.bitkernel.commom.StringUtil.isNumeric;
import static org.bitkernel.tcp.TcpListener.*;

@Slf4j
public class Handler {
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
                fileTransferReq(data);
                break;
            case RECEPTION_REQ_LIST:
                showWaitList();
                break;
            case ACCEPT:
                acceptFile(Integer.parseInt(data.getTo()));
                break;
            case REFUSE:
                refuseFileTranReq(Integer.parseInt(data.getTo()));
                break;
            case PAUSE:
                pauseFileTranReq(Integer.parseInt(data.getTo()));
                break;
            case ACCEPTED_FILES:
                Printer.displayLn(getAllFileNameString(dir));
                break;
            case HELP:
                menu.forEach(System.out::println);
                break;
            case MAX_FILE_SIZE:
                DownLoader.MAX_FILE_SIZE = Long.parseLong(data.getTo());
                Printer.displayLn("Maximum allowed file size is set to %d", DownLoader.MAX_FILE_SIZE);
                break;
            case EXIT:
                exit();
                break;
            default:
                Printer.displayLn("Invalid selection, please re-enter");
        }
    }

    private void refuseFileTranReq(@NotNull int idx) {
        if (idx > fileTransferReqList.size() || idx <= 0) {
            Printer.displayLn("Wrong index of transfer list, valid range is %d - %d",
                    1, fileTransferReqList.size());
            return;
        }
        DownLoader downLoader = fileTransferReqList.get(idx - 1);
        downLoader.refuse();
        Printer.displayLn("Refuse to accept the file [%s] from [%s]",
                downLoader.getFrom(), downLoader.getFileName());
        removeFileTransferReq(idx - 1);
    }

    private void pauseFileTranReq(@NotNull int idx) {
        if (idx > fileTransferReqList.size() || idx <= 0) {
            Printer.displayLn("Wrong index of transfer list, valid range is %d - %d",
                    1, fileTransferReqList.size());
            return;
        }
        DownLoader downLoader = fileTransferReqList.get(idx - 1);
        downLoader.pause();
        Printer.displayLn("Pause to accept the file [%s] from [%s]",
                downLoader.getFrom(), downLoader.getFileName());
    }

    private void showWaitList() {
        cleanWaitList();
        if (fileTransferReqList.isEmpty()) {
            Printer.displayLn("No requests waiting for file reception");
            return;
        }
        for (int i = 0; i < fileTransferReqList.size(); i++) {
            DownLoader downLoader = fileTransferReqList.get(i);
            System.out.printf("\t%d) from: %s, file name: %s, file size: %s, progress: %.2f%%, status: %s%n",
                    i + 1, downLoader.getFrom(), downLoader.getFileName(),
                    downLoader.getFileSize(), downLoader.progressPercentage(), downLoader.getStatus());
        }
    }

    private void cleanWaitList() {
        List<Integer> removeList = new ArrayList<>();
        for (int i = 0; i < fileTransferReqList.size(); i++) {
            if (fileTransferReqList.get(i).isDone()) {
                removeList.add(i);
            }
        }
        removeList.forEach(TcpListener::removeFileTransferReq);
    }

    private void acceptFile(int index) {
        if (index > fileTransferReqList.size() || index <= 0) {
            Printer.displayLn("Wrong index of transfer list, valid range is %d - %d",
                    1, fileTransferReqList.size());
            return;
        }
        DownLoader downLoader = fileTransferReqList.get(index - 1);
        downLoader.start();
    }

    private void fileTransferReq(@NotNull Data data) {
        String from = data.getFrom();
        String to = data.getTo();
        if (from.equals(to)) {
            Printer.displayLn("Cannot transfer file to yourself, please re-entered");
            return;
        }
        if (!isFriend(to)) {
            Printer.displayLn("%s is not your friend", to);
            return;
        }
        String filePath = data.getMsg();
        if (!exist(filePath) && !existInFolder(dir, filePath)) {
            Printer.displayLn("Invalid file format %s, try again%n", filePath);
            return;
        }

        try {
            User toUser = userMap.get(to);
            TcpConn conn = new TcpConn(toUser.getIp(), toUser.getTcpListenPort());
            conn.getDout().writeUTF(data.toDataString());
            conn.getDout().flush();
            conn.pushFile(toUser, data.getMsg());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void pmReq(@NotNull Data data) {
        String to = data.getTo();
        if (!isFriend(to)) {
            Printer.displayLn("%s is not your friend", to);
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
}
