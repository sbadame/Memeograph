package memeograph.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class LineFinder {

  private final File file;
  private final HashMap<Integer, String> cache = new HashMap<Integer, String>();

  public LineFinder(File file) throws FileNotFoundException{
    if (!file.exists()) {
      throw new FileNotFoundException();
    }
    this.file = file;
  }


  public File getFile() {
    return file;
  }

  public String getLine(int wantedLine) {
      if (cache.containsKey(wantedLine)) {
        return cache.get(wantedLine);
      }
      try {
          BufferedReader reader = new BufferedReader(new FileReader(file));
          int currentLine = 1;
          while(currentLine != wantedLine){
             reader.readLine();
             currentLine++;
          }

          String line = reader.readLine();
          cache.put(wantedLine, line);

          reader.close();
          return line;
      } catch (FileNotFoundException ex) {
        ex.printStackTrace();
      } catch (IOException ioe){
        ioe.printStackTrace();
      }
    return "";
  }

}
