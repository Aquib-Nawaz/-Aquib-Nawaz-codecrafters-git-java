package com.codecrafter.git.clone;

import java.io.BufferedReader;
import java.io.IOException;

public class PktLine {

    int idx;
    public PktLine(){
    }
    public static String serialize(String content){
        return String.format("%04x%s\n", content.length()+5, content);

    }

    public static String deserialize(BufferedReader in) throws IOException {

        String ret = "";
        char [] lenBuff = new char[4];
        int readLen;

        readLen = in.read(lenBuff, 0 ,4);

        if(readLen!=4)
            throw new IOException("Partial Read");

        int length = Integer.decode("0x"+new String(lenBuff));
        if(length!=0){
            ret = in.readLine();
            if(ret.length() != length-5){
                throw new IOException("Partial Read");
            }
        }
        return ret;
    }
}
