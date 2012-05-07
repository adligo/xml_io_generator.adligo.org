package org.adligo.xml_io.generator.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipUtils {
	private int BUFFER = 2048; 
	private boolean verbose = true;
	
 	public File unzip(File inFile, File outFolder) {
 		String newFileName = "";
        try
        {
             BufferedOutputStream out = null;
             ZipInputStream  in = new ZipInputStream(
                                           new BufferedInputStream(
                                                new FileInputStream(inFile)));
             ZipEntry entry;
             while((entry = in.getNextEntry()) != null)
             {
            	 if (entry.isDirectory()) {
            		 File newFolder = new File(outFolder.getAbsolutePath() + File.separator + 
            		 	entry.getName());
            		 if (verbose) {
            			 System.out.println("Unzip making directory " + newFolder.getAbsolutePath());
            		 }
            		 newFolder.mkdir();
            	 } 
             }
             in.close();
             
             in = new ZipInputStream(
                     new BufferedInputStream(
                          new FileInputStream(inFile)));
             while((entry = in.getNextEntry()) != null)
             {
            	 if (!entry.isDirectory()) {
	                  //System.out.println("Extracting: " + entry);
	                  int count;
	                  byte data[] = new byte[BUFFER];
	                  
	                  String origName = entry.getName();
	                  char [] chars = origName.toCharArray();
	                  StringBuilder sb = new StringBuilder();
	                  for (int i = 0; i < chars.length; i++) {
	                	  char zipSep = '/';
	                	  char c = chars[i];
						  if (c == zipSep) {
							  sb.append(File.separator);
						  } else {
							  sb.append(c);
						  }
	                  }
	                  newFileName = outFolder.getAbsolutePath() + File.separator + sb.toString();
	                  if (verbose) {
	            			 System.out.println("creating file " + newFileName);
	            		 }
	                  File newFile = new File(newFileName);
	                  File parentFile = newFile.getParentFile();
	                  if (!parentFile.exists()) {
	                	  recurseParentTree(parentFile);
	                  }
	                  newFile.createNewFile();
	                  FileOutputStream fos = new FileOutputStream(newFile);
	                  // write the files to the disk
	                  out = new BufferedOutputStream(fos,BUFFER);
	                 
	                  while ((count = in.read(data,0,BUFFER)) != -1)
	                  {
	                       out.write(data,0,count);
	                  }
	                  out.close();
	                  fos.close();
            	 }
             }
             in.close();
             return outFolder;
        }
        catch(Exception e)
        {
        	System.err.println("error with inFile '"+ inFile + "' outFolder '" + outFolder + "' " +
        			" newFileName is '" +  newFileName + "'");
             e.printStackTrace();
             return inFile;
        }
   }
 	
 	private void recurseParentTree(File parentFile) {
 		if (verbose) {
			 System.out.println("Unzip recurseParentTree making directory " + parentFile.getName());
		 }
 		File grandParent = parentFile.getParentFile();
 		if (!grandParent.exists()) {
 			recurseParentTree(grandParent);
 		}
 		parentFile.mkdir();
 	}


public synchronized boolean isVerbose() {
	return verbose;
}

public synchronized void setVerbose(boolean verbose) {
	this.verbose = verbose;
}


public synchronized int getBUFFER() {
	return BUFFER;
}


public synchronized void setBUFFER(int bUFFER) {
	BUFFER = bUFFER;
} 
}
