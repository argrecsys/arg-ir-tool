/**
 * Copyright 2022
 * Ivan Cantador and Andrés Segura-Tinoco
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DMCommentTree {

    private int id;
    private int level;
    private List<DMCommentTree> parents;
    private List<DMCommentTree> children;

    public DMCommentTree(int id, int level) {
        this(id, level, new ArrayList<>());
    }

    public DMCommentTree(int id, int level, List<DMCommentTree> parents) {
        this.id = id;
        this.level = level;
        this.parents = parents;
        this.children = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public int getLevel() {
        return level;
    }

    public List<DMCommentTree> getParents() {
        return parents;
    }

    public List<DMCommentTree> getChildren() {
        return children;
    }

    private void setLevel(int level) {
        this.level = level;
    }

    public void addParent(DMCommentTree parent) {
        if (!this.parents.contains(parent)) {
            this.parents.add(parent);
        }
    }

    public boolean addChild(DMCommentTree node) {
        List<DMCommentTree> nodeParents = node.getParents();
        if (nodeParents.contains(this)) {
            if (!this.children.contains(node)) {
                node.addParent(this);
                node.setLevel(this.level + 1);
                this.children.add(node);
            }
            return true;
        }

        for (DMCommentTree child : this.children) {
            if (child.addChild(node)) {
                return true;
            }
        }

        return false;
    }

    public void expand(List<DMComment> comments) {
        for (DMComment comment : comments) {
            int commentId = comment.getId();
            int parentId = comment.getParentId();
            if (this.id == parentId) {
                DMCommentTree child = new DMCommentTree(commentId, this.level + 1);
                if (!this.children.contains(child)) {
                    this.children.add(child);
                    child.expand(comments);
                }
            }
        }
    }

    public void countNodesPerLevel(Map<Integer, Integer> nodesAtLevel) {
        if (!nodesAtLevel.containsKey(this.level)) {
            nodesAtLevel.put(this.level, 0);
        }
        int n = nodesAtLevel.get(this.level) + 1;
        nodesAtLevel.put(this.level, n);
        for (DMCommentTree child : this.children) {
            child.countNodesPerLevel(nodesAtLevel);
        }
    }

    public int countDepth() {
        int maxChildDepth = 0;
        for (DMCommentTree child : this.children) {
            int d = child.countDepth();
            if (d > maxChildDepth) {
                maxChildDepth = d;
            }
        }
        return 1 + maxChildDepth;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + this.id;
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
        final DMCommentTree other = (DMCommentTree) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        String s = "";
        for (int i = 0; i < this.level; i++) {
            s += "\t";
        }
        s += this.id + "\n";
        for (DMCommentTree node : this.children) {
            s += node.toString();
        }
        return s;
    }

    public String toString(Map<Integer, DMComment> comments) {
        String s = "";
        for (int i = 0; i < this.level; i++) {
            s += "\t";
        }
        s += this.id + " (u" + comments.get(this.id).getUserId() + ")\t" + comments.get(this.id).getText() + "\n";
        for (DMCommentTree node : this.children) {
            s += node.toString(comments);
        }
        return s;
    }

}
