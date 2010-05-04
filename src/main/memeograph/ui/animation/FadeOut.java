package memeograph.ui.animation;

import memeograph.ui.Node;

public class FadeOut implements Animation{

    Node n;
    public FadeOut(Node n){
        this.n = n;
    }

    public void tick(float percentdone) {
        n.opacity = (1 - percentdone);
    }

}
