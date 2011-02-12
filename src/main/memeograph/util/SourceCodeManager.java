package memeograph.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import memeograph.Config;

public class SourceCodeManager {

  private final File[] sourcepaths;
  private final HashMap<String, File> sourcecache = new HashMap<String, File>();
  private final HashSet<String> notFound = new HashSet<String>();
  private final HashMap<String, LineFinder> linecache = new HashMap<String, LineFinder>();


  public SourceCodeManager(Config c){
      String[] paths = c.getStringArrayProperty(Config.SOURCE_PATHS);
      sourcepaths = new File[paths.length];
      for(int i = 0; i < paths.length; i++)
          sourcepaths[i] = new File(paths[i]);
  }

  public File[] getSourcePaths(){
      return sourcepaths;
  }

  public File getSourceFile(String filename) throws FileNotFoundException{
      if (notFound.contains(filename)) {
          throw new FileNotFoundException();
      }

      if (sourcecache.containsKey(filename)) {
        return sourcecache.get(filename);
      }

      for (File sourcepath : sourcepaths) {
          File possiblePath = new File(sourcepath, filename);
          if (possiblePath.exists() && possiblePath.canRead()) {
              sourcecache.put(filename, possiblePath);
              return possiblePath;
          }
      }

      //Couldn't find it... Oh well.
      notFound.add(filename);
      throw new FileNotFoundException();
  }

  public File getSourceFileFromClass(String classname) throws FileNotFoundException{
      if (classname.equals("boolean")  || classname.equals("byte")
          || classname.equals("char")  || classname.equals("double")
          || classname.equals("float") || classname.equals("int")
          || classname.equals("long")  || classname.equals("short") ) {
          return new File("");
      }

      String filename = classname.replace('.', File.separatorChar)+".java";
      return getSourceFile(filename);
  }

  public LineFinder getLineFinder(String sourcefilename) throws FileNotFoundException{
      if (linecache.containsKey(sourcefilename)) {
          return linecache.get(sourcefilename);
      }

      File sourceFile = getSourceFile(sourcefilename);
      try {
          LineFinder lineFinder = new LineFinder(sourceFile);
          linecache.put(sourcefilename, lineFinder);
          return lineFinder;
      } catch (FileNotFoundException ex) {
          ex.printStackTrace();
      }

      //This should NEVER happen
      return null;
  }

}