package org.bitkernel.commom;

import com.sun.istack.internal.NotNull;

import java.util.Arrays;

public class StringUtil {
    /**
     * Determine if it is a numeric string.
     * @return is numeric or not
     */
    public static boolean isNumeric(String str) {
        for (char ch : str.toCharArray()) {
            if (!Character.isDigit(ch)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Join the string in the array, with the last one being the connector.
     * @param args elements and one connector
     * @return connected string
     */
    @NotNull
    public static String joinDelimiter(@NotNull String... args) {
        String[] argsCopy = Arrays.copyOf(args, args.length - 1);
        String delimiter = args[args.length - 1];
        return joinDelimiter(argsCopy, delimiter);
    }

    @NotNull
    public static String joinDelimiter(@NotNull String[] args,
                                       @NotNull String delimiter) {
        StringBuilder sb = new StringBuilder();
        for (String arg : args) {
            sb.append(arg).append(delimiter);
        }
        return sb.toString();
    }

    /**
     * @param str string
     * @param sym character to be counted
     * @return number of occurrences
     */
    public static int count(@NotNull String str, char sym) {
        int c = 0;
        for (char ch : str.toCharArray()) {
            if (ch == sym) {
                c++;
            }
        }
        return c;
    }

    @NotNull
    public static String getSocketAddrStr(@NotNull String ip,
                                          int port) {
        return String.format("%s:%d", ip, port);
    }
}
