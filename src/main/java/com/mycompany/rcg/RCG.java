/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.rcg;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author artur
 */
public class RCG {

  private String args;

  public RCG(String args) {
    this.args = args;
  }

  public void Run() {
    Random r = new Random();
    File dir = new File(args);
    File outputDir = createDirectory(dir.getParent() + "/" + dir.getName() + "_output");
    ArrayList<File> ArchiveFiles = GetListOfFiles(dir, new ArchiveFileFilter());
    //List<File> ArchiveFiles = Arrays.asList(GetListOfFiles(dir, new ArchiveFileFilter()));
    int max = 1000000;
    int count = 0;

    while (count < max) {
      int index = r.nextInt(ArchiveFiles.size() - 1);
      int extractedfiles = processArchive(ArchiveFiles.get(index), outputDir, max - count, r);
      ArchiveFiles.remove(index);
      count += extractedfiles;
    }

  }

  public File createDirectory(String Directory) {
    File theDir = new File(Directory);
    // if the directory does not exist, create it
    if (!theDir.exists()) {
      try {
        FileUtils.deleteDirectory(theDir);
        FileUtils.forceMkdir(theDir);
      } catch (IOException ignored) {
      }
    }
    return theDir;
  }

  public void deleteDirectory(String Directory) {
    File theDir = new File(Directory);
    try {
      FileUtils.deleteDirectory(theDir);
    } catch (IOException ignored) {
    }
  }

  public void extract(File file, String Destination) {
    //String s;
    try {
      Process p = Runtime.getRuntime().exec("tar -xzf " + file.getPath() + " -C " + Destination);
      System.out.println("Extracting: " + file.getPath());
      p.waitFor();
      p.destroy();
    } catch (Exception ignored) {
    }
  }

  private int processArchive(File FileToExtract, File outputDir, int AmountToExtract, Random r) {
    int count = 0;

    String path = FileToExtract.getPath();
    File tmpDir = createDirectory(FileToExtract.getParent() + "/tmp");
    extract(FileToExtract, tmpDir.getPath());
    ArrayList<File> Files = GetListOfFiles(tmpDir, new XMLFileFilter());
    if (Files.size() == 0) {
      return 0;
    }

    File tmp = (Files.get(0).getParentFile());
    outputDir = createDirectory(outputDir.getPath() + "/" + tmp.getName());

    int rToExtract = 0;
    //DEFINE HOW MANY FILES ARE TO BE EXTRACTED FROM A ZIP
    if (AmountToExtract >= Files.size()) {
      while (rToExtract < Files.size() / 4) {
        rToExtract = r.nextInt(Files.size() - 1);
      }
    } else {
      rToExtract = AmountToExtract;
    }

    int i = 0;
    while (count < rToExtract && Files.size() > 0) {
      i = r.nextInt(Files.size() - 1);
      try {
        FileUtils.copyFileToDirectory(Files.get(i), outputDir);
      } catch (IOException ex) {
        Logger.getLogger(RCG.class.getName()).log(Level.SEVERE, null, ex);
      }
      Files.remove(i);
      count++;
    }

    deleteDirectory(tmpDir.getPath());

    return count;
  }

  public ArrayList<File> GetListOfFiles(File file, FileFilter filter) {
    if (file.exists() && file.isDirectory()) {
      ArrayList<File> files = new ArrayList<File>();
      files.addAll(Arrays.asList(file.listFiles(filter)));
      ArrayList<File> tmpfiles = new ArrayList<File>();
      if (files.size() > 0 && files.get(0).isDirectory()) {
        for (File f : files) {
          tmpfiles.addAll(GetListOfFiles(f, filter));
        }
      }
      if (tmpfiles.size() > 0) {
        files.clear();
        files.addAll(tmpfiles);
      }

      return files;
    }
    return new ArrayList<File>();

  }

  private class XMLFileFilter implements FileFilter {

    public XMLFileFilter() {
    }

    @Override
    public boolean accept(File pathname) {
      boolean accept = false;

      if (pathname.isDirectory() || pathname.getName().endsWith(".xml")) {
        accept = true;
      }

      return accept;
    }
  }

  public class ArchiveFileFilter implements FileFilter {

    public ArchiveFileFilter() {
    }

    boolean isArchive(File pathname) {
      return (pathname.getName().endsWith(".zip")
              || pathname.getName().endsWith(".bzip2")
              || pathname.getName().endsWith(".gzip")
              || pathname.getName().endsWith(".jar")
              || pathname.getName().endsWith(".tar")
              || pathname.getName().endsWith(".tar.gz")
              || pathname.getName().endsWith(".tgz"));
    }

    public String removeExtension(String in) {
      int p = in.lastIndexOf(".");
      if (p < 0) {
        return in;
      }

      int d = in.lastIndexOf(File.separator);

      if (d < 0 && p == 0) {
        return in;
      }

      if (d >= 0 && d > p) {
        return in;
      }

      return in.substring(0, p);
    }

    @Override
    public boolean accept(File pathname) {
      boolean accept = false;
      if (isArchive(pathname)) {
        String name = removeExtension(pathname.getPath());
        if (!(new File(name).isDirectory())) {
          accept = true;
        }
      }
      return accept;
    }
  }
}
