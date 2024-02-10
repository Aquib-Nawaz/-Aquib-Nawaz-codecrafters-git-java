package com.codecrafter.git;

import com.codecrafter.git.Objects.BlobObject;
import com.codecrafter.git.Objects.GitObjects;
import com.codecrafter.git.Objects.TreeObject;

import java.io.File;
import java.io.FileNotFoundException;
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

  static private void git_write(String[] args){
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
       case "hash-object" -> git_write(args);
       case "ls-tree" -> git_read_tree(args);
       case "write-tree" -> git_write_tree();
       default -> System.out.println("Unknown command: " + command);
     }
  }
}
