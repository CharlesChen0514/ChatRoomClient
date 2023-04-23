package org.bitkernel.commom;

import com.sun.istack.internal.NotNull;
import lombok.AllArgsConstructor;

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

    @NotNull
    public String toString() {
        return String.format("%s, %s, %s", cmd, description, example);
    }
}
