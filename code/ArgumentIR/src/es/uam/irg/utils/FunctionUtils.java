/**
 * Copyright 2021
 * Andrés Segura-Tinoco
 * Information Retrieval Group at Universidad Autonoma de Madrid
 *
 * This is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * the current software. If not, see <http://www.gnu.org/licenses/>.
 */
package es.uam.irg.utils;

//import es.uam.irg.io.IOManager;
import es.uam.irg.io.IOManager;
import java.awt.Color;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;

/**
 * Class with a set of static utility functions.
 */
public class FunctionUtils {

    // Class constants
    public static final String MONGO_DB = "MONGO_DB";
    public static final String MYSQL_DB = "MYSQL_DB";
    private static final String MDB_SETUP_FILEPATH = "Resources/config/mdb_setup.yaml";
    private static final String MSQL_SETUP_FILEPATH = "Resources/config/msql_setup.yaml";

    // Class variables
    private static DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    /**
     *
     * @param <T>
     * @param array
     * @param delimiter
     * @return
     */
    public static <T> String arrayToString(T[] array, String delimiter) {
        String result = "";

        if (array != null && array.length > 0) {
            StringBuilder sb = new StringBuilder();

            for (T item : array) {
                sb.append(item.toString()).append(delimiter);
            }

            result = sb.deleteCharAt(sb.length() - 1).toString();
        }

        return result;
    }

    /**
     *
     * @param color
     * @return
     */
    public static String colorToHex(Color color) {
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        String hex = String.format("#%02x%02x%02x", r, g, b);
        return hex;
    }

    /**
     *
     * @param array
     * @return
     */
    public static List<String> createListFromText(String array) {
        array = array.replace("[", "").replace("]", "");
        return new ArrayList<>(Arrays.asList(array.split(",")));
    }

    /**
     *
     * @param dbType
     * @return
     */
    public static Map<String, Object> getDatabaseConfiguration(String dbType) {
        Map<String, Object> setup = null;

        if (dbType.equals(MYSQL_DB)) {
            setup = IOManager.readYamlFile(MSQL_SETUP_FILEPATH);
        } else if (dbType.equals(MONGO_DB)) {
            setup = IOManager.readYamlFile(MDB_SETUP_FILEPATH);
        }

        return setup;
    }

    /**
     *
     * @param filename
     * @return
     */
    public static String getFilenameWithoutExt(String filename) {
        int index = filename.lastIndexOf(".");
        if (index == -1) {
            return filename;
        }
        return filename.substring(0, index);
    }

    /**
     *
     * @param <T>
     * @param array
     * @param startIx
     * @param endIndex
     * @return
     * @throws java.lang.Exception
     */
    public static <T> T[] getSubArray(T[] array, int startIx, int endIndex) throws Exception {
        T[] newArray = null;

        if (startIx >= 0 && endIndex <= array.length) {
            newArray = Arrays.copyOfRange(array, startIx, endIndex);
        }

        return newArray;
    }

    /**
     *
     * @param msg
     */
    public static void printWithDatestamp(String msg) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                System.out.println(msg + " - " + dateFormat.format(new Date()));
            }
        });
    }

    /**
     *
     * @param map
     * @return
     */
    public static Map<Integer, Double> sortMapByDblValue(Map<Integer, Double> map) {
        LinkedHashMap<Integer, Double> reverseSortedMap = new LinkedHashMap<>();

        // Use Comparator.reverseOrder() for reverse ordering
        map.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEachOrdered(x -> reverseSortedMap.put(x.getKey(), x.getValue()));

        return reverseSortedMap;
    }

    /**
     *
     * @param map
     * @return
     */
    public static Map<Integer, Integer> sortMapByIntValue(Map<Integer, Integer> map) {
        LinkedHashMap<Integer, Integer> reverseSortedMap = new LinkedHashMap<>();

        // Use Comparator.reverseOrder() for reverse ordering
        map.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEachOrdered(x -> reverseSortedMap.put(x.getKey(), x.getValue()));

        return reverseSortedMap;
    }

    /**
     *
     * @param filepath
     * @param content
     * @return
     */
    public static boolean writeStringToFile(String filepath, String content) {
        boolean result = false;
        try {
            if (!filepath.isEmpty() && !content.isEmpty()) {
                Files.write(Paths.get(filepath), content.getBytes());
                result = true;
            }
        } catch (IOException ex) {
            Logger.getLogger(FunctionUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
