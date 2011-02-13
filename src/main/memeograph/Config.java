package memeograph;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import memeograph.util.SourceCodeManager;

/**
 * At its core just a bunch of names and values for properties
 * This is a singleton
 * Some handy things:
 *    - Loads a bunch of values from "default.properties"
 *
 *    - Grabs properties from the command line args and those overwrite anything
 *      already defined in "default.properties"
*
 *    - Comma separated values can be loaded as an array of Strings
 *
 *    - Has the ability to create instances of classes that are pointed to in a
 *    property.
 *        ex) if a property were defined as such: myclass = java.util.ArrayList
 *            then: List list = getClass("myclass", List)
 *            Would create a list using its default empty constructor.
 *        To get a class using getClass(String, Class<E>) it MUST have a public
 *        Constructor that either takes a Config (This object is passed in) or
 *        have an empty constructor.
 *
 * @author Sandro Badame <a href="mailto:sandro@sbcoded.com">sandro&amp;sbcoded.com</a>
 */
public class Config extends Properties{
  public static final String FILTERS = "filters";
  public static final String GENERATOR = "generator";
  public static final String GROUP_PRIMATIVES =  "groupprimitives";
  public static final String PORT = "port";
  public static final String RENDERER = "renderer";
  public static final String TRIGGER = "trigger";
  public static final String USE_OPENGL = "useopengl";
  public static final String VERBOSE = "verbose";
  public static final String SOURCE_PATHS = "sourcepath";

  public static final String TARGET_OPTIONS = "vm_options";
  public static final String TARGET_MAIN = "targetmain";
  public static final String TARGET_ARGS = "targetargs";

  //Some of our own properties...
  public static final String PROPERTY_DIVIDER = ",";

  private static Config config;

  /*
   * Create a properties using the String[] as arguments
   */
  public static Config createConfig(String[] args){
    config = new Config(args);
    return config;
  }

  /**
   * @return get the only properties instance in existence
   */
  public static Config getConfig(){
    return config;
  }

  private Generator generator = null;
  private Renderer renderer = null;
  private SourceCodeManager target = null;

  private Config(String[] args){
    try {
      load(getClass().getResourceAsStream("default.properties"));
    } catch (IOException ex) {
      Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
    }

    StringBuilder fullargs = new StringBuilder();
    for (String arg : args) { fullargs = fullargs.append(arg).append(" ");}
    putAll(parseArgs(fullargs.toString()));

    String vm_args = getProperty(TARGET_OPTIONS);
    if (vm_args == null || vm_args.isEmpty()) {
        System.err.println("Please pass in a program to run!");
        System.exit(1);
    }

    if (isSwitchSet(VERBOSE, false)) {
        System.out.println("Passed in Arguments: " + fullargs);
        LinkedList<String> keyvaluepairs = new LinkedList<String>();
        for (Object object : keySet()) {
            keyvaluepairs.add(object + "=" + getProperty((String) object));
        }
        Collections.sort(keyvaluepairs);
        System.out.println("Printing Config:");
        for (String string : keyvaluepairs) { System.out.println("\t"+string); }
    }
  }

  public boolean isSwitchSet(String key, boolean def){
     if (getProperty(key) != null && getProperty(key).isEmpty() == false) {
      return Boolean.valueOf(getProperty(key)).equals(Boolean.TRUE);
     }else{
       return def;
     }
  }

  public boolean isPropertySet(String property){
    return getProperty(property) != null && !getProperty(property).isEmpty();
  }

  public Generator getGenerator() {
      if (generator == null) {
        generator = getClass(GENERATOR, Generator.class);
      }
      return generator;
  }

  public Renderer getRenderer() {
      if (renderer == null) {
        renderer = getClass(RENDERER, Renderer.class);
      }
      return renderer;
  }

  public String[] getStringArrayProperty(String propertyname) {
    String value = getProperty(propertyname);
    if (value != null) {
       return value.split(PROPERTY_DIVIDER);
    }
    return new String[0];
  }

  public SourceCodeManager getTargetProgram(){
      if (target == null) {
          target = new SourceCodeManager(this);
      }
      return target;
  }

  /**
   * Returns an instance of the class pointed to by a property.
   * You must either have a public constructor that takes a Config instance
   * as it's only argument or a default constructor.
   *
   * Note: Writing this class makes me hate java exception handling with a passion.
   *       It also convinced me that maybe sometimes reflection can be the
   *       right answer...
   * @param <E>  The class type that you want loaded
   * @param property Name of the property that you want an instance of
   * @param cls The class type that you want the instance returned as
   * @return An instance of the class pointed to by property using either
   *          the public constructor that takes a Config as it's only argument
   *          or the default constructor.
   */
  @SuppressWarnings("unchecked")
  public <E> E getClass(String property, Class<E> cls){
     //I hate java exception handling sometimes...
    try {
      ClassLoader classLoader = getClass().getClassLoader();
      Class<?> loadClass = classLoader.loadClass(getProperty(property));
      E object = null;
      Constructor<?> constructor = null;
      try {
        constructor = loadClass.getConstructor(Config.class);
        try {
          return (E)constructor.newInstance(Config.this);
        } catch (InstantiationException ex) {
          Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
          Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
          Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
          Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
        }
      } catch (NoSuchMethodException ex) {
        //Ok maybe, it has a empty constructor... lets try again...
        try {
          constructor = loadClass.getConstructor();
          try {
            return (E) constructor.newInstance();
          } catch (InstantiationException ex1) {
            Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex1);
          } catch (IllegalAccessException ex1) {
            Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex1);
          } catch (IllegalArgumentException ex1) {
            Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex1);
          } catch (InvocationTargetException ex1) {
            Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex1);
          }
        } catch (NoSuchMethodException ex1) {
          System.err.println("There is an error trying to set the property \"" + property + "\" to \"" + cls.getCanonicalName() +
          "\".\nThe problem is that I'm trying to instantiate it, but can't find a valid constructor.\n" +
          "Could you please create one that is public and either takes no arguements, or takes a Config object?");
        } catch (SecurityException ex1) {
          Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex1);
        }
      } catch (SecurityException ex) {
        Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
      }
    } catch (ClassNotFoundException ex) {
      System.err.println("I couldn't find the class: \'" + getProperty(property) + "\' in your classpath. It's pointed to by the property: \'" + property + "\'");
    }
    return null;
  }

  public static Map<String, String> parseArgs(String commandLine){
      HashMap<String, String> properties = new HashMap<String, String>();

      Pattern[] patterns = new Pattern[]{
        //Switches like +show
        Pattern.compile("\\+([\\w\\.]+)"),
        //Regular properties like +key=val
        Pattern.compile("\\+([\\w\\.]+)\\s*=\\s*(\\S+)"),
        //Quoted properties +f="f b"
        Pattern.compile("\\+([\\w\\.]+)\\s*=\\s*\"([^\"])*\"") 
      };

      commandLine = commandLine.trim();
      while(!commandLine.isEmpty()){
          Matcher[] results = new Matcher[patterns.length];
          for(int i = 0; i < results.length; i++){
             results[i] = patterns[i].matcher(commandLine);
          }

          Matcher best = null;
          for (Matcher matcher : results) {
              if (!matcher.lookingAt()) { continue; }
              if (best == null) { best = matcher; continue; }

              if (best.end() < matcher.end()) {
                  best = matcher;
              } 
          }

          //Yay! We have a match!!
          if (best != null) {
             if (best.groupCount() == 1) {
                properties.put(best.group(1), String.valueOf(true));
             }else if (best.groupCount() == 2){
                properties.put(best.group(1), best.group(2));
             }
             commandLine = commandLine.substring(best.end());
          }else{
             properties.put(TARGET_OPTIONS, commandLine);
             return properties;
          }
          commandLine = commandLine.trim();
      }

      return properties;
  }

}