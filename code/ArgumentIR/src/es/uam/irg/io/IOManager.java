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
import es.uam.irg.utils.FileUtils;
import es.uam.irg.utils.StringUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Input-output manager class.
 */
public class IOManager {

    // Class constants
    public static final String MONGO_DB = "MONGO_DB";
    public static final String MYSQL_DB = "MYSQL_DB";
    private static final String LEXICON_FILEPATH = "Resources/data/argument_lexicon_{}.csv";
    private static final String MDB_SETUP_FILEPATH = "Resources/config/mdb_setup.yaml";
    private static final String MSQL_SETUP_FILEPATH = "Resources/config/msql_setup.yaml";

    /**
     *
     * @param dbType
     * @return
     */
    public static Map<String, Object> getDatabaseConfiguration(String dbType) {
        Map<String, Object> setup = null;

        if (dbType.equals(MYSQL_DB)) {
            setup = FileUtils.readYamlFile(MSQL_SETUP_FILEPATH);
        } else if (dbType.equals(MONGO_DB)) {
            setup = FileUtils.readYamlFile(MDB_SETUP_FILEPATH);
        }

        return setup;
    }

    /**
     *
     * @param filepath
     * @return
     */
    public static Map<String, ArgumentLabel> readArgumentLabelList(String filepath) {
        Map<String, ArgumentLabel> csvData = new HashMap<>();
        List<String[]> data = FileUtils.readCsvFile(filepath, false);

        if (data.size() > 0) {
            for (String[] row : data) {
                if (row.length == 6) {
                    String id = row[1];
                    String relevance = row[2];
                    String quality = row[3];
                    String timestamp = row[4];
                    ArgumentLabel label = new ArgumentLabel(id, relevance, quality, timestamp);
                    csvData.put(id, label);
                }
            }
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
                    String content = FileUtils.readFile(filepath);
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
    public static List<String> readUsers(String filepath) {
        List<String> users = new ArrayList<>();

        // Get the file
        String content = FileUtils.readFile(filepath);

        // Check if the specified file exists or not
        if (!StringUtils.isEmpty(content)) {
            String[] rows = content.split("\n");
            for (String user : rows) {
                users.add(user.replace("\r", ""));
            }
        }

        return users;
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
    public static boolean saveArgumentLabelList(String filepath, String[] header, Map<String, ArgumentLabel> csvData, String userName, boolean sorted) {
        boolean result = false;

        if (csvData.size() > 0) {

            // Sorting data
            if (sorted) {
                csvData = sortLabelData(csvData);
            }

            // Collect data
            List<String[]> data = new ArrayList<>();
            data.add(header);

            csvData.entrySet().forEach(entry -> {
                ArgumentLabel label = entry.getValue();
                String s = label.toString() + "," + userName;
                String[] row = s.split(",");
                data.add(row);
            });

            // Save data
            result = FileUtils.saveCsvFile(filepath, data);
        }

        return result;
    }

    /**
     *
     * @param filename
     * @return
     */
    private static String getFileName(String filename) {
        filename = FileUtils.getFilenameWithoutExt(filename);
        return filename.replace("-", "_").toUpperCase();
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
