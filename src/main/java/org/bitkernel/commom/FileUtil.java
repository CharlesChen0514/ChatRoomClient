package org.bitkernel.commom;

import com.sun.istack.internal.NotNull;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;

@Slf4j
public class FileUtil {
    /**
     * Get a set of file name in a specific directory
     * @param fileDir directory path
     * @return file name set
     */
    @NotNull
    public static Set<String> getAllFileNameSet(@NotNull String fileDir) {
        File f = new File(fileDir);
        Set<String> set = new LinkedHashSet<>();
        if (!f.exists()) {
            logger.error(fileDir + " not exists");
            return set;
        }
        File[] files = f.listFiles();
        assert files != null;
        for (File fs : files) {
            set.add(fs.getName());
        }
        return set;
    }

    /**
     * @param fileDir directory path
     * @return string contain all file name
     */
    @NotNull
    public static String getAllFileNameString(@NotNull String fileDir) {
        Set<String> allFileNameSet = getAllFileNameSet(fileDir);
        StringBuilder sb = new StringBuilder();
        if (allFileNameSet.isEmpty()) {
            return sb.toString();
        }
        allFileNameSet.forEach(name -> sb.append(name).append(", "));
        // remove the last delimiter
        sb.deleteCharAt(sb.length() - 1);
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    /**
     * Create a folder that does nothing if it exists,
     * or recursively create if it doesn't.
     * @param dir directory path
     * @return is the creation successful
     */
    public static boolean createFolder(@NotNull String dir) {
        File file = new File(dir);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                logger.error("create folder error: {}", dir);
                return false;
            }
            logger.debug("Create folder success: {}", dir);
        } else {
            logger.debug("Folder already exist: {}", dir);
        }
        return true;
    }

    /**
     * @param dir directory path
     * @param name file name
     * @return exist or not
     */
    public static boolean existInFolder(@NotNull String dir,
                                        @NotNull String name) {
        Set<String> allFileNameSet = getAllFileNameSet(dir);
        return allFileNameSet.contains(name);
    }

    /**
     * @param filePath file path
     * @return exist or not
     */
    public static boolean exist(@NotNull String filePath) {
        File file = new File(filePath);
        return file.exists();
    }
}
