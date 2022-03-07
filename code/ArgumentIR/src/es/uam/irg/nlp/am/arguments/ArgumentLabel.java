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
package es.uam.irg.nlp.am.arguments;

import es.uam.irg.utils.StringUtils;

/**
 *
 * @author Usuario
 */
public class ArgumentLabel {

    private final String argumentId;
    private final int proposalId;
    private final String quality;
    private final String relevance;
    private final String timeStamp;

    /**
     *
     * @param argumentId
     * @param relevance
     * @param quality
     * @param timeStamp
     */
    public ArgumentLabel(String argumentId, String relevance, String quality, String timeStamp) {
        this.argumentId = argumentId;
        this.relevance = labelQuality(relevance);
        this.quality = labelQuality(quality);
        this.timeStamp = timeStamp;

        if (!StringUtils.isEmpty(this.argumentId)) {
            var token = StringUtils.getFirstToken(this.argumentId, "-");
            this.proposalId = Integer.parseInt(token);

        } else {
            this.proposalId = -1;
        }
    }

    public int getProposalId() {
        return this.proposalId;
    }

    /**
     * Get the argument rhetoric quality.
     *
     * @return
     */
    public String getQuality() {
        return this.quality;
    }

    /**
     * Get the argument topical relevance.
     *
     * @return
     */
    public String getRelevance() {
        return this.relevance;
    }

    @Override
    public String toString() {
        return String.format("%d,%s,%s,%s,%s", proposalId, argumentId, relevance, quality, timeStamp);
    }

    /**
     *
     * @param value
     * @return
     */
    private String labelQuality(String value) {
        return value.toUpperCase().replace(" ", "_");
    }
}
