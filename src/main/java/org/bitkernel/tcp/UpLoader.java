package org.bitkernel.tcp;

import com.sun.istack.internal.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.bitkernel.commom.Printer;
import org.bitkernel.commom.User;
import org.springframework.util.StopWatch;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.bitkernel.commom.Printer.getTime;

@Slf4j
public class UpLoader implements Runnable {
    private final StopWatch watch = new StopWatch();
    private String startTime;
    private String endTime;
    private static final int WRITE_BUFFER_SIZE = 1024;
    private final User toUser;
    private final String filePath;
    private File file;
    private TcpConn conn;
    private boolean flag = true;

    public UpLoader(@NotNull TcpConn conn, @NotNull User toUser,
                      @NotNull String filePath) {
        this.toUser = toUser;
        this.filePath = filePath;
        file = new File(filePath);
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
        DataOutputStream out = conn.getDout();
        try {
            DataInputStream fis = new DataInputStream(new BufferedInputStream(Files.newInputStream(Paths.get(filePath))));
            out.writeUTF(file.getName());
            out.writeUTF(String.valueOf(file.length()));
            out.flush();

            String rsp = conn.getDin().readUTF();
            if (rsp.equals("No")) {
                Printer.displayLn("Receiver refuses to accept file");
                flag = false;
                return;
            }

            byte[] buf = new byte[WRITE_BUFFER_SIZE];
            while (true) {
                int read = fis.read(buf);
                if (read == -1) {
                    break;
                }
                out.write(buf, 0, read);
            }
            out.flush();

            // waiting for receiver reception done
            conn.getDin().readUTF();
            logger.debug("File receiver reception is complete");
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
        if (!flag) {
            return;
        }
        long ms = watch.getTotalTimeMillis();
        System.out.printf("Successfully transfer file to [%s]%n", toUser.getName());
        System.out.printf("File name [%s], file size [%s bytes]%n", file.getName(), file.length());
        System.out.printf("Start time [%s], end time [%s], total time [%d ms]%n", startTime, endTime, ms);
    }
}
