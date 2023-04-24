package org.bitkernel.commom;

import com.sun.istack.internal.NotNull;
import lombok.AllArgsConstructor;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static org.bitkernel.commom.Data.sym;
import static org.bitkernel.commom.StringUtil.joinDelimiter;

@AllArgsConstructor
public enum CmdType {
    INFO("-i", "your user information", "-i"),
    FRIENDS("-fs", "friend list", "-fs"),
    CONNECT("-c", "connect user", "-c@{port}, -c@local@{port}, -c@{ip}@{port}"),
    DISCONNECT("-dc", "disconnect user", "-dc@{name}"),
    PRIVATE_MSG("-pm", "private message", "-pm@{name}@{msg}"),
    FILE_TRANSFER("-ft", "file transfer", "-ft@{name}@{file}"),
    ACCEPTED_FILES("-af", "accepted file list", "-af"),
    MAX_FILE_SIZE("-mfs", "set the maximum file size allowed", "-mfs@{fileSize}"),
    HELP("-h", "command prompt", "-h"),
    EXIT("-q", "exit", "-q");

    public final String cmd;
    public final String description;
    public final String example;
    public static final Map<String, CmdType> cmdToEnumMap;
    public static final Set<CmdType> menu;

    static {
        cmdToEnumMap = new LinkedHashMap<>();
        for (CmdType cmdType : CmdType.values()) {
            cmdToEnumMap.put(cmdType.cmd, cmdType);
        }

        menu = new LinkedHashSet<>();
        menu.add(INFO);
        menu.add(CONNECT);
        menu.add(DISCONNECT);
        menu.add(FRIENDS);
        menu.add(PRIVATE_MSG);
        menu.add(FILE_TRANSFER);
        menu.add(ACCEPTED_FILES);
        menu.add(MAX_FILE_SIZE);
        menu.add(HELP);
        menu.add(EXIT);
    }

    @NotNull
    public static String constructCmdString(@NotNull String... args) {
        return joinDelimiter(args, sym);
    }

    @NotNull
    public String toString() {
        return String.format("\t%s, %s, %s", cmd, description, example);
    }
}
