package memeograph.ui;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

/**
 * A 2-D Grid of "rails" for our nodes to slide along.
 * @author peter
 */
public class Grid implements Iterable<Vector<Node>> {
    final HashMap<Integer,HashMap<Integer, Vector<Node>>> grid =
            new HashMap<Integer,HashMap<Integer, Vector<Node>>>();

    /** Adds a node to a spike. */
    public void add(int y, int z, Node n) {
        if (!grid.containsKey(z))
            grid.put(z, new HashMap<Integer, Vector<Node>>());
        HashMap<Integer, Vector<Node>> row = grid.get(z);
        if (!row.containsKey(y))
            row.put(y, new Vector<Node>());
        Vector<Node> rail = row.get(y);
        rail.add(n);
    }

    /** Iterate over all of the "rails" stored in the grid. */
    public Iterator<Vector<Node>> iterator() {
        return new Iterator<Vector<Node>>() {
            Iterator<HashMap<Integer,Vector<Node>>> bigiter = grid.values().iterator();
            Iterator<Vector<Node>> littleiter = null;

            public boolean hasNext() {
                return bigiter.hasNext() || (littleiter != null && littleiter.hasNext());
            }

            public Vector<Node> next() {
                if (littleiter == null || !littleiter.hasNext())
                    littleiter = bigiter.next().values().iterator();
                return littleiter.next();
            }

            public void remove() {
                throw new UnsupportedOperationException("Can't remove from a grid using the Iterator.");
            }
        };
    }
}
