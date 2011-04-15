/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package memeograph.renderer.processing;

/**
 *
 * @author mwaldron74
 */
public class Coordinate {
    public float x, y, z;
    public Coordinate(float x, float y, float z){
        this.x = x;
        this.y = y; 
        this.z = z;
    }
    public String toString(){
        return ("x:"+x+"   y:"+y+"   z:"+z);
    }
}
