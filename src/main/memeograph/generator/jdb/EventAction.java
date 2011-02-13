package memeograph.generator.jdb;

import com.sun.jdi.event.Event;
import memeograph.graph.Graph;

public interface EventAction {
    public Graph doAction(Event e);
}
