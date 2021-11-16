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

public class chaotic_behaviour_simulation_100721 extends PApplet {

ArrayList<Particle> particles;
ArrayList<Planet> planets;
ArrayList<Cell> cells;

statusHandler statusHandler;

planetPlaceholder[] planetPlaceholders = new planetPlaceholder[5];

PFont mono;
PFont monoBold;

float cellWidth;
float planetSize;
int scale;
int demoScale;

boolean firstBackground;

boolean exported;

int togglerStatus = 0;
int settingsToggler = 0;
boolean arraysLoaded = false;

//Used to draw planets at a certain distance from border
float marginX;

//Variables used by "showProgress()"
float completion;
float previousCompleted;
float completed;
float total;
float previousTime;
float time;
String progress;


float friction; 
int planetNum;





public void setup() {
  statusHandler = new statusHandler();
    
  
  background(0);
  
  demoScale=10;
  friction = 0.003f;
    
  for (int i = 0; i < 5; i ++) {
    int colour = color(0);
    switch (i) {
      case 0: colour = color(0xffff0000); 
        break;
      case 1: colour = color(0xffffff00);
        break;
      case 2: colour = color(0xff00ff00);
        break;
      case 3: colour = color(0xff00ffff);
        break;
      case 4: colour = color(0xff0000ff);
        break;
    };

    if ( i == 0 || i == 2 || i == 4 ) {
      planetPlaceholders[i] = new planetPlaceholder(colour, true);
    } else {
      planetPlaceholders[i] = new planetPlaceholder(colour, false);
    }
    
  }
  
};

public void draw() {
  if (statusHandler.menu == true) {
    menu();
  };
  if (statusHandler.render == true) {
    render();
  };
  if (statusHandler.demo == true) {
    demo();
  };
   if (statusHandler.trajectory == true) {
    trajectory();
  };
  if (statusHandler.settingsMenu == true) {
    settingsMenu();
  };
};





///////////////////////////////////////
///ARRAYS INITIATION AND DESTRUCTION///
///////////////////////////////////////


///LOAD ARRAYS///
public void loadArrays() {
  
  if (statusHandler.render == true) {
    scale = width;
  }
  
  if (statusHandler.demo == true) {
    scale = demoScale;
  }

  if (statusHandler.render == true || statusHandler.demo == true) {
  
    //CELLS
    cells = new ArrayList<Cell> ();
    cellWidth = width/scale;
      
    for (int y = 0; y < scale; y++) {
      if (y*cellWidth < height){
        for (int x = 0; x < scale; x++){
          cells.add(new Cell(new PVector(x*cellWidth, y*cellWidth), cellWidth));
        }
      };
    };
  }
  
  //PLANETS
  planets = new ArrayList<Planet> ();
  marginX = width/5;
  planetSize = height/32;
  
  for (int i = 0; i < 5; i ++) {
    if (planetPlaceholders[i].selected == true ) {;
      planets.add(new Planet(new PVector (random(marginX, width-marginX), random (marginX, height-marginX)), planetPlaceholders[i].mass, planetPlaceholders[i].colour));
    };
  };
 
  //PARTICLES
  particles = new ArrayList<Particle> ();
  
  if (statusHandler.trajectory == true) {
    for (int i = 0; i < 10; i++) {      
      particles.add(new Particle(new PVector (Math.round(random(0,width)), Math.round(random(0,height)))));
    }
  }
  
  if (statusHandler.render == true || statusHandler.demo == true) {
    for (int i = 0; i < cells.size(); i++) {
      float Xpos = cells.get(i).pos.x + cellWidth/2;
      float Ypos = cells.get(i).pos.y + cellWidth/2;
      
      particles.add(new Particle(new PVector (Xpos, Ypos)));
    }
  }
  
  total = particles.size();
  
  arraysLoaded = true;
}

///DESTROY ARRAYS///
public void destroyArrays() {
  if (statusHandler.render == true || statusHandler.demo == true) {
  cells.clear();}
  particles.clear();
  planets.clear();
  
  arraysLoaded = false;
  println("arraysDestroyed");
}





///////////////////////
//APPLICATIONS STATES//
///////////////////////

///MENU///

public void menu() {
  if (arraysLoaded == true) {
    destroyArrays();
  };
  
  background(0);
  
  mono     = createFont("SpaceMono-Regular.ttf", height/32);
  monoBold = createFont("SpaceMono-Bold.ttf", height/32);
  
  textFont(monoBold);
  fill(255);
  textAlign(CENTER);
  
  textSize(height/12);
  text("ChaosTheory", width/2, 2*height/12);
  textFont(mono);
  
  textSize(height/32);
  
  if (togglerStatus == 0) {
    textFont(monoBold);
    text(">Settings<", width/2, 4*height/12);
  } else {  
    textFont(mono);
    text("Settings", width/2, 4*height/12);
  };
  
  if (togglerStatus == 1) { 
    textFont(monoBold);
    text(">Trajectory<", width/2, 6*height/12);
  } else {
    textFont(mono);
    text("Trajectory", width/2, 6*height/12);
  };
  
  if (togglerStatus == 2) {
    textFont(monoBold);
    text(">Demo<", width/2, 7*height/12);
  } else {  
    textFont(mono);
    text("Demo", width/2, 7*height/12);
  };
  
  if (togglerStatus == 3) { 
    textFont(monoBold);  
    text(">Render<", width/2, 8*height/12);
  } else {
    textFont(mono);
    text("Render", width/2, 8*height/12);
  };
  
  textFont(mono);
  fill(100);
  text("Navigate with ARROW KEYS | Select with ENTER", width/2, height-(height/20) );
  
  firstBackground = true;
  previousCompleted = 0;
 
}



///SETTINGS///

public void settingsMenu() {
  background(0);
  
  textFont(monoBold);
  fill(255);
  textAlign(CENTER);
  
  textSize(height/24);
  text("Render Settings", width/2, 2*height/12);
  textFont(mono);
  
  textSize(height/32);
  text("Select Planets", width/2, 3*height/12);
  
  for (int i = 0; i < 5; i++) {
    noStroke();
    fill(planetPlaceholders[i].colour);
    ellipse((i+1)*width/6, 4.5f*height/12, height/12, height/12);
    fill(255);
    text("Mass\n" + String.valueOf(planetPlaceholders[i].mass), (i+1)*width/6, 6*height/12);

    if (planetPlaceholders[i].selected == false) {
      fill(0,0,0,200);
      noStroke();
      rect((i+1)*width/6-(width/12), 3.5f*height/12, width/6, 3.8f*width/12);
    }
    
    if (settingsToggler < 5) {
      noFill();
      stroke(255);
      rect((settingsToggler+1)*width/6-(width/12), 3.5f*height/12, width/6, 3.8f*width/12);
      noStroke();
    }
   
  }
  
  fill(255);
  
  if (settingsToggler == 5) {
    textFont(monoBold);
    text(">Demo Scale = " + demoScale + "<", width/2, 8.5f*height/12);
  } else {  
    textFont(mono);
    text("Demo Scale = " + demoScale, width/2, 8.5f*height/12);
  }

   if (settingsToggler == 6) {
    textFont(monoBold);
    text(">Friction = " + friction + "<", width/2, 9.5f*height/12);
  } else {  
    textFont(mono);
    text("Friction = " + friction, width/2, 9.5f*height/12);
  }

  textFont(mono);
  fill(100);
  textSize(height/32);
  textLeading(height/32 + 10);
  text("Navigate and change mass with ARROW KEYS \n Select with ENTER", width/2, height-(height/12) );
  
};



///TRAJECTORY///

public void trajectory() {
  
  //This is done just to draw a background over the menu, the fading effect on particles 
  if (firstBackground == true) {
    background(0);
    firstBackground = false;
  };
  
  noStroke();
  fill(0,0,0,4);
  rect(0,0,width,height);
  
  render();
  
  for (int i=0; i < particles.size(); i++ ) {
    particles.get(i).display();
    if (particles.get(i).status != 5) {
      particles.remove(i);
      particles.add(new Particle(new PVector (Math.round(random(0,width)), Math.round(random(0,height)))));
    }
  }
  
}


///DEMO///

public void demo() {
  render();
  for (int i=0; i < particles.size(); i++ ) {
      particles.get(i).display();
  }
}


///RENDER///

public void render() {
  
  if (arraysLoaded == false) {
    loadArrays();
  }
  
  //Checks particle status and assigns color corresponding cell accordingly
  if (statusHandler.render == true || statusHandler.demo == true) {
    background(0);
  for (int i = 0; i < cells.size(); i++) {
    noFill();
    int s = particles.get(i).status;
    switch (s) {
      case 5: break;
      case 0: fill(planets.get(s).colour); break;
      case 1: fill(planets.get(s).colour); break;
      case 2: fill(planets.get(s).colour); break;
      case 3: fill(planets.get(s).colour); break;
      case 4: fill(planets.get(s).colour); break;     
    };
    cells.get(i).display();
  };
  }
  
  //Draws planets
  for (int i = 0; i < planets.size() ; i++) {
    noStroke();
    fill(planets.get(i).colour);
    planets.get(i).display();  
  };
  
  //Prints progress to console
  if (frameCount % 60 == 0) {
     thread("showProgress");
  }
  
  //Executes particle trajectory computation in parallel 
  //on a different thread to speed up the simulation
  thread("computeToNewThead");
    
}

///RUN PARTICLE UPDATE ON NEW THREAD///

public void computeToNewThead() {
  for (int i = 0; i < particles.size() ; i++) {
    if (particles.get(i).status == 5){
      particles.get(i).run();  
    }
  }
}




///////////////////////
///KEY INPUT HANDLER///
///////////////////////

public void keyPressed() {
  
  ///MENU///
 
  if (statusHandler.menu == true) {
    if (key == CODED) {  
      
      if (keyCode == RIGHT || keyCode == DOWN) {
        togglerStatus++;
        if (togglerStatus > 3) {
          togglerStatus = 0;
        }
        
      } else if (keyCode == LEFT || keyCode == UP ) {
        togglerStatus--;
        if (togglerStatus < 0) {
          togglerStatus = 3;
        }
      }
      
    };
    
    if (key == ENTER && togglerStatus == 0){
      statusHandler.menu       = false;
      statusHandler.settingsMenu = true;
      key = ' '; //This avoids selecting/unselecting the first planet in settingsMenu;
    };
    
    if (key == ENTER && togglerStatus == 1){
      statusHandler.menu   = false;
      statusHandler.trajectory = true;
    };
    
    if (key == ENTER && togglerStatus == 2){
      statusHandler.menu   = false;
      statusHandler.demo = true;
    };
    
    if (key == ENTER && togglerStatus == 3){
      statusHandler.menu   = false;
      statusHandler.render = true;
    };
  };
  
  ///BACK TO MENU///

  if (key == DELETE || key == BACKSPACE) {
      statusHandler.menu   = true;
      statusHandler.demo   = false;
      statusHandler.trajectory = false;
      statusHandler.render = false;
      statusHandler.settingsMenu = false;
  }
  
  ///SETTINGS///
  
  if (statusHandler.settingsMenu == true) {
    
    if (settingsToggler < 5) {
      if (key == ENTER) {
          planetPlaceholders[settingsToggler].selected = !planetPlaceholders[settingsToggler].selected;      
       }
    }
      
    if (key == CODED) {  
      
      if (keyCode == RIGHT) {
        settingsToggler++;
        if (settingsToggler > 6) {
          settingsToggler = 0;
        }
      } else if (keyCode == LEFT) {
        settingsToggler--;
        if (settingsToggler < 0) {
          settingsToggler = 6;
        }
      }
      
      if (settingsToggler < 4) {
        if (planetPlaceholders[settingsToggler].selected == true) {
          if (keyCode == UP) {
            planetPlaceholders[settingsToggler].mass += 10e+8f;
          } else if (keyCode == DOWN) {
            planetPlaceholders[settingsToggler].mass -= 10e+8f;
          }
          
          if (planetPlaceholders[settingsToggler].mass > 50e+8f) {
            planetPlaceholders[settingsToggler].mass = 50e+8f;
          } else if (planetPlaceholders[settingsToggler].mass < 10e+8f) {
            planetPlaceholders[settingsToggler].mass = 10e+8f;
          }
        }
      } 
      
      if (settingsToggler == 5) {
        if (keyCode == UP) {
          demoScale++;
        } else if (keyCode == DOWN) {
          demoScale--;
        }
        if (demoScale < 1) {
          demoScale = 1;
        }
      }
      
      if (settingsToggler == 6) {
        if (keyCode == UP) {
          friction += 0.001f;
        } else if (keyCode == DOWN) {
          friction -= 0.001f;
        }
        
         if (friction < 0) {
          friction = 0;
        } else if (friction > 1) {
          friction = 1;
        }
        
        friction = Float.parseFloat(nf(friction, 0, 3));
      }
      
    }

  }
  
  ///RENDER///
  if (statusHandler.render == true) {
    exportImg();
  };
  
};





/////////////////////////
///PROGRESS CALCULATOR///
/////////////////////////

public void showProgress() {
  time = millis()/1000;
  completed = 0;
  
  for (int i = 0; i < particles.size(); i++) {
    if (particles.get(i).status != 5) { 
      completed++;
    };
  }
  
  completion = completed/total;
  float efficency = (completed-previousCompleted)/(time-previousTime);
  progress = Math.floor(completion*10000)/100 + "%" + " | " + efficency + " particles per second";
  println(progress);
  previousTime = time;
  previousCompleted = completed;
};





//////////////////
///IMG EXPORTER///
//////////////////

public void exportImg() {
  if (key == ENTER) {
    int y = year();
    int m = month();
    int d = day();
    int h = hour();
    int n = minute();
    int s = second();
    String filename = "sketch_" + y + m + d + "_" + h + n + s + ".png";
    save(filename);
  }
}





/////////////
///CLASSES///
/////////////

///PLANET///
class Planet {
  PVector pos;
  float mass;
  int colour; 
  
  Planet(PVector l, float _mass, int _colour){
    pos = l.get();
     mass = _mass;
     colour = _colour;
  }
 
  public void display() {
    strokeWeight(3);
    stroke(255);
    ellipse(pos.x,pos.y,planetSize,planetSize);
  }
}


///PARTICLE///
class Particle {
  PVector pos;
  PVector p_pos;
  PVector vel;
  PVector gravity;
  int status;
  float mass; 
  
  Particle(PVector l){
    pos = l.get();
    p_pos = new PVector(0,0);
    vel = new PVector (0,0);
    status = 5;
    mass = 1;
  }
  
  public void run() {
    applyForce();
    update();
  }
 
  public void update() {      
    p_pos = pos.get();
 
    //I add some sort of friction to avoid perpetual motions
    vel.mult(1-friction);
    
    vel.add(gravity);
    pos.add(vel);
  }
    
  public void display() {
    noStroke();
    fill(255);
    if (statusHandler.trajectory == true) {    
      ellipse(pos.x,pos.y, 2,2);
    }  else  {
      ellipse(pos.x,pos.y, 10,10);}
      stroke(255);
    if (p_pos.x != 0 && p_pos.y != 0) {
      line(p_pos.x, p_pos.y, pos.x, pos.y);
    }
  }
  
  
  public void applyForce() {
    
    float Xtf = 0;
    float Ytf = 0;
    
    for (int i = 0; i < planets.size() ; i++) {
      
      float Xdist = planets.get(i).pos.x - this.pos.x;
      float Ydist = planets.get(i).pos.y - this.pos.y;
      
      double distSq = Math.pow(Xdist, 2)+Math.pow(Ydist, 2);  
      
      //10e+7 is added to the denominator to avoid gravitational slingshots and near-infinite speeds
      //double g = 10e+8/(10e+7+distSq);
      double g =  planets.get(i).mass * this.mass / (10e+7f+distSq);
           
      double cosT = Xdist/distSq;
      double sinT = Ydist/distSq;
   
      //The two extra lines of code serve the sole purpose of converting the double to float
      //I need to do this because PVectors can't take double variables as parameters
      
      double Xforce = g*cosT;    
      Double Xfor = Double.valueOf(Xforce);
      float Xf = Xfor.floatValue();

      double Yforce = g*sinT;
      Double Yfor = Double.valueOf(Yforce);
      float Yf = Yfor.floatValue();
    
      Xtf = Xtf+Xf;
      Ytf = Ytf+Yf;
      
      updateStatus(distSq, i);
    }
    
    gravity = new PVector(Xtf, Ytf);
  
  }
  
  public void updateStatus(double _distSq, int _i) {
    Double distS = Double.valueOf(_distSq);
    float dist = sqrt(distS.floatValue());
      
    if (dist < planetSize && Math.abs(vel.x) < 1 && Math.abs(vel.y) < 1) {
      status = _i;
    }
  }
  
}

///CELLS///
class Cell {
  PVector pos;
  float size;
  
  Cell (PVector l, float _size) {
    pos = l.get();
    size = _size;
  }
  
  public void display() {
    if (statusHandler.demo == false) {noStroke();}
    rect(pos.x, pos.y, size, size);
  }
  
}

///STATUS HANDLER///
class statusHandler {
  boolean menu;
  boolean demo;
  boolean trajectory;
  boolean render;
  boolean settingsMenu;
  
  statusHandler () {
    menu   = true;
    demo   = false;
    trajectory = false;
    render = false;
    settingsMenu = false;
  }
};

class planetPlaceholder {
  boolean selected;
  int colour;
  float mass;
  
  planetPlaceholder(int _colour, boolean _selected) {
    selected = _selected;
    colour = _colour;    
    mass = 10e+8f;
  }
}







  
  
  
  public void settings() {  size(1080,1080); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "chaotic_behaviour_simulation_100721" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
