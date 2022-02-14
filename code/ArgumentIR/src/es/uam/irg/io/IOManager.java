/**
 * Copyright 2022
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
package es.uam.irg.io;

import es.uam.irg.utils.FunctionUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.yaml.snakeyaml.Yaml;

/**
 * Input-output manager class.
 */
public class IOManager {

    private static final String DATESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     *
     * @param filepath
     * @return
     */
    public static Map<String, String> readDictFromCsvFile(String filepath) {
        Map<String, String> csvData = new HashMap<>();

        try {
            File csvFile = new File(filepath);

            if (csvFile.exists() && csvFile.isFile()) {
                try ( BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
                    String row;
                    String id;
                    String value;

                    reader.readLine();
                    while ((row = reader.readLine()) != null) {
                        String[] data = row.split(",");

                        if (data.length == 2) {
                            id = data[0];
                            value = data[1];
                            csvData.put(id, value);
                        }
                    }
                }
            }

        } catch (IOException ex) {
            Logger.getLogger(IOManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        return csvData;
    }

    /**
     *
     * @param folderPath
     * @return
     */
    public static Map<String, String> readHtmlReports(String folderPath) {
        Map<String, String> reports = new HashMap<>();
        File folder = new File(folderPath);

        if (folder.exists() && folder.isDirectory()) {
            for (File fileEntry : folder.listFiles()) {
                if (fileEntry.isFile()) {
                    Path filepath = Paths.get(fileEntry.getPath());
                    String filename = getFileName(fileEntry.getName());
                    String content = readFile(filepath);
                    reports.put(filename, content);
                }
            }
        }

        return reports;
    }

    /**
     *
     * @param filepath
     * @return
     */
    public static Map<String, Object> readYamlFile(String filepath) {
        Map<String, Object> data = null;

        try {
            // Get the file
            File yamlFile = new File(filepath);

            // Check if the specified file exists or not
            if (yamlFile.exists()) {
                InputStream inputStream = new FileInputStream(yamlFile);
                Yaml yaml = new Yaml();
                data = (Map<String, Object>) yaml.load(inputStream);
            }

        } catch (IOException ex) {
            Logger.getLogger(IOManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        return data;
    }

    /**
     *
     * @param filepath
     * @param csvData
     * @return
     */
    public static boolean saveDictFromCsvFile(String filepath, Map<String, String> csvData) {
        boolean result = false;

        if (csvData.size() > 0) {

            // Add data
            String header = "arg_id,label,timestamp\n";
            StringBuilder sb = new StringBuilder(header)  ;          
            
            csvData.entrySet().forEach(entry -> {
                String timeStamp = DateTimeFormatter.ofPattern(DATESTAMP_FORMAT).format(LocalDateTime.now());
                String file = entry.getKey() + "," + entry.getValue() + "," + timeStamp + "\n";
                sb.append(file);
            });

            // Save data
            try ( PrintWriter out = new PrintWriter(filepath)) {
                out.println(sb.toString());
                result = true;

            } catch (FileNotFoundException ex) {
                Logger.getLogger(IOManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return result;
    }

    /**
     *
     * @param filename
     * @return
     */
    private static String getFileName(String filename) {
        filename = FunctionUtils.getFilenameWithoutExt(filename);
        return filename.replace("-", "_").toUpperCase();
    }

    /**
     *
     * @param filepath
     * @return
     */
    private static String readFile(Path filepath) {
        String content = "";
        try {
            content = Files.readString(filepath, StandardCharsets.US_ASCII);
        } catch (IOException ex) {
            Logger.getLogger(IOManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return content;
    }

}
