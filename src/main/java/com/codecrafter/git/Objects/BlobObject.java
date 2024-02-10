package com.codecrafter.git.Objects;

import java.io.*;
import java.nio.file.Files;
import java.util.zip.InflaterInputStream;

public class BlobObject extends GitObjects{

    public BlobObject(String repo, String hash) {
        super(repo, hash);
    }

    @Override
    public void readObject() {
        try(InputStream stream = Files.newInputStream(file)) {
            InflaterInputStream inflatedStream = new InflaterInputStream(stream);
            BufferedInputStream bufferedStream = new BufferedInputStream(inflatedStream);
            int b;
            while ((b = bufferedStream.read()) != -1) {
                System.out.print((char) b);
            }
        }
        catch (IOException e){

        }
    }

    @Override
    public void writeObject() {

    }
}
