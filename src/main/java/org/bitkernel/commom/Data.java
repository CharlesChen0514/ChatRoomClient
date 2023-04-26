package org.bitkernel.commom;

import com.sun.istack.internal.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.InetAddressValidator;

import static org.bitkernel.commom.StringUtil.*;

@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class Data {
    @Getter
    private String from;
    @Getter
    private CmdType cmdType;
    @Getter
    private String to;
    @Getter
    private String msg;
    private static int FIELD_LEN = 4;
    /** Command connector */
    public final static String sym = "@";
    /** String for data filing */
    private final static String nullStr = "null";

    /**
     * Check validity based on command type.
     * @param dataStr Raw command string without data filing
     * @return valid or not
     */
    public static boolean checkDataStr(@NotNull String dataStr) {
        // dataStr: from@cmd@to@msg
        String[] split = dataStr.split(sym);
        if (split.length != FIELD_LEN) {
            return false;
        } else {
            String from = split[0].trim();
            CmdType type = CmdType.cmdToEnumMap.get(split[1].trim());
            String to = split[2].trim();
            String msg = split[3].trim();
            if (type == null) {
                return false;
            }
            switch (type) {
                case INFO:
                case RECEPTION_REQ_LIST:
                case FRIENDS:
                case EXIT:
                case HELP:
                case ACCEPTED_FILES:
                    return to.equals(nullStr) && msg.equals(nullStr);
                case CONNECT:
                    return (isNumeric(to) && msg.equals(nullStr) ||
                            isValidIp(to) && isNumeric(msg) ||
                            to.equals("local") && isNumeric(msg));
                case PRIVATE_MSG:
                case FILE_TRANSFER:
                    return !to.equals(nullStr) && !msg.equals(nullStr);
                case MAX_FILE_SIZE:
                case REFUSE:
                case ACCEPT:
                case PAUSE:
                    return isNumeric(to) && msg.equals(nullStr);
                case DISCONNECT:
                    return !to.equals(nullStr) && msg.equals(nullStr);
            }
        }
        return true;
    }

    @NotNull
    public static Data parse(@NotNull String dataStr) {
        String[] split = dataStr.split(sym);
        String from = split[0].trim();
        CmdType type = CmdType.cmdToEnumMap.get(split[1].trim());
        String to = split[2].trim();
        String msg = split[3].trim();
        return new Data(from, type, to, msg);
    }

    @NotNull
    public static String formalize(@NotNull String pktStr) {
        int c = count(pktStr, sym.toCharArray()[0]);
        int r = 3 - c;
        StringBuilder sb = new StringBuilder(pktStr);
        while (r > 0) {
            sb.append(sym).append(nullStr);
            r--;
        }
        return sb.toString();
    }

    @NotNull
    @Override
    public String toString() {
        return joinDelimiter(from, cmdType.cmd, to, msg, " ");
    }

    @NotNull
    public String toDataString() {
        return joinDelimiter(from, cmdType.cmd, to, msg, sym);
    }

    public static boolean isValidIp(String ipStr) {
        InetAddressValidator validator = InetAddressValidator.getInstance();
        return validator.isValidInet4Address(ipStr);
    }

    public static void main(String[] args) {
        String ipTest1 = "1.1.1";
        String ipTest2 = "01.1.1";
        String ipTest3 = "01.1.1.1";
        String ipTest4 = ".1.1.1";
        System.out.println(ipTest1 + " " + isValidIp(ipTest1));
        System.out.println(ipTest2 + " " + isValidIp(ipTest2));
        System.out.println(ipTest3 + " " + isValidIp(ipTest3));
        System.out.println(ipTest4 + " " + isValidIp(ipTest4));
    }
}
