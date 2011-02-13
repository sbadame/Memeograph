package memeograph.generator.jdi;

import com.sun.jdi.event.Event;
import memeograph.graph.Graph;

public interface EventAction {
    public Graph doAction(Event e);
}
