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
    protected byte [] fileContent;
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
            fileContent = new byte[fileSize];
//            All the data read matches the size
            readLen = bufferedReader.read(fileContent, 0, fileSize);
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

    public static byte[] SHAsum(byte[] convertme) throws NoSuchAlgorithmException{
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        return md.digest(convertme);
    }

    public static String byteArray2Hex(final byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }

    protected byte[] writeObject(Path fileName, byte [] typeFrom) throws IOException{

        fileContent = Files.readAllBytes(fileName);

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        buffer.write(typeFrom);
        buffer.write(" ".getBytes());
        buffer.write(String.valueOf(fileContent.length).getBytes());
        buffer.write(0);
        buffer.write(fileContent);
        byte [] obj = buffer.toByteArray();
        byte[] sha;
        try {
            sha = SHAsum(obj);
        }
        catch (NoSuchAlgorithmException e){return new byte[0];}
        String strSha = byteArray2Hex(sha);
        file = Path.of(".git", "objects",
                strSha.substring(0,2),strSha.substring(2));
        final Path tmp = file.getParent();
        if (tmp != null) // null will be returned if the path has no parent
            Files.createDirectories(tmp);
        Files.createFile(file);
//        System.out.println(String.valueOf(obj));
        DeflaterOutputStream out = new DeflaterOutputStream(new FileOutputStream(file.toFile()));
        out.write(obj);
        out.close();
        return sha;

    }
    public abstract void readObject();
    public abstract byte[] writeObject(String filename);


}
