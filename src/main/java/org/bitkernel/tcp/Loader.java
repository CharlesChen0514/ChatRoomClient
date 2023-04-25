package org.bitkernel.tcp;

import com.sun.istack.internal.NotNull;
import lombok.Getter;
import org.springframework.util.StopWatch;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class Loader {
    protected static final int BUFFER_SIZE = 16;
    protected final TcpConn conn;
    protected final StopWatch watch = new StopWatch();
    @Getter
    protected String fileName;
    @Getter
    protected long fileSize;
    protected DataOutputStream out;
    protected DataInputStream in;
    protected String endTime;
    protected String startTime;
    protected long offset;

    public Loader(@NotNull TcpConn conn) {
        this.conn = conn;
        out = conn.getDout();
        in = conn.getDin();
    }

    public double progressPercentage() {
        return (offset * 1.0 / fileSize) * 100;
    }
}
