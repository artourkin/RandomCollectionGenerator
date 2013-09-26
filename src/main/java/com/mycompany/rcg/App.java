package com.mycompany.rcg;

/**
 * Hello world!
 *
 */
public class App {

  public static void main(String[] args) {
    System.out.println("Hello World!");
    if (args.length != 1) {
      System.out.println("You have to specify a path to a folder with .tgz files");
      return;
    }
    RCG rcg=new RCG(args[0]);
    rcg.Run();
  }

  
}
