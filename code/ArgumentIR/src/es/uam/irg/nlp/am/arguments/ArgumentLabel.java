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
    private final String label;
    private final int proposalId;
    private final String timeStamp;

    /**
     *
     * @param argumentId
     * @param label
     * @param timeStamp
     */
    public ArgumentLabel(String argumentId, String label, String timeStamp) {
        this.argumentId = argumentId;
        this.label = label;
        this.timeStamp = timeStamp;

        if (!StringUtils.isEmpty(this.argumentId)) {
            var token = StringUtils.getFirstToken(this.argumentId, "-");
            this.proposalId = Integer.parseInt(token);

        } else {
            this.proposalId = -1;
        }
    }

    public String getLabel() {
        return this.label;
    }

    public int getProposalId() {
        return this.proposalId;
    }

    @Override
    public String toString() {
        return String.format("%d,%s,%s,%s", proposalId, argumentId, label, timeStamp);
    }
}
