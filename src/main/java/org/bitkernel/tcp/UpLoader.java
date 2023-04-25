package org.bitkernel.tcp;

import com.sun.istack.internal.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.bitkernel.commom.Printer;
import org.bitkernel.commom.User;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.bitkernel.commom.Printer.getTime;

@Slf4j
public class UpLoader extends Loader implements Runnable {
    private final User toUser;
    private final String filePath;
    private final File file;
    private boolean flag = true;

    public UpLoader(@NotNull TcpConn conn, @NotNull User toUser,
                    @NotNull String filePath) {
        super(conn);
        this.toUser = toUser;
        this.filePath = filePath;
        file = new File(filePath);
    }

    @Override
    public void run() {
        pushFile();
        outputInfo();
    }

    private void pushFile() {
        startTime = getTime();
        watch.start();
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
            Printer.displayLn("Add to recipient's waiting list");

            logger.debug("Start push file [{}] to [{}]", file.getName(), toUser.getName());
            byte[] buf = new byte[BUFFER_SIZE];
            while (true) {
                int read = fis.read(buf);
                if (read == -1) {
                    break;
                }
                out.write(buf, 0, read);
            }
            out.flush();
            logger.debug("File upload complete, waiting for reception");

            // waiting for receiver reception done
            conn.getDin().readUTF();
            logger.debug("File recipient received completed");
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
