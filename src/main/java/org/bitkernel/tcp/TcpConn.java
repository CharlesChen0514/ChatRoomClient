package org.bitkernel.tcp;

import com.sun.istack.internal.NotNull;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

@Slf4j
public class TcpConn {
    @Getter
    private Socket socket;
    @Getter
    private BufferedReader br;
    @Getter
    private PrintWriter pw;

    public TcpConn(@NotNull Socket socket) {
        this.socket = socket;
        try {
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            pw = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    public TcpConn(@NotNull String ip, int port) throws IOException {
        this(new Socket(ip, port));
    }

    public static boolean checkPort(int port) {
        try (Socket socket = new Socket()) {
            socket.bind(new InetSocketAddress(port));
            logger.debug("Tcp port {} is available", port);
            return true;
        } catch (Exception e) {
            logger.debug("Tcp port {} is unavailable", port);
            return false;
        }
    }
}

class DownLoader {
    private Socket socket;
    private DataInputStream is;
    private FileOutputStream fos;
    public DownLoader(@NotNull Socket socket) {
//        this.socket = socket;
//        try {
//            DataInputStream is = new DataInputStream(socket.getInputStream());
//            FileOutputStream fos = new FileOutputStream(outputPath);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
    }
}
