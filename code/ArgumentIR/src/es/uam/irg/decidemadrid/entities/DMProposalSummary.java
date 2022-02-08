/**
 * Copyright 2021
 * Ivan Cantador
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
package es.uam.irg.decidemadrid.entities;

/**
 *
 */
public class DMProposalSummary {

    private String categories;
    private String districts;
    private int id;
    private String topics;

    public DMProposalSummary(int id, String categories, String districts, String topics) {
        this.id = id;
        this.categories = categories;
        this.districts = districts;
        this.topics = topics;
    }

    public String getCategories() {
        return this.categories;
    }

    public String getDistricts() {
        return this.districts;
    }

    public int getId() {
        return id;
    }

    public String getTopics() {
        return this.topics;
    }

}
