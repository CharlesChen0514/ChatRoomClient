package org.bitkernel.commom;

import com.sun.istack.internal.NotNull;
import lombok.AllArgsConstructor;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@AllArgsConstructor
public enum CmdType {
    FRIENDS("-fs", "friend list", "-fs"),
    CONNECT("-c", "connect user", "-c@port"),
    PRIVATE_MSG("-pm", "private message", "-pm@chen@hello"),
    FILE_TRANSFER("-ft", "file transfer", "-ft@chen@file"),
    ACCEPTED_FILES("-af", "accepted file list", "-af"),
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
        menu.add(FRIENDS);
        menu.add(CONNECT);
        menu.add(PRIVATE_MSG);
        menu.add(FILE_TRANSFER);
        menu.add(ACCEPTED_FILES);
        menu.add(HELP);
        menu.add(EXIT);
    }

    @NotNull
    public String toString() {
        return String.format("\t%s, %s, %s", cmd, description, example);
    }
}
