import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

//Represents a single square of the game area
class Cell {
  // In logical coordinates, with the origin at the top-left corner of the screen
  Cell left;
  Cell top;
  Cell right;
  Cell bottom;
  int x;
  int y;
  Color color;
  boolean flooded;

  Cell(int x, int y, Color color, boolean flooded) {
    this.x = x;
    this.y = y;
    this.color = color;
    this.flooded = flooded;
    this.left = null;
    this.top = null;
    this.right = null;
    this.bottom = null;
  }

  public void floodAdjacentsHelper(Color c) {

    if(!(this.top == null) && (this.top.color.equals(this.color))) {
      this.top.color = c;
      this.top.flooded = true;
    }
    if(!(this.left == null) && (this.left.color.equals(this.color))) {
      this.left.color = c;
      this.left.flooded = true;

    }
    if(!(this.right == null) && (this.right.color.equals(this.color))) {
      this.right.color = color;
      this.right.flooded = true;

    }
    if(!(this.bottom == null) && (this.bottom.color.equals(this.color))) {
      this.bottom.color = c;
      this.bottom.flooded = true;

    }
  }
}  

// represents a FloodIt World
class FloodItWorld extends World {

  ArrayList<Cell> board;
  int size;
  Random rand;
  Color floodColor;
  Integer chancesTotal;
  Integer chancesUsed;
  Integer secondsLeft;
  ArrayList<Cell> recentlyFlooded;

  FloodItWorld(int size) {
    this.size = size;
    this.rand = new Random();
    this.constructBoard();
    this.floodColor = this.board.get(0).color;
    chancesTotal = this.size * 2 + this.size / 2;
    chancesUsed = 0;
    this.secondsLeft = 10;
    recentlyFlooded = new ArrayList<Cell>();
  }
  
  FloodItWorld(int size, Random rand) {
    this.size = size;
    this.rand = rand;
    this.constructBoard();
    this.floodColor = this.board.get(0).color;
    chancesTotal = this.size * 2 + this.size / 2;
    chancesUsed = 0;
    this.secondsLeft = 10;
    recentlyFlooded = new ArrayList<Cell>();
  }


//method that makes the WorlScene
 public WorldScene makeScene() {
   int width = this.size * 30;
   int height = this.size * 30;
   WorldScene game = new WorldScene(width, height);
   WorldImage gamePic = new EmptyImage();
   for (int i = this.size - 1; i >= 0; i--) {
     gamePic = new AboveImage(makeRow(i), gamePic);
   }
   game.placeImageXY(gamePic, width / 2, height / 2);
   game.placeImageXY(new TextImage(chancesUsed.toString() + "/" + this.chancesTotal.toString(), 30, Color.BLACK), width + 30, height + 30);
   game.placeImageXY(new TextImage(this.secondsLeft.toString(), 30, Color.black), width + 30, 30);
   
   if(this.chancesUsed > this.chancesTotal) {
     game.placeImageXY(new TextImage("You Lose!", 30, Color.black), width + 70, 70);
   } else if (this.allColorsSame()) {
     game.placeImageXY(new TextImage("You Win!", 30, Color.black), width + 70, 70);
   }
   
   return game;
 }
  
  public void onKeyEvent(String key) {
    if(key.equals("r")) {
      this.constructBoard();
      this.chancesTotal = this.size * 2;
      this.chancesUsed = 0;
      this.secondsLeft = 10;
    }
  }
  
  public void onMouseClicked(Posn pos) {
    int xValue = pos.x / 30;
    int yValue = pos.y / 30;
    
    if (pos.x < this.size * 30 && pos.y < this.size * 30) {
      int index = xValue + yValue * this.size;
      this.floodColor = this.board.get(index).color;
      for(int i = 0; i < this.board.size(); i++) {
        if(this.board.get(i).flooded) {
          this.board.get(i).color = this.floodColor;
        }
      }
      floodAdjacents();
      this.chancesUsed ++;
      this.secondsLeft = 10;
    }
  }
  
  public void onTick() {
    
    if(this.secondsLeft == 0) {
      this.chancesUsed ++;
      this.secondsLeft = 10;
    } else {
      this.secondsLeft --;
    }
    
  }
  
  
  
  void floodAdjacents() {
    for(int i = 0; i < this.board.size(); i++) {
      if (this.board.get(i).flooded) {
        this.board.get(i).floodAdjacentsHelper(this.floodColor);
      }
    }
  }
  
 

  // method that makes the rows for the world
  WorldImage makeRow(int row) {
    WorldImage img = new EmptyImage();
    for (int i = this.size - 1; i >= 0; i--) {
      img = new BesideImage(new RectangleImage(30, 30, "solid", 
          this.board.get(i + (this.size * row)).color), img);
    }
    return img;
  }
  
  // creates the board containing the cells
  void constructBoard() {
    ArrayList<Cell> result = new ArrayList<Cell>();
    ArrayList<Color> colors = new ArrayList<Color>(Arrays.asList(
        Color.blue, Color.red, Color.green, Color.pink, Color.orange, Color.magenta));
    for (int i = 0; i < this.size; i++) {
      for (int j = 0; j < this.size; j++) {
        result.add(new Cell(j, i, colors.get(this.rand.nextInt(colors.size())), false));
      }
    }

    // sets bottom for all Cells
    for (int i = 0; i < this.size * (this.size - 1); i += this.size) {
      for (int j = 0; j < this.size; j++) {
        result.get(i + j).bottom = result.get(i + j + this.size);
      }
    }

    // sets right for all Cells
    for (int i = 0; i < this.size * (this.size - 1) + 1; i += this.size) {
      for (int j = 0; j < this.size - 1; j++) {
        result.get(i + j).right = result.get(j + i + 1);
      }
    }

    // sets left for all Cells
    for (int i = this.size * this.size - 1; i > 0; i -= this.size) {
      for (int j = 0; j < this.size - 1; j++) {
        result.get(i - j).left = result.get(i - j - 1);
      }
    }


    // sets top for all Cells
    for (int i = this.size * this.size - 1; i >= this.size; i -= this.size) {
      for (int j = 0; j < this.size; j++) {
        result.get(i - j).top = result.get(i - j - this.size);
      }
    }
    
    this.board = result;
    this.board.get(0).flooded = true;
  }
  
  boolean allColorsSame() {
    for(int i = 0; i < this.board.size(); i++) {
      if (!(this.board.get(i).color.equals(this.floodColor))) {
        return false;
      }
    }
    return true;
  }


}

// examples and tests for FloodIt
class FloodExamples {
  Random seeded = new Random(2);
  Random seeded2 = new Random(2);
  
  FloodItWorld testGame = new FloodItWorld(4);
  FloodItWorld testGame2 = new FloodItWorld(6);
  FloodItWorld testGame3 = new FloodItWorld(3);
  FloodItWorld testGame0 = new FloodItWorld(1);
  FloodItWorld testGame4 = new FloodItWorld(2);
  FloodItWorld testGame10 = new FloodItWorld(10);
 
  FloodItWorld testGame5 = new FloodItWorld(1, seeded);
  FloodItWorld testGame5Copy = new FloodItWorld(1, seeded);
  
  FloodItWorld testGame6 = new FloodItWorld(2, seeded);
  FloodItWorld testGame6Copy = new FloodItWorld(2, seeded);

  // testing the location of cells
  boolean test(Tester t) {

    return t.checkExpect(this.testGame.board.get(2).left, this.testGame.board.get(1))
        && t.checkExpect(this.testGame.board.get(2).right, this.testGame.board.get(3))
        && t.checkExpect(this.testGame.board.get(2).bottom, this.testGame.board.get(6))
        && t.checkExpect(this.testGame.board.get(6).top, this.testGame.board.get(2))
        && t.checkExpect(this.testGame.board.get(7).top, this.testGame.board.get(2).right)
        && t.checkExpect(this.testGame.board.get(15).bottom, null);
  }
  
  // testing the top for cells
  boolean testTop(Tester t) {
    return t.checkExpect(this.testGame3.board.get(0).top, null)
        && t.checkExpect(this.testGame3.board.get(1).top, null)
        && t.checkExpect(this.testGame3.board.get(2).top, null)
        && t.checkExpect(this.testGame3.board.get(3).top, this.testGame3.board.get(0))
        && t.checkExpect(this.testGame3.board.get(4).top, this.testGame3.board.get(1))
        && t.checkExpect(this.testGame3.board.get(5).top, this.testGame3.board.get(2))
        && t.checkExpect(this.testGame3.board.get(6).top, this.testGame3.board.get(3))
        && t.checkExpect(this.testGame3.board.get(7).top, this.testGame3.board.get(4))
        && t.checkExpect(this.testGame3.board.get(8).top, this.testGame3.board.get(5));
  }
  
  // testing the right for cells
  boolean testRight(Tester t) {
    return t.checkExpect(this.testGame3.board.get(0).right, this.testGame3.board.get(1))
        && t.checkExpect(this.testGame3.board.get(1).right, this.testGame3.board.get(2))
        && t.checkExpect(this.testGame3.board.get(2).right, null)
        && t.checkExpect(this.testGame3.board.get(3).right, this.testGame3.board.get(4))
        && t.checkExpect(this.testGame3.board.get(4).right, this.testGame3.board.get(5))
        && t.checkExpect(this.testGame3.board.get(5).right, null)
        && t.checkExpect(this.testGame3.board.get(6).right, this.testGame3.board.get(7))
        && t.checkExpect(this.testGame3.board.get(7).right, this.testGame3.board.get(8))
        && t.checkExpect(this.testGame3.board.get(8).right, null);
  }
  
  // testing the bottom for cells
  boolean testBottom(Tester t) {
    return t.checkExpect(this.testGame3.board.get(0).bottom, this.testGame3.board.get(3))
        && t.checkExpect(this.testGame3.board.get(1).bottom, this.testGame3.board.get(4))
        && t.checkExpect(this.testGame3.board.get(2).bottom, this.testGame3.board.get(5))
        && t.checkExpect(this.testGame3.board.get(3).bottom, this.testGame3.board.get(6))
        && t.checkExpect(this.testGame3.board.get(4).bottom, this.testGame3.board.get(7))
        && t.checkExpect(this.testGame3.board.get(5).bottom, this.testGame3.board.get(8))
        && t.checkExpect(this.testGame3.board.get(6).bottom, null)
        && t.checkExpect(this.testGame3.board.get(7).bottom, null)
        && t.checkExpect(this.testGame3.board.get(8).bottom, null);
  }
  
  // testing the left for cells
  boolean testLeft(Tester t) {
    return t.checkExpect(this.testGame3.board.get(0).left, null)
        && t.checkExpect(this.testGame3.board.get(1).left, this.testGame3.board.get(0))
        && t.checkExpect(this.testGame3.board.get(2).left, this.testGame3.board.get(1))
        && t.checkExpect(this.testGame3.board.get(3).left, null)
        && t.checkExpect(this.testGame3.board.get(4).left, this.testGame3.board.get(3))
        && t.checkExpect(this.testGame3.board.get(5).left, this.testGame3.board.get(4))
        && t.checkExpect(this.testGame3.board.get(6).left, null)
        && t.checkExpect(this.testGame3.board.get(7).left, this.testGame3.board.get(6))
        && t.checkExpect(this.testGame3.board.get(8).left, this.testGame3.board.get(7));
  }
  
  // testing the x-cord of cells
  boolean checkCoordX(Tester t) {
    return t.checkExpect(this.testGame3.board.get(0).x, 0)
        && t.checkExpect(this.testGame3.board.get(1).x, 1)
        && t.checkExpect(this.testGame3.board.get(2).x, 2)
        && t.checkExpect(this.testGame3.board.get(3).x, 0)
        && t.checkExpect(this.testGame3.board.get(4).x, 1)
        && t.checkExpect(this.testGame3.board.get(5).x, 2)
        && t.checkExpect(this.testGame3.board.get(6).x, 0)
        && t.checkExpect(this.testGame3.board.get(7).x, 1)
        && t.checkExpect(this.testGame3.board.get(8).x, 2);
  }
  
  // testing the y-cord for cells
  boolean testCoordY(Tester t) {
    return t.checkExpect(this.testGame3.board.get(0).y, 0)
        && t.checkExpect(this.testGame3.board.get(1).y, 0)
        && t.checkExpect(this.testGame3.board.get(2).y, 0)
        && t.checkExpect(this.testGame3.board.get(3).y, 1)
        && t.checkExpect(this.testGame3.board.get(4).y, 1)
        && t.checkExpect(this.testGame3.board.get(5).y, 1)
        && t.checkExpect(this.testGame3.board.get(6).y, 2)
        && t.checkExpect(this.testGame3.board.get(7).y, 2)
        && t.checkExpect(this.testGame3.board.get(8).y, 2);
  }
  
  // testing makeRow()
  boolean testMakeRow(Tester t) {
    return t.checkExpect(this.testGame0.makeRow(0), 
        new BesideImage(
            new RectangleImage(30, 30, "solid", 
                this.testGame0.board.get(0 + (this.testGame0.size * 0)).color), new EmptyImage()))
        && t.checkExpect(this.testGame3.makeRow(2), 
            new BesideImage(
                new RectangleImage(30, 30, "solid", 
                    this.testGame3.board.get(2 + (this.testGame3.size * 2)).color),
                new BesideImage(
                    new RectangleImage(30, 30, "solid", 
                        this.testGame3.board.get(1 + (this.testGame3.size * 2)).color), 
                    new BesideImage(
                            new RectangleImage(30, 30, "solid", 
                                this.testGame3.board.get(
                                    0 + (this.testGame3.size * 2)).color), new EmptyImage()))))
        && t.checkExpect(this.testGame.makeRow(3), 
            new BesideImage(
                new RectangleImage(30, 30, "solid", 
                    this.testGame.board.get(3 + (this.testGame.size * 3)).color),
                new BesideImage(
                    new RectangleImage(30, 30, "solid", 
                        this.testGame.board.get(2 + (this.testGame.size * 3)).color),
                    new BesideImage(
                        new RectangleImage(30, 30, "solid", 
                            this.testGame.board.get(1 + (this.testGame.size * 3)).color), 
                        new BesideImage(
                                new RectangleImage(30, 30, "solid", 
                                    this.testGame.board.get(0 + 
                                        (this.testGame.size * 3)).color), new EmptyImage())))));
  }
  
  // tests for makeScene()
  boolean testMakeScene(Tester t) {
    WorldScene world1 = new WorldScene(this.testGame0.size * 30, this.testGame0.size * 30);
    world1.placeImageXY(new AboveImage(this.testGame0.makeRow(0), new EmptyImage()), 
        this.testGame0.size * 15, this.testGame0.size * 15);
    WorldScene world2 = new WorldScene(this.testGame3.size * 30, this.testGame3.size * 30);
    world2.placeImageXY(new AboveImage(this.testGame3.makeRow(2), 
        new AboveImage(this.testGame3.makeRow(1), new AboveImage(this.testGame3.makeRow(0), 
            new EmptyImage()))), 
        this.testGame3.size * 15, this.testGame3.size * 15);
    return t.checkExpect(this.testGame0.makeScene(), world1)
        && t.checkExpect(this.testGame3.makeScene(), world2);



  }
  
  // tests for constructBoard()
  void testConstructBoard(Tester t) {
    // initial data and test for initial data
    this.testGame5.board = new ArrayList<Cell>();
    this.testGame6.board = new ArrayList<Cell>();
    t.checkExpect(this.testGame6.board, new ArrayList<Cell>());
    t.checkExpect(this.testGame5.board, new ArrayList<Cell>());
    // mutate data
    this.testGame5.constructBoard();
    this.testGame5Copy.board.get(0).color = Color.pink;
    this.testGame5Copy.board.get(0).flooded = true;
    t.checkExpect(this.testGame5, this.testGame5Copy);
    this.testGame6.constructBoard();
    this.testGame6Copy.board.get(0).color = Color.red;
    this.testGame6Copy.board.get(1).color = Color.blue;
    this.testGame6Copy.board.get(2).color = Color.green;
    this.testGame6Copy.board.get(3).color = Color.blue;
    this.testGame6Copy.board.get(0).flooded = true;
    this.testGame6Copy.board.get(1).flooded = false;
    this.testGame6Copy.board.get(2).flooded = false;
    this.testGame6Copy.board.get(3).flooded = false;
    t.checkExpect(this.testGame6, this.testGame6Copy);
    
  }
  
  boolean testFloodAdjacentHelper(Tester t) {
    return true;
  }
  
  boolean testFloodAdjacent(Tester t) {
    return true;
  }
  
  boolean testOnMouseClick(Tester t) {
    return true;
  }


  /*
  // test for drawScene()
  boolean testDrawGame(Tester t) {
    WorldCanvas c = new WorldCanvas(600, 600);
    return c.drawScene(this.testGame2.makeScene()) && c.show();
  }
  */
  
  void testGame(Tester t) {
    testGame10.bigBang(1000,1000, 1.0 / 1);
  }
 




}
