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

import es.uam.irg.utils.StringUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.yaml.snakeyaml.Yaml;

/**
 * Input-output manager class.
 */
public class IOManager {

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
     * @param folderPath
     * @return
     */
    public static Map<String, String> readHtmlReports(String folderPath) {
        Map<String, String> reports = null;

        if (!StringUtils.isEmpty(folderPath)) {
            reports = new HashMap<>();
            File folder = new File(folderPath);
            Path filepath;
            String filename;
            String content;

            for (File fileEntry : folder.listFiles()) {
                if (fileEntry.isFile()) {
                    try {
                        filepath = Paths.get(fileEntry.getPath());
                        filename = getNameWithoutExt(fileEntry.getName()).replace("-", "_").toUpperCase();
                        content = Files.readString(filepath, StandardCharsets.US_ASCII);
                        reports.put(filename, content);

                    } catch (IOException ex) {
                        Logger.getLogger(IOManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }

        return reports;
    }

    /**
     *
     * @param filename
     * @return
     */
    private static String getNameWithoutExt(String filename) {
        int index = filename.lastIndexOf(".");
        if (index == -1) {
            return filename;
        }
        return filename.substring(0, index);
    }

}
