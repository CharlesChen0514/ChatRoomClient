package org.bitkernel.tcp;

import com.sun.istack.internal.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bitkernel.commom.User;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

import static org.bitkernel.client.Client.isRunning;
import static org.bitkernel.client.Client.user;
import static org.bitkernel.tcp.HeartBeatDetector.ALIVE;
import static org.bitkernel.tcp.HeartBeatDetector.HEART_BEAT;

@Slf4j
public class TcpConn {
    @Getter
    private Socket socket;
    @Getter
    private DataInputStream din;
    @Getter
    private DataOutputStream dout;
    @Setter
    private User to;

    public TcpConn(@NotNull Socket socket) {
        this.socket = socket;
        try {
            din = new DataInputStream(socket.getInputStream());
            dout = new DataOutputStream(socket.getOutputStream());
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

    public void close() {
        try {
            socket.close();
            dout.close();
            din.close();
        } catch (IOException e) {
            logger.error("Close resource error");
        }
    }

    public void startHeartBeat() {
        Thread t1 = new Thread(new HearBeat());
        t1.start();
    }

    class HearBeat implements Runnable {
        @Override
        public void run() {
            logger.debug("Heart beat thread started successfully");
            while (isRunning) {
                try {
                    String msg = din.readUTF();
                    if (msg.equals(HEART_BEAT)) {
                        dout.writeUTF(ALIVE);
                        dout.flush();
                    }
                } catch (IOException e) {
                    logger.error("Cannot read data");
                    break;
                }
            }
            logger.debug("Heart beat to [{}] thread ended successfully", user.getName());
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
