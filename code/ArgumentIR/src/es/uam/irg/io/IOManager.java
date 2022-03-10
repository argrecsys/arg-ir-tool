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

import es.uam.irg.nlp.am.arguments.ArgumentLabel;
import es.uam.irg.utils.FunctionUtils;
import es.uam.irg.utils.StringUtils;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.yaml.snakeyaml.Yaml;

/**
 * Input-output manager class.
 */
public class IOManager {

    // Class constants
    private static final String LEXICON_FILEPATH = "Resources/data/argument_lexicon_{}.csv";

    /**
     *
     * @param filepath
     * @return
     */
    public static List<String> readAnnotators(String filepath) {
        List<String> annotators = new ArrayList<>();

        try {
            File txtFile = new File(filepath);

            if (txtFile.exists() && txtFile.isFile()) {
                try ( BufferedReader reader = new BufferedReader(new FileReader(txtFile))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        annotators.add(line.trim());
                    }
                }
            }

        } catch (IOException ex) {
            Logger.getLogger(IOManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        return annotators;
    }

    /**
     *
     * @param filepath
     * @return
     */
    public static Map<String, ArgumentLabel> readDictFromCsvFile(String filepath) {
        Map<String, ArgumentLabel> csvData = new HashMap<>();

        try {
            File csvFile = new File(filepath);

            if (csvFile.exists() && csvFile.isFile()) {
                try ( BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
                    String row;
                    ArgumentLabel label;
                    String id;
                    String relevance;
                    String quality;
                    String timestamp;

                    reader.readLine();
                    while ((row = reader.readLine()) != null) {
                        String[] data = row.split(",");

                        if (data.length == 6) {
                            id = data[1];
                            relevance = data[2];
                            quality = data[3];
                            timestamp = data[4];
                            label = new ArgumentLabel(id, relevance, quality, timestamp);
                            csvData.put(id, label);
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
     * @param lang
     * @return
     */
    public static Map<String, List<String>> readRelationTaxonomy(String lang) {
        Map<String, List<String>> taxonomy = new HashMap<>();
        String taxonomyFilepath = LEXICON_FILEPATH.replace("{}", lang);

        try {
            // Get the file
            File csvFile = new File(taxonomyFilepath);

            // Check if the specified file exists or not
            if (csvFile.exists()) {
                try ( BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
                    String row;
                    String category;
                    String subCategory;

                    reader.readLine();
                    while ((row = reader.readLine()) != null) {
                        String[] data = row.split(",");

                        if (data.length == 6) {
                            category = StringUtils.toTitleCase(data[2]);
                            subCategory = StringUtils.toTitleCase(data[3]);

                            if (!taxonomy.containsKey(category)) {
                                taxonomy.put(category, new ArrayList<>());
                            }
                            if (!taxonomy.get(category).contains(subCategory)) {
                                taxonomy.get(category).add(subCategory);
                            }
                        }
                    }
                }
            }

        } catch (IOException ex) {
            Logger.getLogger(IOManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        return taxonomy;
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
     * @param header
     * @param csvData
     * @param userName
     * @param sorted
     * @return
     */
    public static boolean saveDictToCsvFile(String filepath, String[] header, Map<String, ArgumentLabel> csvData, String userName, boolean sorted) {
        boolean result = false;

        if (csvData.size() > 0) {

            // Sorting data
            if (sorted) {
                csvData = sortLabelData(csvData);
            }

            // Add data
            StringBuilder sb = new StringBuilder();
            sb.append(String.join(",", header)).append("\n");

            csvData.entrySet().forEach(entry -> {
                ArgumentLabel label = entry.getValue();
                sb.append(label.toString())
                        .append(",")
                        .append(userName)
                        .append("\n");
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

    /**
     *
     * @param map
     * @return
     */
    private static Map<String, ArgumentLabel> sortLabelData(Map<String, ArgumentLabel> map) {
        LinkedHashMap<String, ArgumentLabel> reverseSortedMap = new LinkedHashMap<>();

        // Use Comparator.reverseOrder() for reverse ordering
        map.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue((v1, v2) -> Integer.compare(v1.getProposalId(), v2.getProposalId())))
                .forEachOrdered(x -> reverseSortedMap.put(x.getKey(), x.getValue()));

        return reverseSortedMap;
    }

}
