package com.codecrafter.git;

import com.codecrafter.git.Objects.BlobObject;
import com.codecrafter.git.Objects.CommitObject;
import com.codecrafter.git.Objects.GitObjects;
import com.codecrafter.git.Objects.TreeObject;
import com.codecrafter.git.clone.CloneCommand;
import com.codecrafter.git.clone.PktFile;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Main {

  static private void git_init(String repo){

    final File root = new File(repo, ".git");
    new File(root, "objects").mkdirs();
    new File(root, "refs").mkdirs();
    final File head = new File(root, "HEAD");

    try {
      head.createNewFile();
      Files.write(head.toPath(), "ref: refs/heads/master\n".getBytes());
      System.out.println("Initialized git directory");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }

  static private void git_read_object(String[] args){
    if(args.length<3)
      return;
    GitObjects object;
    try {
        if(args[1].equals("-p")){
            object= new BlobObject(".", args[2]);
            object.readObject();
        }
    }
    catch (FileNotFoundException f){
        System.out.println(String.format("Object Not Found:- %s", args[2]));
    }

  }

  static private void git_write_object(String[] args){
    if(args.length<3)
      return;
    GitObjects object;
    try {
        if(args[1].equals("-w")){
            object= new BlobObject(".","" );
            System.out.print(
                    GitObjects.byteArray2Hex(object.writeObject(args[2]))
            );
        }
    }
    catch (FileNotFoundException f){
        System.out.println(String.format("Object Not Found:- %s", args[2]));
    }

  }

  static private void git_read_tree(String [] args){
      if(args.length<3)
          return;
      GitObjects object;
      try {
          if(args[1].equals("--name-only")){
              object= new TreeObject(".", args[2]);
              object.readObject();
          }
      }
      catch (FileNotFoundException f){
          System.out.println(String.format("Object Not Found:- %s", args[2]));
      }
  }

  static private void git_write_tree(){
      GitObjects object;
      try {
          object= new TreeObject(".", "");
          System.out.print(GitObjects.byteArray2Hex(object.writeObject(".")));
      }
      catch (Exception f){
          System.out.println(String.format("%s", f.getMessage()));
      }
  }

  static private void git_commit(String[] args){
      if(args.length<6)
          return;

      CommitObject object;
      try {
          object= new CommitObject(".", "");
          object.setCommitData(args[1], args[3], args[5]);
          System.out.print(GitObjects.byteArray2Hex(object.writeObject(".")));
      }
      catch (Exception f){
          System.out.println(String.format("%s", f.getMessage()));
      }
  }

private static void git_clone(String[] args) {
    CloneCommand.git_clone(args);
}

private static InputStream sendWantRequest(String repo_url, byte[] write_buffer) throws IOException {
    URL url = new URL(repo_url + "/git-upload-pack");
    HttpURLConnection postHttpURLConnection = (HttpURLConnection) url.openConnection();
    postHttpURLConnection.setDoOutput(true);
    postHttpURLConnection.setRequestMethod("POST");
    postHttpURLConnection.setRequestProperty("Content-Type", "application/x-git-upload-pack-request");
    postHttpURLConnection.setRequestProperty("Git-Protocol", "version=2");
    try (DataOutputStream outputStream = new DataOutputStream(postHttpURLConnection.getOutputStream())) {
        outputStream.write(write_buffer);
    }

    return new BufferedInputStream(postHttpURLConnection.getInputStream());
}

  public static void main(String[] args){
    // You can use print statements as follows for debugging, they'll be visible when running tests.
//    System.out.println("Logs from your program will appear here!");

     //Uncomment this block to pass the first stage
     if(args.length<1){
       System.out.println("Expected command:- init, cate-file");
       return;
     }
     final String command = args[0];

     switch (command) {
       case "init" -> git_init(".");
       case "cat-file" -> git_read_object(args);
       case "hash-object" -> git_write_object(args);
       case "ls-tree" -> git_read_tree(args);
       case "write-tree" -> git_write_tree();
       case "commit-tree" -> git_commit(args);
       case "clone" -> {git_init(args[2]);git_clone(args);}
       default -> System.out.println("Unknown command: " + command);
     }
  }

}
