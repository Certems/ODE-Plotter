import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class OdePlotter extends PApplet {

manager cManager;

public void setup(){
    
    cManager = new manager();
    
    background(30,30,30);

    //cManager.spawnNodes(cManager.nodeNumber, height/3.0);    //Initial nodes
}
public void draw(){
    cManager.calcSystem();
    cManager.displaySystem();
}

public void keyPressed(){
    if(key == '1'){     //Spd -
        cManager.multi -= 0.01f;
    }
    if(key == '2'){     //Spd +
        cManager.multi += 0.01f;
    }
    if(key == '3'){     //Bifur -
        cManager.bifur -= 0.5f;
    }
    if(key == '4'){     //BiFur +
        cManager.bifur += 0.5f;
    }
}
class manager{
    ArrayList<node> nodeSet = new ArrayList<node>();

    int tickRate  = 4000;  //How many calculates are performed in one frame (will NOT affect speed, only precision)
    int tickCount = 0;  //Counts # of ticks that have occurred so far in the simulation

    PVector origin = new PVector(width/2.0f, height/2.0f);

    float pixelUnitFactor = 100.0f;   //How many pixels = 1 unit
    int nodeNumber = 500;
    int maxLife    = 10000*60*tickRate; //LifeSpan in ticks

    //--FOR ODE --
    float multi = 0.01f; //Just affects the magnitude of the f_dot, so it looks slower or faster on screen
    float bifur = 2.0f;  //Variable if you want to do bifurcation stuff

    manager(){
        //pass
    }

    public PVector odeInput(PVector f){
        /*
        Takes in a vector function f=(x,y), and returns a vector value f_dot = (x_dot, y_dot)
        x and y should be in units NOT pixels

        The 1st ODE should be input into the vector f_dot

        ## CAN EXAPND TO N DIM ODE ## --> ONLY 2 DIM FOR NOW

        EXAMPLES
        ---------
        PVector f_dot = new PVector(-f.y, f.x);                     --> CCW cirles
        PVector f_dot = new PVector(-f.x + f.y, f.x + f.y);         --> 2 linear subspaces in X shape
        PVector f_dot = new PVector(f.x -f.y, 2.0*f.x + f.y);       --> Unstable spiral
        PVector f_dot = new PVector(-f.x -f.y, 2.0*f.x -f.y);       --> Stable spiral
        */
        PVector f_dot = new PVector(bifur*f.x + f.y - f.x*( pow(f.x,2) + pow(f.y,2) ), -f.x + bifur*f.y - f.x*( pow(f.x,2) + pow(f.y,2) ));
        return new PVector(multi*f_dot.x, multi*f_dot.y);
    }
    public PVector pixelToUnitVector(PVector pixelCoord){
        /*
        1. Shifts pixel coord to be relative to origin specified, NOT of window
        2. Converts this value into units
        */
        PVector pxlRelCoord  = new PVector(pixelCoord.x -origin.x, pixelCoord.y -origin.y);
        PVector unitRelCoord = new PVector(pxlRelCoord.x /pixelUnitFactor, pxlRelCoord.y /pixelUnitFactor);
        return unitRelCoord;
    }
    public PVector unitToPixelVector(PVector unitCoord){
        /*
        1. Converts unit coord to pixels relative to origin
        2. Shift to be relative to window not origin
        */
        PVector pxlRelCoord = new PVector(unitCoord.x*pixelUnitFactor, unitCoord.y*pixelUnitFactor);
        PVector pxlCoord    = new PVector(pxlRelCoord.x +origin.x, pxlRelCoord.y +origin.y);
        return pxlCoord;
    }

    
    //Long computation stuff
    public void populateNodeFile(int nFrames){
        /*
        Calculates the positions of all nodes, does X computations in the given frame, then stores new positions in file
        Repeats this process for Y frames
        Only does this for the nodes present when the function is called + involves no replacing, just flows points through to the end

        This file can then be read off and plotted -> 1 line = 1 frame, all positions for particle

        1. For each frame
            2. Calc new position
            3. Add positions to string
            4. Save string
        */
        //1
        ArrayList<String> fullPositionSet = new ArrayList<String>();
        for(int i=0; i<nFrames; i++){
            //2
            calcNodeParams();
            //3
            String positionSet = "";
            for(int j=0; j<nodeSet.size(); j++){
                positionSet = positionSet +str(nodeSet.get(i).pos.x)+","+str(nodeSet.get(i).pos.y)+",";
            }
            fullPositionSet.add(positionSet);
        }
        //saveStrings("posSet", fullPositionSet);
    }
    public void plotPositionSet(){
        /*
        Plots the nodes at the positions given in posSet (file)
        */
        //ArrayList<String> fullPositionSet = loadStrings("posSet");
        //... Break it up into bits here ...
    }


    public void displaySystem(){
        /*
        Displays everything in the system
        e.g Nodes, Axis, etc
        */
        //displayBackground();
        displayAxis();

        displayNodes();

        displayOverlay();
    }
    public void displayNodes(){
        /*
        Displays all nodes
        */
        for(int i=0; i<nodeSet.size(); i++){
            if(i <= 10){
                nodeSet.get(i).display( new PVector(66, 135, 245) );}
            else{
                nodeSet.get(i).display( new PVector(255, 255, 255) );}
        }
    }
    public void displayBackground(){
        background(0,0,0);
    }
    public void displayAxis(){
        /*
        Displays an axis
        ### CAN MAKE DYNAMIC IN FUTURE -> SHRINK AND EXPAND TO FIT ALL NODES IN ###
        */
        float length       = 4.0f*height/10.0f;
        float intervalFreq = 0.1f;
        pushStyle();

        //Lines
        noFill();
        stroke(255,0,0, 100);
        strokeWeight(3);
        line(origin.x -length, origin.y        , origin.x +length, origin.y        );    //X axis
        line(origin.x        , origin.y -length, origin.x        , origin.y +length); //Y axis

        //Values
        rectMode(CENTER);
        fill(255,0,0,100);
        noStroke();
        for(float i=origin.x -length; i<=origin.x +length; i+=length*intervalFreq){
            rect(i, origin.y, 5,5);}
        for(float j=origin.y -length; j<=origin.y +length; j+=length*intervalFreq){
            rect(origin.x, j, 5,5);}

        popStyle();
    }
    public void displayOverlay(){
        /*
        Displays bug fixing tools
        */
        pushStyle();
        
        fill(255);
        textAlign(LEFT, CENTER);
        textSize(20);
        //text(frameRate, 10,30);
        
        popStyle();
    }


    public void calcSystem(){
        /*
        Calculates everything that must be change per tick
        */
        for(int i=0; i<tickRate; i++){  //Do n calculates per frame, ###with quantities 1/n* original value###
            if(tickCount % ceil((float)tickRate*0.1f) == 0){    //Check this over x ticks, not every single tick
                calcNodeLifespan(60000);}
            calcNodeParams();
        }
        spawnNodesOverTime(60000, 3.0f*height/10.0f);
    }
    public void calcNodeLifespan(int maxTime){
        /*
        Removes nodes from nodeSet after existing for a given number of ticks
        maxTime = lifespan time limit before nodes start being removed
        */
        for(int i=nodeSet.size()-1; i>=0; i--){
            if(nodeSet.get(i).lifeTime >= maxTime){
                nodeSet.remove(i);
            }
        }
    }
    public void calcNodeParams(){
        for(int i=0; i<nodeSet.size(); i++){
            nodeSet.get(i).updateParams(tickRate);
        }
    }


    public void spawnNodesOverTime(int sRate, float rad){
        /*
        Every sRate ticks, a new node is spawned randomly
        */
        for(int i=0; i<nodeNumber -nodeSet.size(); i++){
            spawnNodes(1, rad);
        }
    }
    public void spawnNodes(int sCount, float rad){
        /*
        Instantly spawns sCount nodes within an radius of origin (in pixels)
        */
        for(int i=0; i<sCount; i++){
            float dist  = random(-rad, rad);
            float theta = random(0.0f, 2.0f*PI);
            node newNode = new node( pixelToUnitVector(new PVector(origin.x +dist*cos(theta), origin.y +dist*sin(theta))) );
            newNode.lifeTime = i*floor(maxLife/sCount);
            nodeSet.add(newNode);
        }
    }
}
class node{
    PVector pos;                        //In units
    PVector vel = new PVector(0,0);     //
    PVector acc = new PVector(0,0);     //

    int lifeTime = 0;   //How many ticks it has existed for

    node(PVector initialPos){
        pos = initialPos;
    }

    public void display(PVector col){
        /*
        Draws the node
        */
        pushStyle();

        fill(col.x, col.y, col.z);
        noStroke();
        PVector pxlPos = cManager.unitToPixelVector(pos);
        ellipse(pxlPos.x, pxlPos.y, 2,2);

        popStyle();
    }

    public void updateParams(int tickRate){
        /*
        Updates the position and velocity of the node from the 1st order ODE system
        */
        lifeTime++;
        updateVel(tickRate);
        updatePos();
    }
    public void updateVel(int tickRate){
        PVector f_dot = cManager.odeInput(pos);
        vel.x = f_dot.x / (float)tickRate;      //## MAKE SURE IS CORRECT, NOT A DIFFERENT RELATIONSHIP E.G  1/n^2  OR  1/n^0.5
        vel.y = f_dot.y / (float)tickRate;      //##
    }
    public void updatePos(){
        pos.x += vel.x;
        pos.y += vel.y;
    }
}
  public void settings() {  fullScreen(); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "OdePlotter" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
