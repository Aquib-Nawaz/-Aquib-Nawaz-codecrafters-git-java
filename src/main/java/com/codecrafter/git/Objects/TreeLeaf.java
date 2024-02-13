package com.codecrafter.git.Objects;

public class TreeLeaf implements Comparable<TreeLeaf>{
    public String mode,name,sha;
    int idx;

    @Override
    public int compareTo(TreeLeaf o) {
        return name.compareTo(o.name);
    }
}