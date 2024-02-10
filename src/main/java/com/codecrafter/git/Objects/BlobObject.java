package com.codecrafter.git.Objects;

import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.zip.InflaterInputStream;

public class BlobObject extends GitObjects{

    public BlobObject(String repo, String hash) {
        super(repo, hash);
    }

    @Override
    public void readObject() {
        try(InputStream stream = Files.newInputStream(file)) {
            InflaterInputStream inflatedStream = new InflaterInputStream(stream);
            Reader reader = new InputStreamReader(inflatedStream);
            BufferedReader bufferedReader = new BufferedReader(reader);
            int readLen;
            char [] type = new char[4];
            readLen = bufferedReader.read(type, 0, 4);
            assert (readLen == 4);
            assert (Arrays.equals(type,"blob".toCharArray()));
            int space = bufferedReader.read();
            assert(space==(int)' ');
            int reading = bufferedReader.read();

            int fileSize=0;

            while(reading !=-1){
                reading -= (int)'0';
                assert ( reading<10 && reading >=0);
                fileSize = fileSize*10 +reading;
                reading = bufferedReader.read();
                if(reading==0)break;
            }

            char [] fileContent = new char[fileSize];
//            All the data read matches the size
            assert fileSize == bufferedReader.read(fileContent, 0, fileSize);
            System.out.print(fileContent);
//            End of file reached
            assert bufferedReader.read() == -1;
        }
        catch (IOException e){
            System.out.println("Couldn't Read File\n");
        }
        catch(NumberFormatException e){
            System.out.println("Wrong file format");
        }
    }

    @Override
    public void writeObject() {

    }
}
