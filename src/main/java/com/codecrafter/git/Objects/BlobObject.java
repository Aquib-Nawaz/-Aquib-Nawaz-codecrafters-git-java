package com.codecrafter.git.Objects;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class BlobObject extends GitObjects{

    public BlobObject(String repo, String hash)throws FileNotFoundException {
        super(repo, hash);
        type = new byte[]{'b', 'l', 'o', 'b'};
    }

    @Override
    public void readObject() {
        try {
            super.readObjectFromHash();
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
            fileContent = Files.readAllBytes(Path.of(filename));
            hash = super.writeObject();
        }
        catch (IOException e){
            System.out.println(String.format("Blob Write Exception:- %s", e.getMessage()));
        }
        return hash;
    }
}
