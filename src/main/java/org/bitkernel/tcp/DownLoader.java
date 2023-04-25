package org.bitkernel.tcp;

import com.sun.istack.internal.NotNull;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bitkernel.commom.Printer;

import java.io.FileOutputStream;
import java.io.IOException;

import static org.bitkernel.client.Client.dir;
import static org.bitkernel.commom.FileUtil.deleteFile;
import static org.bitkernel.commom.Printer.getTime;

@Slf4j
public class DownLoader extends Loader implements Runnable {
    public static long MAX_FILE_SIZE = 500L * 1024 * 1024 * 10;
    @Getter
    private final String from;
    private String outputPath;
    @Getter
    private Status status = Status.READY;
    private FileOutputStream fos;

    public DownLoader(@NotNull TcpConn conn,
                      @NotNull String from) {
        super(conn);
        this.from = from;
    }

    public void init() {
        logger.debug("Initialize {} file transfer request from {}", fileName, from);
        try {
            fileName = in.readUTF();
            fileSize = Long.parseLong(in.readUTF());
            outputPath = dir + fileName;
            if (isExceedMaximumSize()) {
                out.writeUTF("No");
                status = Status.DONE;
            } else {
                out.writeUTF("Yes");
                fos = new FileOutputStream(outputPath);
            }
            out.flush();
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        logger.debug("Initialize {} file transfer request from {} done", fileName, from);
    }

    public boolean isExceedMaximumSize() {
        return fileSize > MAX_FILE_SIZE;
    }

    @Override
    public void run() {
        logger.debug("The reception thread of file {} from {} started", fileName, from);
        while (true) {
            boolean flag = false;
            switch (status) {
                case RUNNING:
                    acceptOneBuffBytes();
                    break;
                case DONE:
                    flag = true;
                    done();
                    break;
                case PAUSE:
                case READY:
                    break;
                case REFUSE:
                    flag = true;
                    close("Refuse");
                    deleteFile(outputPath);
                    break;
                default:
            }
            if (flag) {
                break;
            }
        }
    }

    private void done() {
        logger.debug("Received all byte of file {} from {}", fileName, from);
        close("Done");
        endTime = getTime();
        outputInfo();
        logger.debug("Accept file {} from {} success", fileName, from);
    }

    private void acceptOneBuffBytes() {
        watch.start();
        byte[] buf = new byte[BUFFER_SIZE];
        int length;
        try {
            length = in.read(buf);
            offset += length;
            fos.write(buf, 0, length);
            if (offset == fileSize) {
                logger.debug("Reach file size break");
                status = Status.DONE;
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        watch.stop();
    }

    public void start() {
        switch (status) {
            case READY:
                startTime = getTime();
                logger.debug("Start accept file {} from {}", fileName, from);
                Printer.displayLn("Start accept file [%s] from [%s]", fileName, from);
                status = Status.RUNNING;
                break;
            case PAUSE:
                Printer.displayLn("Continue accept file [%s] from [%s], current progress [%.2f]",
                        fileName, from, progressPercentage());
                status = Status.RUNNING;
                break;
            case RUNNING:
                Printer.displayLn("This file [%s] from [%s] is currently being received, current progress [%.2f]",
                        fileName, from, progressPercentage());
                break;
            case DONE:
                Printer.displayLn("This file [%s] from [%s] has been received and completed", fileName, from);
                break;
        }
    }

    public void pause() {
        switch (status) {
            case READY:
                Printer.displayLn("The reception of file [%s] from [%s] has not started yet", fileName, from);
                break;
            case PAUSE:
                Printer.displayLn("The reception of file [%s] from [%s] in a paused state, current progress [%.2f]",
                        fileName, from, progressPercentage());
                break;
            case RUNNING:
                Printer.displayLn("Pause reception of file [%s] from [%s], current progress [%.2f]",
                        fileName, from, progressPercentage());
                status = Status.PAUSE;
                break;
            case DONE:
                Printer.displayLn("This file [%s] from [%s] has been received and completed", fileName, from);
                break;
        }
    }

    public void refuse() {
        status = Status.REFUSE;
    }

    private void close(@NotNull String msg) {
        try {
            fos.flush();
            fos.close();
            out.writeUTF(msg);
            out.writeUTF(String.valueOf(watch.getTotalTimeMillis()));
            out.flush();
            conn.close();
            logger.debug("Close all relevant resource");
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private void outputInfo() {
        long ms = watch.getTotalTimeMillis();
        System.out.printf("Successfully accept file from [%s]%n", from);
        System.out.printf("File name [%s], file size [%s bytes], store in [%s]%n",
                fileName, fileSize, outputPath);
        System.out.printf("Start time [%s], end time [%s], transmission time [%d ms]%n", startTime, endTime, ms);
    }

    public boolean isDone() {
        return status == Status.DONE;
    }
}

enum Status {
    READY,
    PAUSE,
    RUNNING,
    REFUSE,
    DONE
}
