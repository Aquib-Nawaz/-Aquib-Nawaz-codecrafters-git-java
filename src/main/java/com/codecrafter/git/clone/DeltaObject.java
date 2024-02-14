package com.codecrafter.git.clone;

import com.codecrafter.git.Objects.GitObjects;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public class DeltaObject extends GitObjects {
    private List<DeltaInstruction>instructions;

    private long srcSize, tgtSize;

    private String baseObjHash;

    public DeltaObject(String _repo, String baseObjHash) throws FileNotFoundException {
        super(_repo, "");
        type = "delta".getBytes();
        this.baseObjHash = baseObjHash;
    }

    public void setDeltaData(List<DeltaInstruction>instructions,
                             long srcSize, long tgtSize){
        this.instructions = instructions;
        this.srcSize = srcSize;
        this.tgtSize = tgtSize;
    }

    @Override
    public void readObject() {
        return;
    }

    @Override
    public byte[] writeObject(String filename) {
        try{
            super.setFile(baseObjHash);
        }
        catch (FileNotFoundException f){
            System.out.printf("Delta File Can't Read Base file:- %s %s\n", baseObjHash, f.getMessage());
            return new byte[0];
        }
        super.readObjectFromHash();
        //Src file in fileContentArray

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()){
            for(DeltaInstruction instruction:instructions){
                instruction.setFileData(fileContent);
                os.write(instruction.getContent());
            }
            fileContent = os.toByteArray();
            return writeObject();
        }
        catch (IOException e){}
        return new byte[0];
    }

}
