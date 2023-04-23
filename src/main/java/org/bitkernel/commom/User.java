package org.bitkernel.commom;

import com.sun.istack.internal.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static org.bitkernel.udp.UdpData.sym;

@AllArgsConstructor
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
    private int tcpListPort;
    private final static int FIELD_LEN = 4;

    @NotNull
    public String toString() {
        return name + sym
                + ip + sym
                + udpPort + sym
                + tcpListPort;
    }

    @NotNull
    public static User parse(@NotNull String str) {
        String[] split = str.split(sym);
        if (split.length != FIELD_LEN) {
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
