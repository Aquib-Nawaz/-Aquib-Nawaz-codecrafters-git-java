package com.codecrafter.git.Objects;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class CommitObject extends GitObjects{

    private String parent;
    private String message;
    private String authorName = "Aquib Nawaz";
    private final String authorMail = "random@gmail.com";;
    private String tree;

    public CommitObject(String repo, String hash) throws FileNotFoundException {
        super(repo, hash);
        type = "commit".getBytes();
    }

    public void setCommitData( String tree, String parentCommit, String message){
        parent = parentCommit;
        this.message = message;
        this.tree = tree;
    }
    @Override
    public void readObject() {
    }

    public String getTree() {
        super.readObjectFromHash();
        return new String(fileContent, 5, 40);
    }

    @Override
    public byte[] writeObject(String filename) {
        byte [] sha = new byte[0];
        try(ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {

            String line = String.format("tree %s\n", tree);
            buffer.write(line.getBytes());

            line = String.format("parent %s\n", parent);
            buffer.write(line.getBytes());

            long unixTime = System.currentTimeMillis() / 1000L;
            ZoneOffset o = OffsetDateTime.now().getOffset();
            String time = String.format("%d%s", unixTime, o.toString());

            String author = String.format("author %s <%s> %s\n", authorName, authorMail, time);
            String committer = String.format("committer %s <%s> %s\n", authorName, authorMail, time);

            buffer.write(author.getBytes());
            buffer.write(committer.getBytes());

            line = String.format("\n%s\n", message);
            buffer.write(line.getBytes());

            fileContent = buffer.toByteArray();
            sha = super.writeObject();
//            System.out.println(o.toString());

        }catch (IOException e){}
        return sha;
    }
}
