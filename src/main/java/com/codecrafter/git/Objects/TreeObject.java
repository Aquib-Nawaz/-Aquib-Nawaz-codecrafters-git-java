package com.codecrafter.git.Objects;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Stream;
import java.nio.file.Path;

public class TreeObject extends GitObjects{

    public TreeObject(String repo, String hash) throws FileNotFoundException {
        super(repo, hash);
        type = new byte[]{'t', 'r', 'e', 'e'};
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

    public Set<TreeLeaf> getEntries(){
        super.readObjectFromHash();
//        [mode name020]
        int cur = 0;
        Set<TreeLeaf> leaves = new TreeSet<>();
        while(cur!=fileContent.length) {
            TreeLeaf ret = readTreeLeaf(cur);
            cur = ret.idx;
            leaves.add(ret);
        }
        return leaves;
    }
    @Override
    public void readObject() {
        Set<TreeLeaf>leaves = getEntries();
        for(TreeLeaf leaf:leaves)
            System.out.println(leaf.name);
    }

    @Override
    public byte[] writeObject(String filename) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte [] sha = new byte[0];
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
                                buffer.write("40000".getBytes());
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
            sha = super.writeObject();
        }
        catch (IOException e){
            System.out.printf("Tree Write Exception for dir %s\n", filename);
        }
        return sha;
    }
}
