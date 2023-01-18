manager cManager;

void setup(){
    fullScreen();
    cManager = new manager();
    
    background(30,30,30);

    //cManager.spawnNodes(cManager.nodeNumber, height/3.0);    //Initial nodes
}
void draw(){
    cManager.calcSystem();
    cManager.displaySystem();
}

void keyPressed(){
    if(key == '1'){     //Spd -
        cManager.multi -= 0.01;
    }
    if(key == '2'){     //Spd +
        cManager.multi += 0.01;
    }
    if(key == '3'){     //Bifur -
        cManager.bifur -= 0.5;
    }
    if(key == '4'){     //BiFur +
        cManager.bifur += 0.5;
    }
}
