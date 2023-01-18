class manager{
    ArrayList<node> nodeSet = new ArrayList<node>();

    int tickRate  = 4000;  //How many calculates are performed in one frame (will NOT affect speed, only precision)
    int tickCount = 0;  //Counts # of ticks that have occurred so far in the simulation

    PVector origin = new PVector(width/2.0, height/2.0);

    float pixelUnitFactor = 100.0;   //How many pixels = 1 unit
    int nodeNumber = 500;
    int maxLife    = 10000*60*tickRate; //LifeSpan in ticks

    //--FOR ODE --
    float multi = 0.01; //Just affects the magnitude of the f_dot, so it looks slower or faster on screen
    float bifur = 2.0;  //Variable if you want to do bifurcation stuff

    manager(){
        //pass
    }

    PVector odeInput(PVector f){
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
    PVector pixelToUnitVector(PVector pixelCoord){
        /*
        1. Shifts pixel coord to be relative to origin specified, NOT of window
        2. Converts this value into units
        */
        PVector pxlRelCoord  = new PVector(pixelCoord.x -origin.x, pixelCoord.y -origin.y);
        PVector unitRelCoord = new PVector(pxlRelCoord.x /pixelUnitFactor, pxlRelCoord.y /pixelUnitFactor);
        return unitRelCoord;
    }
    PVector unitToPixelVector(PVector unitCoord){
        /*
        1. Converts unit coord to pixels relative to origin
        2. Shift to be relative to window not origin
        */
        PVector pxlRelCoord = new PVector(unitCoord.x*pixelUnitFactor, unitCoord.y*pixelUnitFactor);
        PVector pxlCoord    = new PVector(pxlRelCoord.x +origin.x, pxlRelCoord.y +origin.y);
        return pxlCoord;
    }

    
    //Long computation stuff
    void populateNodeFile(int nFrames){
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
    void plotPositionSet(){
        /*
        Plots the nodes at the positions given in posSet (file)
        */
        //ArrayList<String> fullPositionSet = loadStrings("posSet");
        //... Break it up into bits here ...
    }


    void displaySystem(){
        /*
        Displays everything in the system
        e.g Nodes, Axis, etc
        */
        //displayBackground();
        displayAxis();

        displayNodes();

        displayOverlay();
    }
    void displayNodes(){
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
    void displayBackground(){
        background(0,0,0);
    }
    void displayAxis(){
        /*
        Displays an axis
        ### CAN MAKE DYNAMIC IN FUTURE -> SHRINK AND EXPAND TO FIT ALL NODES IN ###
        */
        float length       = 4.0*height/10.0;
        float intervalFreq = 0.1;
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
    void displayOverlay(){
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


    void calcSystem(){
        /*
        Calculates everything that must be change per tick
        */
        for(int i=0; i<tickRate; i++){  //Do n calculates per frame, ###with quantities 1/n* original value###
            if(tickCount % ceil((float)tickRate*0.1) == 0){    //Check this over x ticks, not every single tick
                calcNodeLifespan(60000);}
            calcNodeParams();
        }
        spawnNodesOverTime(60000, 3.0*height/10.0);
    }
    void calcNodeLifespan(int maxTime){
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
    void calcNodeParams(){
        for(int i=0; i<nodeSet.size(); i++){
            nodeSet.get(i).updateParams(tickRate);
        }
    }


    void spawnNodesOverTime(int sRate, float rad){
        /*
        Every sRate ticks, a new node is spawned randomly
        */
        for(int i=0; i<nodeNumber -nodeSet.size(); i++){
            spawnNodes(1, rad);
        }
    }
    void spawnNodes(int sCount, float rad){
        /*
        Instantly spawns sCount nodes within an radius of origin (in pixels)
        */
        for(int i=0; i<sCount; i++){
            float dist  = random(-rad, rad);
            float theta = random(0.0, 2.0*PI);
            node newNode = new node( pixelToUnitVector(new PVector(origin.x +dist*cos(theta), origin.y +dist*sin(theta))) );
            newNode.lifeTime = i*floor(maxLife/sCount);
            nodeSet.add(newNode);
        }
    }
}
