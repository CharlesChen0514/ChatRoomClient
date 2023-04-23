package org.bitkernel.commom;

import com.sun.istack.internal.NotNull;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
public class Printer {
    @NotNull
    public static String getTime() {
        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.applyPattern("HH:mm:ss:SSS");
        Date date = new Date();
        return sdf.format(date);
    }

    public static void display(@NotNull String msg) {
        logger.debug("Display [{}]", msg);
        System.out.println(getTime() + ": " + msg);
    }
}
