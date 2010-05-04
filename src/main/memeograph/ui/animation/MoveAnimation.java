package memeograph.ui.animation;

import memeograph.ui.Node;


public class MoveAnimation implements Animation{

    Node node;
    float startx, starty;
    float endx, endy;

    public MoveAnimation(Node node, float endx, float endy){
        this.node = node;
        this.startx = node.x;
        this.starty = node.y;
        this.endx = endx;
        this.endy = endy;
    }

    public void tick(float percentdone) {
        //Taken from:http://sol.gfxile.net/interpolation/index.html#s4
        float v = (percentdone * percentdone * (3 - 2 * percentdone));
        node.x = (startx * v) + (endx * (1-v));
        node.y = (starty * v) + (endx * (1-v));
    }

}
