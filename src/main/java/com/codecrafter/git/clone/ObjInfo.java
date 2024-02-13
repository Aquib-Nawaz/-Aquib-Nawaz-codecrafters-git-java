package com.codecrafter.git.clone;

public class ObjInfo {
    public long len;
    public int offset;

    public ObjInfo(int type, long len, int offset) {
        this.len = len;
        this.type = type;
        this.offset = offset;
    }

    public int type;
}
