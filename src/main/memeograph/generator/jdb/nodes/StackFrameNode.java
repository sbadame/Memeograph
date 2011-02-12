package memeograph.generator.jdb.nodes;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import java.io.File;
import java.io.FileNotFoundException;
import memeograph.Config;
import memeograph.util.LineFinder;

public class StackFrameNode extends GraphNodeType {

  public final int count;
  public final String name;
  public final String uid;

  public StackFrameNode(StackFrame frame, int d) throws IncompatibleThreadStateException {
    ThreadReference thread = frame.thread();
    count = thread.frameCount() - d - 1;
    String tmp;
    try {
      String sourcename = frame.location().sourceName();
      LineFinder linefinder = Config.getConfig().getTargetProgram().getLineFinder(sourcename);
      tmp = linefinder.getLine(frame.location().lineNumber()).trim();
    } catch (AbsentInformationException ex) {
      tmp = thread.name();
    } catch (FileNotFoundException fnfe){
      tmp = thread.name();
    }
    name = tmp;
    uid = "Frame[" + count + " in Thread " + frame.thread().name() + "]";
  }

  public String getUniqueID() {
    return uid;
  }

  @Override
  public String toString(){
    return name;
  }

}
