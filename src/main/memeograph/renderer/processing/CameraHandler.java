package memeograph.renderer.processing;

import java.awt.event.MouseWheelEvent;
import processing.core.PApplet;
import static processing.core.PApplet.*;
import processing.core.PVector;



public class CameraHandler {
    private static final PVector camNorth = new PVector(0, 1, 0);
    private static final int MOVE_TICK = 50;

    private PApplet processing;
    private PVector pos;
    private PVector dir;

    public CameraHandler(PApplet papplet){
        processing = papplet;
    }

    public void setup(){
        pos = new PVector(processing.width/2.0f, processing.height/2.0f, (processing.height/2.0f) / tan(PI*60.0f / 360.0f));
        dir = new PVector(processing.width/2.0f, processing.height/2.0f, 0);
    }

    public void draw(){
        processing.camera(pos.x, pos.y, pos.z, dir.x, dir.y, dir.z, camNorth.x, camNorth.y, camNorth.z);
    }

    public void scrollUp(){

    }

    private float dtheta = .03f;
    public void mouseDragged() {
        float dy = processing.pmouseY - processing.mouseY;
        if (dy != 0) {
            float y = (pos.y-dir.y);
            float z = (pos.z-dir.z);
            float r = PApplet.sqrt(y*y + z*z);
            float theta = PApplet.atan2(y, z);

            float theta_new = theta + ((dy > 0) ? dtheta : (-1*dtheta));
            y = sin(theta_new) * r;
            z = cos(theta_new) * r;
            pos.y = dir.y + y;
            pos.z = dir.z + z;
        }

        float dx = processing.pmouseX - processing.mouseX;
        if (dx != 0) {
            float x = (pos.x-dir.x);
            float z = (pos.z-dir.z);
            float r = sqrt(x*x + z*z);
            float theta = atan2(z, x);

            float theta_new = theta + ((dx < 0) ? dtheta : (-1*dtheta));
            x = cos(theta_new) * r;
            z = sin(theta_new) * r;
            pos.x = dir.x + x;
            pos.z = dir.z + z;
        }
    }

     void keyPressed() {
        char k = (char)processing.key;
        switch(k){
            case 'w':
            case 'W': translateCameraY(-MOVE_TICK); break;
            case 's':
            case 'S': translateCameraY(MOVE_TICK); break;
            case 'a':
            case 'A': translateCameraX(-MOVE_TICK); break;
            case 'd':
            case 'D': translateCameraX(MOVE_TICK); break;
            default: break;
        }
    }


    private void translateCameraY(float amount){
        PVector camera = PVector.sub(dir,pos);
        PVector cross = camera.cross(camNorth);
        PVector up = cross.cross(camera);
        up.normalize();
        up.mult(amount);
        pos.add(up);
        dir.add(up);
    }

    private void translateCameraX(float amount){
        PVector camera = PVector.sub(dir,pos);
        PVector cross = camera.cross(camNorth);
        cross.normalize();
        cross.mult(amount);
        pos.add(cross);
        dir.add(cross);
    }

  void mouseWheelMoved(MouseWheelEvent e) {
      int notches = -1*e.getWheelRotation(); //notches goes negative if the
                                          //wheel is scrolled up.

      PVector camera = PVector.sub(dir,pos);
      camera.normalize();

      pos.add(PVector.mult(camera, (float)notches * 100f));
      dir.add(PVector.mult(camera, (float)notches * 100f));
  }


}