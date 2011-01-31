package memeograph.generator.jdb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Is use this to re-route the SuT's System.out and System.err to our own
 * so that we can see any output that is generating.
 * Also fun-fact about JVMs: If you don't read their STDOUT they can get clogged
 * and stop running, therefore this is actually a necessity not just a novelty.
 */
public class ProcessDirector{
  private InputStream inputStream;
  private InputStream errorStream;
  private OutputStream outputStream;
  private Process process;

  private boolean kill = false;

  public ProcessDirector(Process p) {
    inputStream = p.getInputStream();
    errorStream = p.getErrorStream();
    outputStream = p.getOutputStream();
    this.process = p;
  }

  public void start(){
    final Thread t1 = new Thread(){
      @Override
      public void run(){
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        try {
          String s;
          while ((s = reader.readLine())!= null){System.out.println(s);}
        } catch (IOException ex) {
          ex.printStackTrace();
          if (!kill) {
            Logger.getLogger(ProcessDirector.class.getName()).log(Level.SEVERE,
                                                                  null, ex);
          }
        }
      }
    };

    final Thread t2 = new Thread(){
      @Override
      public void run(){
        BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream));
        try {
          String s;
          while ((s = reader.readLine())!=null){System.err.println(s);}
        } catch (IOException ex) {
          if (!kill) {
            ex.printStackTrace();
            Logger.getLogger(ProcessDirector.class.getName()).log(Level.SEVERE,
                                                                null, ex);
          }
        }
      }
    };

    final Thread t3 = new Thread(){
      @Override
      public void run(){
        while(true){
          try {
            outputStream.write(System.in.read());
          } catch (IOException ex) {
            if (!kill) {
              Logger.getLogger(ProcessDirector.class.getName()).log(Level.SEVERE, null, ex);
            }
          }
        }
      }
    };

    new Thread(){
      @Override
      public void run(){
        boolean exception = false;
        try {
          while(exception == false) process.waitFor();
        } catch (InterruptedException ex) {
          exception = true;
          Logger.getLogger(ProcessDirector.class.getName()).log(Level.SEVERE, null, ex);
        }
        kill = true;
        t1.interrupt();
        t2.interrupt();
        t3.interrupt();
      }
    }.start();

    t1.start();
    t2.start();
    t3.start();
  }
}
