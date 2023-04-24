package org.bitkernel.commom;

import com.sun.istack.internal.NotNull;

import java.util.Arrays;

public class StringUtil {
    public static boolean isNumeric(String str) {
        for (char ch : str.toCharArray()) {
            if (!Character.isDigit(ch)) {
                return false;
            }
        }
        return true;
    }

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

    public static int count(@NotNull String str, char sym) {
        int c = 0;
        for (char ch : str.toCharArray()) {
            if (ch == sym) {
                c++;
            }
        }
        return c;
    }
}
