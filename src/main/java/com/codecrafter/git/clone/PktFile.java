package com.codecrafter.git.clone;

import com.codecrafter.git.Objects.BlobObject;
import com.codecrafter.git.Objects.CommitObject;
import com.codecrafter.git.Objects.GitObjects;
import com.codecrafter.git.Objects.TreeObject;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class PktFile {

    private static final int COPY_OFFSET_BIT_LEN = 4;
    private static final int COPY_SIZE_BIT_LEN = 3;
    int idx;
    public PktFile(){
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

    public static long bigEndian(byte [] in, int offset){
        long ret  = 0;//bigendian
        for(int i=0;i<4; i++)
            ret = ret*256 + in[offset+i];
        return ret;
    }

    public static byte[] parseLine(InputStream in) throws IOException{
        byte [] lenbuff = in.readNBytes(4);
        int readLen = Integer.decode("0x"+new String(lenbuff));
        if (readLen==0){
            return null;
        }
        int nextByte = in.read();
        if(nextByte==2)
            System.out.printf("remote:- %s",new String(in.readNBytes((int)readLen-5)));
        else if(nextByte==1){
            return in.readNBytes(readLen-5);
        }
        else {
            byte[] ret = new byte[readLen - 4];
            ret[0] = (byte) nextByte;
            in.readNBytes(ret, 1, readLen - 5);
            System.out.printf("host:- %s", new String(ret));
        }
        return new byte[0];
    }

    static ObjInfo objInfo(byte [] in, int offset){
        byte first = in[offset++];
        int type = ( first>>4&7);
        int msb = first>>7;
        long len = first&15;
//        System.out.printf("New Object:- %d ", first);
        int iter = 0;

        while(msb!=0){
            byte val = in[offset++];
//            System.out.printf("%d ", val);
            msb = val >> 7;
            len += (long)(val&127)<<(4 + iter*7);
            iter++;
        }
        System.out.println();
        return new ObjInfo(type, len, iter+1);
    }
    public static void parsePackfile(InputStream in, String repo) throws IOException{
        byte[] parsedContent;
//        boolean first=true;
        parseLine(in);
        parsedContent  = in.readAllBytes();

        int offset = 0;
        byte [] magic = Arrays.copyOfRange(parsedContent,offset,offset+4);
        if(!Arrays.equals( "PACK".getBytes(), magic)){
            System.out.printf("Magic: -%s does not match\n",new String(magic));
        }
        offset += 4;
        long version = bigEndian(parsedContent, offset);
        offset += 4;
        long numObject = bigEndian(parsedContent, offset);
        offset += 4;
        System.out.printf("debug:- %d %d\n", version, numObject);

        GitObjects curObj = null;
        List<DeltaObject> retObjList = new ArrayList<>();
        for(long i=0; i<numObject&&offset<parsedContent.length-20; i++){

            ObjInfo obj = objInfo(parsedContent, offset); //Header
            offset += obj.offset;

            if(obj.type == 1)
                curObj = new CommitObject(repo, "");
            else if (obj.type == 2) {
                curObj = new TreeObject(repo, "");
            }
            else if (obj.type == 3) {
                curObj = new BlobObject(repo, "");
            }

            if(obj.type==7){
                byte[] hash = Arrays.copyOfRange(parsedContent,offset, offset+20);
                offset += 20; //sha-1 of base
                curObj = new DeltaObject(repo, GitObjects.byteArray2Hex(hash));
                //Delta File Fuck You
            }

            Inflater inflater = new Inflater();
            inflater.setInput(parsedContent, offset, parsedContent.length-offset);
            inflater.finished();
            byte[] fileContent = new byte[(int)obj.len];
            try {
                inflater.inflate(fileContent);
            }
            catch (DataFormatException e)
            {System.out.printf("DataFormatException:- %s", e.getMessage());}

            long lenRead = inflater.getBytesRead();
            offset += (int)lenRead;

            if(!inflater.finished()){
                System.out.printf("Error:- Inflater is not finished\n");
            }

            inflater.end();

            if(obj.type == 4 || obj.type == 6){
                System.out.printf("Error:- Unsupported File Format %d\n", obj.type);
                continue;
            }

            if(obj.type !=7 ) {
                if(curObj!=null){
                    curObj.setFileContent(fileContent);
                    curObj.writeObject();
                }
//                System.out.printf("File %d:- %s\n",i, new String(fileContent));
                continue;
            }

            retObjList.add(DeltaParser.parse(fileContent, (DeltaObject) curObj));
        }
        for(DeltaObject delta:retObjList){
            delta.writeObject(".");
        }
    }

}
