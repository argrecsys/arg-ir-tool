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
package es.uam.irg.decidemadrid.entities;

import java.util.Objects;

public class DMTopic {

    private String name;
    private double weight;
    private double nWeight;
    private String source;

    public DMTopic(String name, double weight, double nWeight, String source) {
        this.name = name;
        this.weight = weight;
        this.nWeight = nWeight;
        this.source = source;
    }

    public String getName() {
        return name;
    }

    public double getWeight() {
        return weight;
    }

    public double getNormalizedWeight() {
        return nWeight;
    }

    public String getSource() {
        return source;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.name);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DMTopic other = (DMTopic) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "DMTopic{" + "name=" + name + ", weight=" + weight + ", nWeight=" + nWeight + ", source=" + source + '}';
    }

}
