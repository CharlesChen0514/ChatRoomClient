package org.bitkernel.client;

import com.sun.istack.internal.NotNull;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bitkernel.commom.Data;
import org.bitkernel.commom.Printer;
import org.bitkernel.commom.User;
import org.bitkernel.udp.Udp;

import java.util.Set;

import static org.bitkernel.client.Client.isRunning;
import static org.bitkernel.client.Client.user;
import static org.bitkernel.commom.Data.parse;
import static org.bitkernel.tcp.TcpListener.userMap;

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
    public String getFriendString() {
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
    public Set<String> getFriends() {
        return userMap.keySet();
    }

    public boolean isFriend(@NotNull String name) {
        return getFriends().contains(name);
    }

    @Override
    public void run() {

    }
}
