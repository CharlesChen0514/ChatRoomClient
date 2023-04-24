package org.bitkernel.commom;

import com.sun.istack.internal.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static org.bitkernel.commom.Data.sym;
import static org.bitkernel.commom.StringUtil.joinDelimiter;

@NoArgsConstructor
@Slf4j
public class User {
    @Getter
    private String name;
    @Getter
    private String ip;
    @Getter
    private int udpPort;
    @Getter
    private int tcpListenPort;

    public User(@NotNull String name, @NotNull String ip,
                int udpPort, int tcpListenPort) {
        this.name = name;
        this.ip = ip;
        this.udpPort = udpPort;
        this.tcpListenPort = tcpListenPort;
    }

    @NotNull
    public String toString() {
        return joinDelimiter(name, ip, String.valueOf(udpPort), String.valueOf(tcpListenPort), " ");
    }

    @NotNull
    public String detailed() {
        return String.format("name: %s, ip: %s, udp port: %s, tcp listen port: %s",
                name, ip, udpPort, tcpListenPort);
    }

    @NotNull
    public static User parse(@NotNull String userString) {
        String[] split = userString.split(" ");
        if (split.length != 4) {
            logger.error("Error user string format: {}", userString);
            return new User();
        }
        String name = split[0].trim();
        String ip = split[1].trim();
        int udpPort = Integer.parseInt(split[2].trim());
        int tcpListPort = Integer.parseInt(split[3].trim());
        return new User(name, ip, udpPort, tcpListPort);
    }
}
