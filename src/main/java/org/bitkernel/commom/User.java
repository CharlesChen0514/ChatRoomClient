package org.bitkernel.commom;

import com.sun.istack.internal.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static org.bitkernel.commom.Data.sym;

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
        return name + sym
                + ip + sym
                + udpPort + sym
                + tcpListenPort;
    }

    @NotNull
    public String detailed() {
        return String.format("name: %s, ip: %s, udp port: %s, tcp listen port: %s",
                name, ip, udpPort, tcpListenPort);
    }

    @NotNull
    public static User parse(@NotNull String str) {
        String[] split = str.split(sym);
        if (split.length != 4) {
            logger.error("Error user string format: {}", str);
            return new User();
        }
        String name = split[0].trim();
        String ip = split[1].trim();
        int udpPort = Integer.parseInt(split[2].trim());
        int tcpListPort = Integer.parseInt(split[3].trim());
        return new User(name, ip, udpPort, tcpListPort);
    }
}
