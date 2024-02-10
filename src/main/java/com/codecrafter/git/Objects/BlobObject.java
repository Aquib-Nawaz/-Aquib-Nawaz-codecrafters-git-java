package com.codecrafter.git.Objects;

import java.io.*;
import java.nio.file.Path;

public class BlobObject extends GitObjects{

    byte [] type = {'b', 'l', 'o', 'b'};
    public BlobObject(String repo, String hash)throws FileNotFoundException {
        super(repo, hash);
    }

    @Override
    public void readObject() {
        try {
            super.readObject(type);
            System.out.print(new String(fileContent));
        }
        catch (Exception e){
            System.out.println(String.format("Blob Read Exception:- %s", e.getMessage()));
        }

    }

    @Override
    public byte[] writeObject(String filename) {
        byte[] hash = {};
        try {
            hash = super.writeObject(Path.of(filename),type);
        }
        catch (IOException e){
            System.out.println(String.format("Blob Write Exception:- %s", e.getMessage()));
        }
        return hash;
    }
}
