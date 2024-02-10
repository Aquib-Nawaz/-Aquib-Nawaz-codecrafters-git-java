package com.codecrafter.git.Objects;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Stream;
import java.nio.file.Path;

class TreeLeaf {
    public String mode,name,sha;
    int idx;

}
public class TreeObject extends GitObjects{
    byte [] type = {'t', 'r', 'e', 'e'};

    public TreeObject(String repo, String hash) throws FileNotFoundException {
        super(repo, hash);
    }

    private TreeLeaf readTreeLeaf(int idx){
        int valueLen=0;
        TreeLeaf ret = new TreeLeaf();

        if(fileContent[idx+5]==' '){
            valueLen = 5;
        }
        else if(fileContent[idx+6]==' ') {
            valueLen = 6;
        }
        ret.mode = new String(fileContent, idx, valueLen);
        idx += valueLen+1;

        valueLen = 0;
        while(fileContent[idx+valueLen]!=0)
            valueLen++;
        ret.name = new String(fileContent, idx, valueLen);
        idx += valueLen+1;

        valueLen = 20;
        byte[] sha = new byte[20];
        System.arraycopy(fileContent, idx, sha, 0, valueLen);
        ret.sha = byteArray2Hex(sha);
        idx += valueLen;

        ret.idx = idx;
        return ret;
    }

    @Override
    public void readObject() {
        super.readObject(type);
//        [mode name020]
        int cur = 0;
        Set<String> names = new TreeSet<>();
        while(cur!=fileContent.length) {
            TreeLeaf ret = readTreeLeaf(cur);
            cur = ret.idx;
            names.add(ret.name);
        }

        for(String name:names)
            System.out.println(name);
    }

    @Override
    public byte[] writeObject(String filename) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte [] sha = new byte[1];
        try(Stream<Path> fileStream  = Files.list(Path.of(filename))){
//            buffer.write();
            fileStream.
                    filter(e->!e.getFileName().toString().equals(".git"))
                    .sorted(Path::compareTo).forEach(p->{
                        String fileName = p.getFileName().toString();
//                        System.out.printf("Parsing :- %s\n", fileName);
                        GitObjects child;
                        try {
                            if(Files.isDirectory(p)){
                                child = new TreeObject(".", "");
                                buffer.write("040000".getBytes());
                            }
                            else{
                                buffer.write("100644".getBytes());
                                child = new BlobObject(".", "");
                            }
                            buffer.write(" ".getBytes());
                            buffer.write(fileName.getBytes());
                            byte[] childSha = child.writeObject(p.toString());
                            buffer.write(0);
                            buffer.write(childSha);
                        }
                        catch (IOException f){}

                    });
            fileContent = buffer.toByteArray();
            sha = super.writeObject(type);
        }
        catch (IOException e){
            System.out.printf("Tree Write Exception for dir %s\n", filename);
        }
        return sha;
    }
}
