package com.codecrafter.git;

import com.codecrafter.git.Objects.BlobObject;
import com.codecrafter.git.Objects.GitObjects;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Main {

  static private void git_init(){

    final File root = new File(".git");
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

  static private void git_read(String[] args){
    if(args.length<3)
      return;
    GitObjects object;
    if(args[1].equals("-p")){
       object= new BlobObject(".", args[2]);
       object.readObject();
    }
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
       case "init" -> git_init();
       case "cat-file" -> git_read(args);
       default -> System.out.println("Unknown command: " + command);
     }
  }
}
