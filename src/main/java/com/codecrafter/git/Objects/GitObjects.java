package com.codecrafter.git.Objects;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;

public abstract class GitObjects {
    protected Path file;
    public GitObjects(String repo, String hash){
        int length = hash.length();
        assert (length>2);
        file = Path.of(repo, ".git", "objects" ,hash.substring(0,2), hash.substring(2,length));
        if(!Files.exists(file)){
            try {
                Files.createFile(file);
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
        else{
//            System.out.println("File Exists\n");
        }
    }

    public abstract void readObject();
    public abstract void writeObject();


}
