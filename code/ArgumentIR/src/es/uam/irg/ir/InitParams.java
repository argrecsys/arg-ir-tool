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
package es.uam.irg.ir;

import es.uam.irg.utils.FileUtils;
import es.uam.irg.utils.StringUtils;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;

/**
 * Helper for loading application input parameters.
 */
public class InitParams {

    private final static String FILE_PATH = "Resources/config/params.json";

    /**
     *
     * @return
     */
    public static Map<String, Object> readInitParams() {
        Map<String, Object> params = new HashMap<>();
        String jsonText = FileUtils.readFile(FILE_PATH);

        if (!StringUtils.isEmpty(jsonText)) {
            JSONObject json = new JSONObject(jsonText);

            if (!json.isEmpty()) {
                JSONObject data;

                // General parameters
                String lang = json.getString("language");
                params.put("language", lang);

                String datapath = json.getString("data_path");
                params.put("data_path", datapath);
            }
        }

        return params;
    }

}
