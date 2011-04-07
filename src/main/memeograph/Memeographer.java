package memeograph;

import memeograph.generator.jdi.nodes.ObjectNode;
import memeograph.graph.Graph;
import memeograph.graph.Node;
import memeograph.util.ACyclicIterator;

/**
 * Seriously, this is the whole program in a nutshell.
 * Config takes the args from the commandline and from default.properties
 *  - Look into Config to see how the Renderer and Generator are chosen
 *
 * The Renderer actually gets created first. Since the renderer is mostly
 * a GUI (The Processing Applet) it can be useful to have it pop up first
 * and let the user know what the graph generator is about to get it to work.
 * You can also get clever and use the renderer to maybe set some options for
 * the generator by blocking in init() until you're ready.
 *
 * The Generator gets going and starts to generate a graph that
 * gets passed to the Renderer.
 *
 * The program then exits.
 *
 */
public class Memeographer {

    public static void main(String[] args) {
        Config config = Config.createConfig(args);

        Renderer renderer = config.getRenderer();
        renderer.init();

        Generator generator = config.getGenerator();
        generator.start();
        Graph g = null;
        int k = 0;
        while( (g = generator.getNextGraph()) != null ){
            renderer.addGraph(g);
            /*ACyclicIterator<Node> i = new ACyclicIterator<Node>(g.preorderTraversal());
            int j = 0;
            while(i.hasNext()){
              Node n = i.next();
              if (n.gnt instanceof ObjectNode)
                System.out.println(++j+":"+((ObjectNode)n.gnt).getUniqueID());
            }
            System.out.println(++k+ "-----------------");//*/
        }
        renderer.finish();
    }

}
