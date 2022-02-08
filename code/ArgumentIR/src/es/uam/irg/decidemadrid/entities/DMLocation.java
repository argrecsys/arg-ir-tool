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

import java.util.Objects;

public class DMLocation {

    private String district;
    private String neighborhood;
    private String location;
    private String tag;

    public DMLocation(String district, String neighborhood, String location, String tag) throws Exception {
        if (district == null) {
            throw new IllegalArgumentException("Null district");
        }
        this.district = district;
        this.neighborhood = neighborhood != null ? neighborhood : "";
        this.location = location != null ? location : "";
        this.tag = tag != null ? tag : "";
    }

    public String getDistrict() {
        return district;
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public String getLocation() {
        return location;
    }

    public String getTag() {
        return tag;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.district);
        hash = 53 * hash + Objects.hashCode(this.neighborhood);
        hash = 53 * hash + Objects.hashCode(this.location);
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
        final DMLocation other = (DMLocation) obj;
        if (!Objects.equals(this.district, other.district)) {
            return false;
        }
        if (!Objects.equals(this.neighborhood, other.neighborhood)) {
            return false;
        }
        if (!Objects.equals(this.location, other.location)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "DMLocation{" + "district=" + district + ", neighborhood=" + neighborhood + ", location=" + location + ", tag=" + tag + '}';
    }
}
