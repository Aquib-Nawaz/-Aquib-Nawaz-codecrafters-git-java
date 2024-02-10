package com.codecrafter.git.Objects;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;


public abstract class GitObjects {
    protected Path file;
    protected char [] fileContent;
    public GitObjects(String repo, String hash) throws FileNotFoundException {

        int length = hash.length();
        if(length!=0){
            assert (length>2);
            file = Path.of(repo, ".git", "objects" ,hash.substring(0,2), hash.substring(2,length));
            if(!Files.exists(file)){
               throw new FileNotFoundException();
            }
        }

    }

    public int charArrayToInt(InputStream stream) throws NumberFormatException, IOException
    {
        int reading = stream.read();
        int fileSize = 0;
        while(reading !=-1){
            reading -= (int)'0';
            assert ( reading<10 && reading >=0);
            fileSize = fileSize*10 +reading;
            reading = stream.read();
            if(reading==0)break;
        }
        return fileSize;
    }

    public byte [] intToByteArray(int value){
        int temp = value, len=0;
        while(temp>0){
            temp/=10;
            len += 1;
        }
        if(len==0)len=1;
        byte [] ret = new byte[len];
        while(len!=0){
            ret[--len] = (byte)(value%10);
            value /= 10;
        }
        return ret;
    }
    protected void readObject(byte [] typeFrom){
        try(InputStream stream = Files.newInputStream(file)) {

            InflaterInputStream inflatedStream = new InflaterInputStream(stream);
//            Reader reader = new InputStreamReader(inflatedStream);
            BufferedInputStream bufferedReader = new BufferedInputStream(inflatedStream);

            int readLen;

            byte [] type = new byte[typeFrom.length];
            readLen = bufferedReader.read(type, 0, typeFrom.length);

            assert (readLen == typeFrom.length);
            assert (Arrays.equals(type,typeFrom));

            int space = bufferedReader.read();
            assert(space==(int)' ');

            int fileSize = charArrayToInt(bufferedReader);
            fileContent = new char[fileSize];
//            All the data read matches the size
            readLen = new InputStreamReader(bufferedReader).read(fileContent, 0, fileSize);
            assert readLen == fileSize;
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

    public static String SHAsum(byte[] convertme) throws NoSuchAlgorithmException{
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        return byteArray2Hex(md.digest(convertme));
    }

    private static String byteArray2Hex(final byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }

    protected String writeObject(Path fileName, byte [] typeFrom) throws IOException{

        byte [] fileContent = Files.readAllBytes(fileName);
        int fileSize = fileContent.length;
        byte [] len = intToByteArray(fileSize);
        int objLen = typeFrom.length + 1 + len.length + 1 + fileSize;
        byte [] obj = new byte[objLen];

        System.arraycopy(typeFrom, 0, obj, 0, typeFrom.length);
        obj[typeFrom.length] = (byte)' ';
        System.arraycopy(len, 0, obj, typeFrom.length+1, len.length);
        obj[typeFrom.length + 1 + len.length] = (byte)0;
        System.arraycopy(fileContent, 0, obj, typeFrom.length+2+ len.length, fileSize);

        String sha;
        try {
            sha = SHAsum(obj);
        }
        catch (NoSuchAlgorithmException e){return "";}
        file = Path.of(".git", "objects",
                sha.substring(0,2),sha.substring(2));
        final Path tmp = file.getParent();
        if (tmp != null) // null will be returned if the path has no parent
            Files.createDirectories(tmp);
        Files.createFile(file);

        try(OutputStream stream = Files.newOutputStream(file)){
            OutputStream deflater = new DeflaterOutputStream(stream);
            deflater.write(obj);
            deflater.flush();
        }
        catch (IOException e){
            Files.delete(file);
            throw e;
        }
        return sha;

    }
    public abstract void readObject();
    public abstract String writeObject(String filename);


}
