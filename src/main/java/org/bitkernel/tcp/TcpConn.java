package org.bitkernel.tcp;

import com.sun.istack.internal.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bitkernel.commom.User;
import org.springframework.util.StopWatch;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.bitkernel.client.Client.dir;
import static org.bitkernel.client.Client.isRunning;
import static org.bitkernel.commom.Printer.getTime;
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
            din.close();
            dout.close();
            socket.close();
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
            logger.debug("Heart beat to [{}] thread ended successfully", to.getName());
        }
    }

    public void acceptFile(@NotNull String from) {
        Thread t1 = new Thread(new DownLoadFile(this, from));
        t1.start();
    }

    public void pushFile(@NotNull User toUser, @NotNull String filePath) {
        Thread t1 = new Thread(new UpLoadFile(this, toUser, filePath));
        t1.start();
    }
}

@Slf4j
class DownLoadFile implements Runnable {
    private final String from;
    private final StopWatch watch = new StopWatch();
    private String startTime;
    private String endTime;
    private final DataInputStream din;
    private static final int READ_BUFFER_SIZE = 1024;
    private String fileName;
    private long fileSize;
    private String outputPath;

    public DownLoadFile(@NotNull TcpConn conn,
                        @NotNull String from) {
        this.from = from;
        din = conn.getDin();
    }

    @Override
    public void run() {
        acceptFile();
        outputInfo();
    }

    private void acceptFile() {
        logger.debug("Start accept file");
        startTime = getTime();
        watch.start();

        try {
            fileName = din.readUTF();
            fileSize = Long.parseLong(din.readUTF());
            outputPath = dir + fileName;
            FileOutputStream fos = new FileOutputStream(outputPath);
            byte[] buf = new byte[READ_BUFFER_SIZE];

            int length;
            while ((length = din.read(buf)) != -1) {
                fos.write(buf, 0, length);
            }

            fos.flush();
            fos.close();
        } catch (IOException e) {
            logger.error(e.getMessage());
        }

        watch.stop();
        endTime = getTime();
        logger.debug("Accept file {} success", fileName);
    }

    private void outputInfo() {
        long ms = watch.getTotalTimeMillis();
        System.out.printf("Successfully accept file from [%s]%n", from);
        System.out.printf("File name [%s], file size [%s bytes], store in [%s]%n",
                fileName, fileSize, outputPath);
        System.out.printf("Start time [%s], end time [%s], total time [%d]%n", startTime, endTime, ms);
    }
}

@Slf4j
class UpLoadFile implements Runnable {
    private final StopWatch watch = new StopWatch();
    private String startTime;
    private String endTime;
    private static final int WRITE_BUFFER_SIZE = 1024;
    private final User toUser;
    private final String filePath;
    private final DataOutputStream ps;
    private File file;
    private TcpConn conn;

    public UpLoadFile(@NotNull TcpConn conn, @NotNull User toUser,
                      @NotNull String filePath) {
        this.toUser = toUser;
        this.filePath = filePath;
        file = new File(filePath);
        ps = conn.getDout();
        this.conn = conn;
    }

    @Override
    public void run() {
        pushFile();
        outputInfo();
    }

    private void pushFile() {
        logger.debug("Start push file: {}", filePath);
        startTime = getTime();
        watch.start();

        try {
            DataInputStream fis = new DataInputStream(new BufferedInputStream(Files.newInputStream(Paths.get(filePath))));
            ps.writeUTF(file.getName());
            ps.writeUTF(String.valueOf(file.length()));
            ps.flush();

            byte[] buf = new byte[WRITE_BUFFER_SIZE];
            while (true) {
                int read = fis.read(buf);
                if (read == -1) {
                    break;
                }
                ps.write(buf, 0, read);
            }

            ps.flush();
            fis.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        watch.stop();
        endTime = getTime();
        conn.close();
        logger.debug("Push file {} success", filePath);
    }

    private void outputInfo() {
        long ms = watch.getTotalTimeMillis();
        System.out.printf("Successfully transfer file to [%s]%n", toUser.getName());
        System.out.printf("File name [%s], file size [%s bytes]%n", file.getName(), file.length());
        System.out.printf("Start time [%s], end time [%s], total time [%d]%n", startTime, endTime, ms);
    }
}
