package memeograph.ui.animation;

import memeograph.ui.Node;

public class FadeIn implements Animation{

    Node n;
    public FadeIn(Node n){
        this.n = n;
    }

    public void tick(float percentdone) {
        n.opacity = percentdone;
    }

}
