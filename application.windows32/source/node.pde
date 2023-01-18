class node{
    PVector pos;                        //In units
    PVector vel = new PVector(0,0);     //
    PVector acc = new PVector(0,0);     //

    int lifeTime = 0;   //How many ticks it has existed for

    node(PVector initialPos){
        pos = initialPos;
    }

    void display(PVector col){
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

    void updateParams(int tickRate){
        /*
        Updates the position and velocity of the node from the 1st order ODE system
        */
        lifeTime++;
        updateVel(tickRate);
        updatePos();
    }
    void updateVel(int tickRate){
        PVector f_dot = cManager.odeInput(pos);
        vel.x = f_dot.x / (float)tickRate;      //## MAKE SURE IS CORRECT, NOT A DIFFERENT RELATIONSHIP E.G  1/n^2  OR  1/n^0.5
        vel.y = f_dot.y / (float)tickRate;      //##
    }
    void updatePos(){
        pos.x += vel.x;
        pos.y += vel.y;
    }
}
