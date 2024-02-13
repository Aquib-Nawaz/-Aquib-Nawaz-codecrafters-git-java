package com.codecrafter.git.clone;

import java.util.Arrays;

public class DeltaInstruction {

    private int type;
    private byte [] bufferData;

    private int srcOffset;
    private int size;

    private byte[] fileData;

    public DeltaInstruction(int type, int size, int srcOffset, byte[] data ){
        this.type = type;
        this.srcOffset = srcOffset;
        this.size = size;
        this.bufferData = data;
    }

    public void setFileData(byte [] data){
        fileData = data;
    }
    public  byte[] getContent(){
        if(type==1){
            //Instruction
            return bufferData;
        }
        return Arrays.copyOfRange(fileData, srcOffset, srcOffset+size);
    }
}
