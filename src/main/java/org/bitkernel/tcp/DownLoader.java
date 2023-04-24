package org.bitkernel.tcp;

import com.sun.istack.internal.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.bitkernel.commom.Printer;
import org.springframework.util.StopWatch;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.bitkernel.client.Client.dir;
import static org.bitkernel.commom.Printer.getTime;

@Slf4j
public class DownLoader implements Runnable {
    private final String from;
    private final StopWatch watch = new StopWatch();
    private String startTime;
    private String endTime;
    private final TcpConn conn;
    private static final int READ_BUFFER_SIZE = 1024;
    private String fileName;
    private long fileSize;
    private String outputPath;
    /**
     * 500 MB
     */
    public static long MAX_FILE_SIZE = 500 * 1024 * 1024;
    private boolean flag = true;

    public DownLoader(@NotNull TcpConn conn,
                      @NotNull String from) {
        this.from = from;
        this.conn = conn;
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
        DataOutputStream out = conn.getDout();
        DataInputStream in = conn.getDin();

        try {
            fileName = in.readUTF();
            fileSize = Long.parseLong(in.readUTF());
            if (fileSize > MAX_FILE_SIZE) {
                Printer.displayLn("%s transferred a file larger than 500 MB, " +
                        "refuse to accept", from);
                out.writeUTF("No");
                flag = false;
                return;
            } else {
                out.writeUTF("Yes");
            }
            outputPath = dir + fileName;
            FileOutputStream fos = new FileOutputStream(outputPath);
            byte[] buf = new byte[READ_BUFFER_SIZE];

            int length;
            int c = 0;
            while ((length = in.read(buf)) != -1) {
                c += length;
                fos.write(buf, 0, length);
                if (c == fileSize) {
                    break;
                }
            }

            fos.flush();
            fos.close();
            out.writeUTF("Done");
            out.flush();
        } catch (IOException e) {
            logger.error(e.getMessage());
        }

        watch.stop();
        endTime = getTime();
        logger.debug("Accept file {} success", fileName);
    }

    private void outputInfo() {
        if (!flag) {
            return;
        }
        long ms = watch.getTotalTimeMillis();
        System.out.printf("Successfully accept file from [%s]%n", from);
        System.out.printf("File name [%s], file size [%s bytes], store in [%s]%n",
                fileName, fileSize, outputPath);
        System.out.printf("Start time [%s], end time [%s], total time [%d ms]%n", startTime, endTime, ms);
    }
}
