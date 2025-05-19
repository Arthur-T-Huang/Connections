import tester.*; // The tester library
import javalib.worldimages.*; // images, like RectangleImage or OverlayImages
import javalib.impworld.*; // the abstract World class and the big-bang library
import java.util.ArrayList;
import java.util.Random;
import java.awt.Color; // general colors (as triples of red,green,blue values)
// and predefined colors (Red, Green, Yellow, Blue, Black, White)

//Sets up the world for the connections game 
class ConnectionsWorld extends World {
  int width = 900;
  int height = 700;
  SingleGame theChosenGame;
  SingleGame initialGame;
  Random rand;
  int lives;
  int y;

  ConnectionsWorld(ArrayList<SingleGame> possibleBoards) {
    this.rand = new Random();
    this.theChosenGame = possibleBoards.get(rand.nextInt(5));
    this.initialGame = theChosenGame;
    this.lives = 4;
    this.y = 190;
    this.theChosenGame.uncorrect();
    this.theChosenGame.undraw();

  }

  // constructor for tests
  ConnectionsWorld(SingleGame theChosenGame) {
    this.rand = new Random();
    this.theChosenGame = theChosenGame;
    this.lives = 4;
    this.y = 190;
    this.theChosenGame.uncorrect();
    this.theChosenGame.undraw();
  }

  // Makes the world scene for the game
  public WorldScene makeScene() {
    // draw starting board
    int yLocal = this.y;
    WorldImage startImg = new FromFileImage("Images/mountainpic.jpg");
    WorldScene scene = new WorldScene(width, height);
    scene.placeImageXY(startImg, 280, 350);
    scene.placeImageXY(new TextImage("CONNECTIONS", 30, FontStyle.ITALIC, Color.BLACK), 430, 40);
    scene.placeImageXY(new TextImage("Lives: " + this.lives, 20, FontStyle.ITALIC, Color.BLACK),
        430, 95);
    scene.placeImageXY(new StarImage(16, OutlineMode.SOLID, Color.RED), 370, 95);
    int yAnswer = 190;
    int yTitle = 180;
    int yWords = 205;
    for (Answer a : this.theChosenGame.answers) {
      if (a.shouldDraw) {
        scene.placeImageXY(new RectangleImage(800, 100, OutlineMode.SOLID, a.c), 440, yAnswer);
        scene.placeImageXY(new TextImage(a.title, 20, FontStyle.BOLD, Color.BLACK), 440, yTitle);
        scene.placeImageXY(new TextImage(a.wordGroupDisplay(), 15, FontStyle.BOLD, Color.BLACK),
            440, yWords);

        yAnswer += 100;
        yTitle += 100;
        yWords += 100;
        yLocal += 100;

      }
    }

    // draws the tiles
    for (int i = 0; i < this.theChosenGame.unCorrectedList().size() / 4; i++) {
      int x = 20;
      for (int j = i * 4; j < (i + 1) * 4; j++) {
        scene.placeImageXY(this.theChosenGame.drawTiles(j), (x + 100), yLocal);
        x = x + 200;
      }
      yLocal += 100;

    }
    scene.placeImageXY(new OverlayImage(new TextImage(
        "Press d to deselect all tiles : Press s to shuffle the board : Press r to reset the game",
        20, FontStyle.ITALIC, Color.BLACK),
        new RectangleImage(850, 50, OutlineMode.SOLID, new Color(225, 225, 225))), 430, 580);
    scene.placeImageXY(
        new OverlayImage(new TextImage("SUBMIT :)", 30, FontStyle.ITALIC, Color.BLACK),
            new RectangleImage(150, 50, OutlineMode.SOLID, new Color(193, 92, 247))),
        430, 640);

    // if losing the game
    if (lives <= 0) {
      WorldImage img = new FromFileImage("Images/abhi.jpg");
      WorldScene loseScene = new WorldScene(width, height);
      loseScene.placeImageXY(startImg, 280, 350);
      loseScene.placeImageXY(new TextImage("YOU LOST! :(", 30, FontStyle.ITALIC, Color.BLACK), 600,
          300);
      loseScene.placeImageXY(
          new TextImage("Press r to reset the game", 20, FontStyle.ITALIC, Color.BLACK), 600, 400);
      loseScene.placeImageXY(img, 250, 350);
      return loseScene;
    }

    // if winning the game
    if (this.theChosenGame.unCorrectedList().size() == 0) {
      WorldImage winImg = new FromFileImage("Images/vincentog.jpeg");
      WorldScene winScene = new WorldScene(width, height);
      winScene.placeImageXY(startImg, 280, 350);
      winScene.placeImageXY(new TextImage("YOU WON! :)", 30, FontStyle.ITALIC, Color.BLACK), 600,
          300);
      winScene.placeImageXY(
          new TextImage("Press r to reset the game", 20, FontStyle.ITALIC, Color.BLACK), 600, 400);
      winScene.placeImageXY(winImg, 250, 350);

      return winScene;
    }
    return scene;

  }

  // run this code whenever the mouse is clicked
  public void onMouseClicked(Posn pos) {
    int mod = (4 - (this.theChosenGame.unCorrectedList().size() / 4));
    int index = ((pos.x - 50) / 200) + (4 * ((pos.y - 120 - (100 * mod)) / 105));
    if (index >= 0) {

      if (index < this.theChosenGame.unCorrectedList().size()) {
        if ((!this.theChosenGame.getTile(index).selected)
            && this.theChosenGame.selectedNum() == 4) {
          this.theChosenGame.getTile(index).selected = this.theChosenGame.getTile(index).selected;
        }
        else {
          this.theChosenGame.getTile(index).selected = !this.theChosenGame.getTile(index).selected;
        }

      }
    }

    ArrayList<String> chosenWords = new ArrayList<String>();
    if ((pos.x <= 505 && pos.x >= 355) && (pos.y <= 665 && pos.y >= 615)) {
      for (Tile t : this.theChosenGame.unCorrectedList()) {
        if (t.selected) {
          chosenWords.add(t.word);
        }
      }
      if (chosenWords.size() == 4) {
        for (Answer a : this.theChosenGame.answers) {
          if (a.matchesGroup(chosenWords)) {
            for (String s : chosenWords) {
              for (Tile t : this.theChosenGame.unCorrectedList()) {
                if (s.equals(t.word)) {
                  t.corrected = true;
                  t.selected = false;
                }
              }
            }
            a.shouldDraw = true;

          }
          else {
            for (Tile t : this.theChosenGame.unCorrectedList()) {
              t.selected = false;
            }

          }
        }
        if (this.theChosenGame.answerNum(chosenWords) == 0) {
          lives--;
        }

      }
    }
  }

  // run this code whenever a key is pressed
  public void onKeyEvent(String key) {
    if (key.equals("d")) {
      for (Tile t : this.theChosenGame.unCorrectedList()) {
        t.selected = false;
      }
    }
    if (key.equals("r")) {
      this.theChosenGame = new SingleGame(this.theChosenGame.answers);
      ConnectionsWorld resetScene = new ConnectionsWorld(this.theChosenGame);
      lives = 4;
      resetScene.makeScene();

    }

    if (key.equals("s")) {
      for (Tile t : this.theChosenGame.unCorrectedList()) {
        t.selected = false;
      }
      ArrayList<Tile> tempTiles = new ArrayList<Tile>();

      this.theChosenGame = new SingleGame(this.theChosenGame.unCorrectedList(),
          this.theChosenGame.answers);
      this.makeScene();

    }

  }

}

//represents a tile in the game 
class Tile {
  boolean selected;
  boolean corrected;
  String word;

  Tile(String word) {
    this.word = word;
    this.selected = false;
    this.corrected = false;
  }

  // draws the given active tile on the screen
  public OverlayImage drawTile() {
    if (this.selected) {
      return new OverlayImage(new TextImage(this.word, 20, FontStyle.BOLD, Color.BLACK),
          new RectangleImage(180, 90, OutlineMode.SOLID, new Color(140, 213, 224)));
    }
    else {
      return new OverlayImage(new TextImage(this.word, 20, FontStyle.BOLD, Color.BLACK),
          new RectangleImage(180, 90, OutlineMode.SOLID, new Color(203, 213, 224)));
    }

  }

}

//represents a single game board 
class SingleGame {
  ArrayList<Tile> tiles;
  ArrayList<Answer> answers;
  boolean test;
  Random rand;

  SingleGame(ArrayList<Answer> answers) {
    this.rand = new Random();
    this.tiles = new ArrayList<Tile>();
    ArrayList<Tile> tempTiles = new ArrayList<Tile>();

    for (int i = 0; i < answers.size(); i++) {
      Answer curr = answers.get(i);
      ArrayList<String> wordSet = curr.wg;
      for (int j = 0; j < wordSet.size(); j++) {
        tempTiles.add(new Tile(wordSet.get(j)));
      }
    }
    for (int i = 0; i < 16; i = i + 1) {
      int index = this.rand.nextInt(tempTiles.size());
      this.tiles.add(tempTiles.get(index));
      tempTiles.remove(index);
    }

    this.answers = answers;
  }

  SingleGame(ArrayList<Tile> tiles, ArrayList<Answer> answers) {
    this.rand = new Random();
    this.tiles = new ArrayList<Tile>();
    ArrayList<Tile> tempTiles = new ArrayList<Tile>();
    int size = tiles.size();
    for (int i = 0; i < size; i++) {
      tempTiles.add(tiles.get(i));

    }
    int tempSize = tempTiles.size();
    for (int i = 0; i < tempSize; i = i + 1) {
      int index = this.rand.nextInt(tempTiles.size());
      this.tiles.add(tempTiles.get(index));
      tempTiles.remove(index);

    }
    this.answers = answers;
  }

  SingleGame(ArrayList<Tile> tiles, ArrayList<Answer> answers, boolean test) {
    this.tiles = tiles;
    this.answers = answers;
    this.test = test;
    this.rand = new Random();
  }

  // returns the drawn word at the given index in the array list
  OverlayImage drawTiles(int index) {
    return this.unCorrectedList().get(index).drawTile();
  }

  // gets the tile at the given index of the array list of tiles
  Tile getTile(int index) {
    if (index < 0 || index >= this.unCorrectedList().size()) {
      throw new IndexOutOfBoundsException("The index is out of bounds!");
    }
    return this.unCorrectedList().get(index);
  }

  // counts the number of selected tiles in the game's tile list
  int selectedNum() {
    int result = 0;
    for (Tile t : this.tiles) {
      if (t.selected) {
        result += 1;
      }
    }
    return result;

  }

  // counts the number of incorrect tiles in the game's tile list
  int correctedNum() {
    int result = 0;
    for (Tile t : this.tiles) {
      if (!t.corrected) {
        result += 1;
      }
    }
    return result;

  }

  // EFFECT: turns every tile's corrected and selected values to false
  void uncorrect() {
    for (Tile t : this.tiles) {
      t.corrected = false;
      t.selected = false;
    }
  }

  // EFFECT: turns all of the answer's shouldDraw values to false
  void undraw() {
    for (Answer a : this.answers) {
      a.shouldDraw = false;
    }
  }

  // returns a list of only uncorrected tiles in this game
  ArrayList<Tile> unCorrectedList() {
    ArrayList<Tile> result = new ArrayList<Tile>();
    for (Tile t : this.tiles) {
      if (!t.corrected) {
        result.add(t);
      }
    }
    return result;
  }

  // counts the number of answer groups that match the given array list of strings
  int answerNum(ArrayList<String> string) {
    int result = 0;
    for (Answer a : this.answers) {
      if (a.matchesGroup(string)) {
        result += 1;
      }
    }
    return result;
  }

}

//represents one answer group 
class Answer {
  String title;
  Color c;
  ArrayList<String> wg;
  boolean shouldDraw;

  Answer(String title, Color c, ArrayList<String> wg) {
    this.title = title;
    this.c = c;
    this.wg = wg;
    this.shouldDraw = false;
  }

  // checks to see if the given selected tiles match this answer word group
  boolean matchesGroup(ArrayList<String> selectedTiles) {
    for (String item : selectedTiles) {
      if (!wg.contains(item)) {
        return false;
      }
    }
    return true;
  }

  // returns a string of all of the Strings in the word group
  String wordGroupDisplay() {
    String result = "";
    for (int i = 0; i < wg.size(); i++) {
      result = result + " " + wg.get(i);
    }

    return result;
  }

}

class ExamplesConnections {
  ArrayList<SingleGame> possibleBoards;
  SingleGame game1;
  SingleGame game2;
  SingleGame game3;
  SingleGame game4;
  SingleGame game5;

  Answer game1Answer1;
  Answer game1Answer2;
  Answer game1Answer3;
  Answer game1Answer4;

  Answer game2Answer1;
  Answer game2Answer2;
  Answer game2Answer3;
  Answer game2Answer4;

  Answer game3Answer1;
  Answer game3Answer2;
  Answer game3Answer3;
  Answer game3Answer4;

  Answer game4Answer1;
  Answer game4Answer2;
  Answer game4Answer3;
  Answer game4Answer4;

  Answer game5Answer1;
  Answer game5Answer2;
  Answer game5Answer3;
  Answer game5Answer4;

  ArrayList<Answer> answersListGame1;
  ArrayList<Answer> answersListGame2;
  ArrayList<Answer> answersListGame3;
  ArrayList<Answer> answersListGame4;
  ArrayList<Answer> answersListGame5;

  ArrayList<String> filament;
  ArrayList<String> trucks;
  ArrayList<String> fiveGroups;
  ArrayList<String> inhibitNo;

  ArrayList<String> milieu;
  ArrayList<String> tools;
  ArrayList<String> bar;
  ArrayList<String> luminary;

  ArrayList<String> weather;
  ArrayList<String> nba;
  ArrayList<String> keyboard;
  ArrayList<String> palindromes;

  ArrayList<String> enjoy;
  ArrayList<String> filler;
  ArrayList<String> lock;
  ArrayList<String> unitsMeasure;

  ArrayList<String> cards;
  ArrayList<String> clubs;
  ArrayList<String> yearn;
  ArrayList<String> insufficient;

  Tile ache;
  Tile tileLong;
  Tile pine;
  Tile thirst;

  Tile thread;
  Tile string;
  Tile fiber;
  Tile strand;

  Tile jackson;
  Tile maroon;
  Tile mc;
  Tile benFolds;

  Tile dump;
  Tile garbage;
  Tile pickup;
  Tile monster;

  Tile abandon;
  Tile freedom;
  Tile unrestraint;
  Tile spontaneity;

  SingleGame testGame;
  SingleGame testGame1;

  ArrayList<Tile> testTileList;
  ArrayList<Tile> testTileList1;

  ArrayList<Tile> tileList;

  ConnectionsWorld w;
  ConnectionsWorld newWorld;

  /*
   * ExamplesConnections() {
   * this.initData(); // Call initData() in the constructor before big bang
   * }
   */

  void initData() {
    // to add all the boards to
    this.possibleBoards = new ArrayList<SingleGame>();

    // setup for possible game 1
    this.filament = new ArrayList<String>();
    this.filament.add("THREAD");
    this.filament.add("STRING");
    this.filament.add("FIBER");
    this.filament.add("STRAND");
    this.game1Answer1 = new Answer("FILAMENT", Color.YELLOW, this.filament);

    this.trucks = new ArrayList<String>();
    this.trucks.add("MONSTER");
    this.trucks.add("DUMP");
    this.trucks.add("GARBAGE");
    this.trucks.add("PICKUP");
    this.game1Answer2 = new Answer("KINDS OF TRUCKS", new Color(93, 196, 240), this.trucks);

    this.fiveGroups = new ArrayList<String>();
    this.fiveGroups.add("JACKSON");
    this.fiveGroups.add("MAROON");
    this.fiveGroups.add("MC");
    this.fiveGroups.add("BEN FOLDS");
    this.game1Answer3 = new Answer("BANDS MINUS THE NUMBER FIVE", new Color(196, 145, 237),
        this.fiveGroups);

    this.inhibitNo = new ArrayList<String>();
    this.inhibitNo.add("ABANDON");
    this.inhibitNo.add("FREEDOM");
    this.inhibitNo.add("SPONTANEITY");
    this.inhibitNo.add("UNRESTRAINT");
    this.game1Answer4 = new Answer("UNINHIBITEDNESS", Color.GREEN, this.inhibitNo);

    this.answersListGame1 = new ArrayList<Answer>();
    this.answersListGame1.add(this.game1Answer1);
    this.answersListGame1.add(this.game1Answer2);
    this.answersListGame1.add(this.game1Answer3);
    this.answersListGame1.add(this.game1Answer4);

    this.game1 = new SingleGame(this.answersListGame1);

    this.possibleBoards.add(this.game1);

    // setup for possible game 2
    this.milieu = new ArrayList<String>();
    this.milieu.add("CIRCLE");
    this.milieu.add("SCENE");
    this.milieu.add("SPHERE");
    this.milieu.add("WORLD");
    this.game2Answer1 = new Answer("MILIEU", Color.YELLOW, this.milieu);

    this.tools = new ArrayList<String>();
    this.tools.add("COMPASS");
    this.tools.add("RULER");
    this.tools.add("STENCIL");
    this.tools.add("T-SQUARE");
    this.game2Answer2 = new Answer("ARCHITECTURAL DRAWING TOOLS", new Color(93, 196, 240),
        this.tools);

    this.bar = new ArrayList<String>();
    this.bar.add("CHART");
    this.bar.add("EXAM");
    this.bar.add("MITZVAH");
    this.bar.add("SOAP");
    this.game2Answer3 = new Answer("BAR __", new Color(196, 145, 237), this.bar);

    this.luminary = new ArrayList<String>();
    this.luminary.add("GREAT");
    this.luminary.add("ICON");
    this.luminary.add("LEGEND");
    this.luminary.add("LION");
    this.game2Answer4 = new Answer("LUMINARY", Color.GREEN, this.luminary);

    this.answersListGame2 = new ArrayList<Answer>();
    this.answersListGame2.add(this.game2Answer1);
    this.answersListGame2.add(this.game2Answer2);
    this.answersListGame2.add(this.game2Answer3);
    this.answersListGame2.add(this.game2Answer4);

    this.game2 = new SingleGame(this.answersListGame2);

    this.possibleBoards.add(this.game2);

    // setup for possible game 3
    this.weather = new ArrayList<String>();
    this.weather.add("HAIL");
    this.weather.add("RAIN");
    this.weather.add("SLEET");
    this.weather.add("SNOW");
    this.game3Answer1 = new Answer("WET WEATHER", Color.YELLOW, this.weather);

    this.nba = new ArrayList<String>();
    this.nba.add("BUCKS");
    this.nba.add("HEAT");
    this.nba.add("JAZZ");
    this.nba.add("NETS");
    this.game3Answer2 = new Answer("NBA TEAMS", Color.GREEN, this.nba);

    this.palindromes = new ArrayList<String>();
    this.palindromes.add("KAYAK");
    this.palindromes.add("LEVEL");
    this.palindromes.add("MOM");
    this.palindromes.add("RACECAR");
    this.game3Answer3 = new Answer("PALINDROMES", new Color(196, 145, 237), this.palindromes);

    this.keyboard = new ArrayList<String>();
    this.keyboard.add("OPTION");
    this.keyboard.add("RETURN");
    this.keyboard.add("SHIFT");
    this.keyboard.add("TAB");
    this.game3Answer4 = new Answer("KEYBOARD KEYS", new Color(93, 196, 240), this.keyboard);

    this.answersListGame3 = new ArrayList<Answer>();
    this.answersListGame3.add(this.game3Answer1);
    this.answersListGame3.add(this.game3Answer2);
    this.answersListGame3.add(this.game3Answer3);
    this.answersListGame3.add(this.game3Answer4);

    this.game3 = new SingleGame(this.answersListGame3);

    this.possibleBoards.add(this.game3);

    // setup for possible game 4
    this.enjoy = new ArrayList<String>();
    this.enjoy.add("FANCY");
    this.enjoy.add("LOVE");
    this.enjoy.add("RELISH");
    this.enjoy.add("SAVOR");
    this.game4Answer1 = new Answer("ENJOY", Color.YELLOW, this.enjoy);

    this.filler = new ArrayList<String>();
    this.filler.add("LIKE");
    this.filler.add("LITERALLY");
    this.filler.add("UM");
    this.filler.add("WELL");
    this.game4Answer2 = new Answer("FILLER WORDS", Color.GREEN, this.filler);

    this.unitsMeasure = new ArrayList<String>();
    this.unitsMeasure.add("CARROT");
    this.unitsMeasure.add("HURTS");
    this.unitsMeasure.add("JEWEL");
    this.unitsMeasure.add("OM");
    this.game4Answer3 = new Answer("HOMOPHONES OF UNITS OF MEASURE", new Color(196, 145, 237),
        this.unitsMeasure);

    this.lock = new ArrayList<String>();
    this.lock.add("CYLINDER");
    this.lock.add("PIN");
    this.lock.add("SPRING");
    this.lock.add("TUMBLER");
    this.game4Answer4 = new Answer("COMPONENTS OF A LOCK", new Color(93, 196, 240), this.lock);

    this.answersListGame4 = new ArrayList<Answer>();
    this.answersListGame4.add(this.game4Answer1);
    this.answersListGame4.add(this.game4Answer2);
    this.answersListGame4.add(this.game4Answer3);
    this.answersListGame4.add(this.game4Answer4);

    this.game4 = new SingleGame(this.answersListGame4);

    this.possibleBoards.add(this.game4);

    // setup for possible game 5
    this.cards = new ArrayList<String>();
    this.cards.add("CLUB");
    this.cards.add("DIAMOND");
    this.cards.add("HEART");
    this.cards.add("SPADE");
    this.game5Answer1 = new Answer("PLAYING CARD SUITS", Color.YELLOW, this.cards);

    this.clubs = new ArrayList<String>();
    this.clubs.add("IRON");
    this.clubs.add("PUTTER");
    this.clubs.add("WEDGE");
    this.clubs.add("WOOD");
    this.game5Answer2 = new Answer("GOLF CLUBS", Color.GREEN, this.clubs);

    this.insufficient = new ArrayList<String>();
    this.insufficient.add("LOW");
    this.insufficient.add("SHORT");
    this.insufficient.add("SHY");
    this.insufficient.add("WANTING");
    this.game5Answer3 = new Answer("INSUFFICIENT", new Color(196, 145, 237), this.insufficient);

    this.yearn = new ArrayList<String>();
    this.yearn.add("ACHE");
    this.yearn.add("LONG");
    this.yearn.add("PINE");
    this.yearn.add("THIRST");
    this.game5Answer4 = new Answer("YEARN", new Color(93, 196, 240), this.yearn);

    this.ache = new Tile("ACHE");
    this.tileLong = new Tile("LONG");
    this.pine = new Tile("PINE");
    this.thirst = new Tile("THIRST");

    this.tileList = new ArrayList<Tile>();
    this.tileList.add(ache);
    this.tileList.add(tileLong);
    this.tileList.add(pine);
    this.tileList.add(thirst);

    this.thread = new Tile("THREAD");
    this.string = new Tile("STRING");
    this.fiber = new Tile("FIBER");
    this.strand = new Tile("STRAND");

    this.jackson = new Tile("JACKSON");
    this.mc = new Tile("MC");
    this.benFolds = new Tile("BEN FOLDS");
    this.maroon = new Tile("MAROON");

    this.dump = new Tile("DUMP");
    this.garbage = new Tile("GARBAGE");
    this.pickup = new Tile("PICKUP");
    this.monster = new Tile("MONSTER");

    this.spontaneity = new Tile("SPONTANEITY");
    this.abandon = new Tile("ABANDON");
    this.unrestraint = new Tile("UNRESTRAINT");
    this.freedom = new Tile("FREEDOM");

    this.testTileList = new ArrayList<Tile>();
    this.testTileList.add(thread);
    this.testTileList.add(string);
    this.testTileList.add(fiber);
    this.testTileList.add(strand);

    this.testTileList1 = new ArrayList<Tile>();
    this.testTileList1.add(thread);
    this.testTileList1.add(string);
    this.testTileList1.add(fiber);
    this.testTileList1.add(strand);
    this.testTileList1.add(monster);
    this.testTileList1.add(dump);
    this.testTileList1.add(garbage);
    this.testTileList1.add(pickup);
    this.testTileList1.add(jackson);
    this.testTileList1.add(maroon);
    this.testTileList1.add(mc);
    this.testTileList1.add(benFolds);
    this.testTileList1.add(abandon);
    this.testTileList1.add(freedom);
    this.testTileList1.add(spontaneity);
    this.testTileList1.add(unrestraint);

    this.testGame = new SingleGame(this.testTileList, this.answersListGame1, true);
    this.testGame1 = new SingleGame(this.testTileList1, this.answersListGame1, true);

    this.answersListGame5 = new ArrayList<Answer>();
    this.answersListGame5.add(this.game5Answer1);
    this.answersListGame5.add(this.game5Answer2);
    this.answersListGame5.add(this.game5Answer3);
    this.answersListGame5.add(this.game5Answer4);

    this.game5 = new SingleGame(this.answersListGame5);

    this.possibleBoards.add(this.game5);
    this.w = new ConnectionsWorld(this.possibleBoards.get(0));
    this.newWorld = new ConnectionsWorld(this.testGame1);

  }

  void testBigBang(Tester t) {
    this.initData();
    ConnectionsWorld w = new ConnectionsWorld(this.possibleBoards);
    int worldWidth = 900;
    int worldHeight = 700;
    double tickRate = 0.1;
    w.bigBang(worldWidth, worldHeight, tickRate);

  }

  // tests the makeScene method
  void testMakeScene(Tester t) {
    initData();
    WorldScene scene = new WorldScene(900, 700);

    ConnectionsWorld w = new ConnectionsWorld(this.testGame1);
    WorldImage startImg = new FromFileImage("Images/mountainpic.jpg");
    scene.placeImageXY(startImg, 280, 350);
    scene.placeImageXY(new TextImage("CONNECTIONS", 30, FontStyle.ITALIC, Color.BLACK), 430, 40);
    scene.placeImageXY(new TextImage("Lives: " + 4, 20, FontStyle.ITALIC, Color.BLACK), 430, 95);
    scene.placeImageXY(new StarImage(16, OutlineMode.SOLID, Color.RED), 370, 95);
    scene.placeImageXY(this.testGame1.drawTiles(0), (120), 190);
    scene.placeImageXY(this.testGame1.drawTiles(1), (320), 190);
    scene.placeImageXY(this.testGame1.drawTiles(2), (520), 190);
    scene.placeImageXY(this.testGame1.drawTiles(3), (720), 190);
    scene.placeImageXY(this.testGame1.drawTiles(4), (120), 290);
    scene.placeImageXY(this.testGame1.drawTiles(5), (320), 290);
    scene.placeImageXY(this.testGame1.drawTiles(6), (520), 290);
    scene.placeImageXY(this.testGame1.drawTiles(7), (720), 290);
    scene.placeImageXY(this.testGame1.drawTiles(8), (120), 390);
    scene.placeImageXY(this.testGame1.drawTiles(9), (320), 390);
    scene.placeImageXY(this.testGame1.drawTiles(10), (520), 390);
    scene.placeImageXY(this.testGame1.drawTiles(11), (720), 390);
    scene.placeImageXY(this.testGame1.drawTiles(12), (120), 490);
    scene.placeImageXY(this.testGame1.drawTiles(13), (320), 490);
    scene.placeImageXY(this.testGame1.drawTiles(14), (520), 490);
    scene.placeImageXY(this.testGame1.drawTiles(15), (720), 490);
    scene.placeImageXY(new OverlayImage(new TextImage(
        "Press d to deselect all tiles : Press s to shuffle the board : Press r to reset the game",
        20, FontStyle.ITALIC, Color.BLACK),
        new RectangleImage(850, 50, OutlineMode.SOLID, new Color(225, 225, 225))), 430, 580);
    scene.placeImageXY(
        new OverlayImage(new TextImage("SUBMIT :)", 30, FontStyle.ITALIC, Color.BLACK),
            new RectangleImage(150, 50, OutlineMode.SOLID, new Color(193, 92, 247))),
        430, 640);
    // checks to see if the initial game board is drawn properly
    t.checkExpect(w.makeScene(), scene);

    // checks to see if the win scene is drawn correctly

    w.theChosenGame.tiles.clear();
    SingleGame empty = new SingleGame(new ArrayList<Tile>(), this.answersListGame1, true);
    ConnectionsWorld p = new ConnectionsWorld(this.testGame1);
    WorldImage winImg = new FromFileImage("Images/vincentog.jpeg");
    WorldScene winScene = new WorldScene(900, 700);
    scene.placeImageXY(startImg, 280, 350);
    scene.placeImageXY(new TextImage("YOU WON! :)", 30, FontStyle.ITALIC, Color.BLACK), 600, 300);
    scene.placeImageXY(
        new TextImage("Press r to reset the game", 20, FontStyle.ITALIC, Color.BLACK), 600, 400);
    scene.placeImageXY(winImg, 250, 350);
    t.checkExpect(w.makeScene(), scene);

    // checks to see if the lose scene is drawn correctly
    initData();
    ConnectionsWorld w1 = new ConnectionsWorld(this.testGame1);
    WorldImage img = new FromFileImage("Images/abhi.jpg");
    WorldScene loseScene = new WorldScene(900, 700);
    loseScene.placeImageXY(startImg, 280, 350);
    loseScene.placeImageXY(new TextImage("CONNECTIONS", 30, FontStyle.ITALIC, Color.BLACK), 430,
        40);
    loseScene.placeImageXY(new TextImage("Lives: " + 4, 20, FontStyle.ITALIC, Color.BLACK), 430,
        95);
    loseScene.placeImageXY(new StarImage(16, OutlineMode.SOLID, Color.RED), 370, 95);
    loseScene.placeImageXY(this.testGame1.drawTiles(0), (120), 190);
    loseScene.placeImageXY(this.testGame1.drawTiles(1), (320), 190);
    loseScene.placeImageXY(this.testGame1.drawTiles(2), (520), 190);
    loseScene.placeImageXY(this.testGame1.drawTiles(3), (720), 190);
    loseScene.placeImageXY(this.testGame1.drawTiles(4), (120), 290);
    loseScene.placeImageXY(this.testGame1.drawTiles(5), (320), 290);
    loseScene.placeImageXY(this.testGame1.drawTiles(6), (520), 290);
    loseScene.placeImageXY(this.testGame1.drawTiles(7), (720), 290);
    loseScene.placeImageXY(this.testGame1.drawTiles(8), (120), 390);
    loseScene.placeImageXY(this.testGame1.drawTiles(9), (320), 390);
    loseScene.placeImageXY(this.testGame1.drawTiles(10), (520), 390);
    loseScene.placeImageXY(this.testGame1.drawTiles(11), (720), 390);
    loseScene.placeImageXY(this.testGame1.drawTiles(12), (120), 490);
    loseScene.placeImageXY(this.testGame1.drawTiles(13), (320), 490);
    loseScene.placeImageXY(this.testGame1.drawTiles(14), (520), 490);
    loseScene.placeImageXY(this.testGame1.drawTiles(15), (720), 490);
    loseScene.placeImageXY(new OverlayImage(new TextImage(
        "Press d to deselect all tiles : Press s to shuffle the board : Press r to reset the game",
        20, FontStyle.ITALIC, Color.BLACK),
        new RectangleImage(850, 50, OutlineMode.SOLID, new Color(225, 225, 225))), 430, 580);
    loseScene.placeImageXY(
        new OverlayImage(new TextImage("SUBMIT :)", 30, FontStyle.ITALIC, Color.BLACK),
            new RectangleImage(150, 50, OutlineMode.SOLID, new Color(193, 92, 247))),
        430, 640);
    loseScene.placeImageXY(startImg, 280, 350);
    loseScene.placeImageXY(new TextImage("YOU LOST! :(", 30, FontStyle.ITALIC, Color.BLACK), 600,
        300);
    loseScene.placeImageXY(
        new TextImage("Press r to reset the game", 20, FontStyle.ITALIC, Color.BLACK), 600, 400);
    loseScene.placeImageXY(img, 250, 350);
    w.lives = 0;
    t.checkExpect(w.makeScene(), loseScene);

  }

  // tests the drawTile method
  void testDrawTile(Tester t) {
    initData();
    // tests the drawTile method on a tile
    t.checkExpect(this.ache.drawTile(),
        new OverlayImage(new TextImage("ACHE", 20, FontStyle.BOLD, Color.BLACK),
            new RectangleImage(180, 90, OutlineMode.SOLID, new Color(203, 213, 224))));
  }

  // tests the drawTiles method
  void testDrawTiles(Tester t) {
    initData();
    SingleGame testGame = new SingleGame(this.tileList, this.answersListGame5, true);

    // tests if the drawTiles method draws the tile of the given index properly
    t.checkExpect(testGame.drawTiles(0),
        new OverlayImage(new TextImage("ACHE", 20, FontStyle.BOLD, Color.BLACK),
            new RectangleImage(180, 90, OutlineMode.SOLID, new Color(203, 213, 224))));

  }

  // tests the getTile method
  void testGetTile(Tester t) {
    this.initData();
    // gets the first tile from this game's tile list
    // gets the second tile from this game's tile list
    // gets the third tile from this game's tile list
    // tests an invalid index and throws an exception
    // tests an invalid index and throws an exception
    t.checkExpect(testGame.getTile(0), new Tile("THREAD"));
    t.checkExpect(testGame.getTile(1), new Tile("STRING"));
    t.checkExpect(testGame.getTile(2), new Tile("FIBER"));
    t.checkException(new IndexOutOfBoundsException(("The index is out of bounds!")), testGame,
        "getTile", -1);
    t.checkException(new IndexOutOfBoundsException(("The index is out of bounds!")), testGame,
        "getTile", 60);
  }

  // tests the selectedNum method
  void testSelectedNum(Tester t) {
    this.initData();
    // counts the number of selected tiles in a game with all unselected tiles
    // counts the number of selected tiles in a game with one selected tile
    // counts the number of selected tiles in a game with more than one selected
    // tile
    t.checkExpect(testGame.selectedNum(), 0);
    testGame.tiles.get(0).selected = true;
    t.checkExpect(testGame.selectedNum(), 1);
    testGame.tiles.get(1).selected = true;
    testGame.tiles.get(2).selected = true;
    testGame.tiles.get(3).selected = true;
    t.checkExpect(testGame.selectedNum(), 4);
  }

  // tests the correctedNum method
  void testCorrectedNum(Tester t) {
    initData();
    // counts the number of not correct tiles in a game with all not corrected tiles
    // counts the number of not correct tiles in a game with one corrected tile
    // counts the number of not correct tiles in a game with more than one corrected
    // tile
    t.checkExpect(testGame.correctedNum(), 4);
    testGame.tiles.get(0).corrected = true;
    t.checkExpect(testGame.correctedNum(), 3);
    testGame.tiles.get(1).corrected = true;
    testGame.tiles.get(2).corrected = true;
    testGame.tiles.get(3).corrected = true;
    t.checkExpect(testGame.correctedNum(), 0);

  }

  // tests the uncorrect method
  void testUncorrect(Tester t) {
    initData();
    // tests if an uncorrected/unselected list remains the same after method is
    // called
    // tests if a fully corrected/selected list sets to all uncorrected and
    // unselected after method
    // tests if a partially corrected/selected list sets to all uncorrected and
    // unselected after method
    ArrayList<Tile> testTile = testGame.tiles;
    testGame.uncorrect();
    t.checkExpect(testGame.tiles, testTile);
    testGame.tiles.get(0).corrected = true;
    testGame.tiles.get(1).corrected = true;
    testGame.tiles.get(2).corrected = true;
    testGame.tiles.get(3).corrected = true;
    testGame.tiles.get(0).selected = true;
    testGame.tiles.get(1).selected = true;
    testGame.tiles.get(2).selected = true;
    testGame.tiles.get(3).selected = true;
    testGame.uncorrect();
    t.checkExpect(testGame.tiles, testTile);
    initData();
    testGame.tiles.get(0).corrected = true;
    testGame.tiles.get(1).selected = true;
    testGame.tiles.get(0).selected = true;
    testGame.tiles.get(1).corrected = true;
    testGame.uncorrect();
    t.checkExpect(testGame.tiles, testTile);

  }

  // tests the undraw method
  void testUndraw(Tester t) {
    initData();
    // tests if an answer list with all non-drawn answers remains the same after
    // method call
    // tests if an answer list with all drawn answers gets set to a list with all
    // non-drawn answers
    // tests if an answer list with some drawn answers gets set to a list with all
    // non-drawn answers
    ArrayList<Answer> testAnswer = testGame.answers;
    testGame.undraw();
    t.checkExpect(testGame.answers, testAnswer);
    testGame.answers.get(0).shouldDraw = true;
    testGame.answers.get(1).shouldDraw = true;
    testGame.answers.get(2).shouldDraw = true;
    testGame.answers.get(3).shouldDraw = true;
    testGame.undraw();
    t.checkExpect(testGame.answers, testAnswer);
    initData();
    testGame.answers.get(0).shouldDraw = true;
    testGame.answers.get(1).shouldDraw = true;
    testGame.undraw();
    t.checkExpect(testGame.answers, testAnswer);

  }

  // tests the unCorrectedList method
  void testUnCorrectedList(Tester t) {
    initData();
    // returns the entire list of uncorrected tiles from a game with all uncorrected
    // tiles
    // returns a modified list of uncorrected tiles from a game with some corrected
    // tiles
    // returns a modified list of uncorrected tiles from a game with all corrected
    // tiles
    t.checkExpect(testGame.tiles, testGame.unCorrectedList());
    testGame.tiles.get(0).corrected = true;
    testGame.tiles.get(1).corrected = true;
    ArrayList<Tile> testTileList = new ArrayList<Tile>();
    testTileList.add(testGame.tiles.get(2));
    testTileList.add(testGame.tiles.get(3));
    t.checkExpect(testGame.unCorrectedList(), testTileList);
    testGame.tiles.get(2).corrected = true;
    testGame.tiles.get(3).corrected = true;
    t.checkExpect(testGame.unCorrectedList(), new ArrayList<Tile>());

  }

  // tests the answerNum method
  void testAnswerNum(Tester t) {
    initData();
    // tests if an arraylist of stings that should match an answer group results in
    // 1
    // tests if an arraylist of strings that should not match an answer group
    // results in 0
    // tests if an arraylist of different strings that should not match an answer
    // group results in 0
    ArrayList<String> matchingStrings = new ArrayList<String>();
    matchingStrings.add("THREAD");
    matchingStrings.add("FIBER");
    matchingStrings.add("STRAND");
    matchingStrings.add("STRING");
    ArrayList<String> nonMatchingStrings1 = new ArrayList<String>();
    nonMatchingStrings1.add("THREAD");
    nonMatchingStrings1.add("ARTHUR");
    nonMatchingStrings1.add("FIBER");
    nonMatchingStrings1.add("ABHI");
    ArrayList<String> nonMatchingStrings2 = new ArrayList<String>();
    nonMatchingStrings2.add("ANOUSHKA");
    nonMatchingStrings2.add("ARTHUR");
    nonMatchingStrings2.add("VANCE");
    nonMatchingStrings2.add("ABHI");
    t.checkExpect(testGame.answerNum(matchingStrings), 1);
    t.checkExpect(testGame.answerNum(nonMatchingStrings1), 0);
    t.checkExpect(testGame.answerNum(nonMatchingStrings2), 0);

  }

  // tests the matchesGroup method
  void testMatchesGroup(Tester t) {
    initData();
    // tests if this answer group matches the given tiles when it should
    // tests if this answer group matches the given tiles when it should not
    // tests if this answer group matches the given tiles when it should not pt. 2
    t.checkExpect(this.game1Answer1.matchesGroup(this.filament), true);
    t.checkExpect(this.game1Answer2.matchesGroup(this.filament), false);
    t.checkExpect(this.game1Answer2.matchesGroup(this.tools), false);

  }

  // tests the wordGroupDisplay method
  void testWordGroupDisplay(Tester t) {
    initData();
    // tests to see if the method writes the items in an answer in one string
    // tests to see if the method writes the items in another answer in one string
    // tests to see if the method writes the items in another answer in one string
    t.checkExpect(this.game1Answer1.wordGroupDisplay(), " THREAD STRING FIBER STRAND");
    t.checkExpect(this.game1Answer3.wordGroupDisplay(), " JACKSON MAROON MC BEN FOLDS");
    t.checkExpect(this.game1Answer2.wordGroupDisplay(), " MONSTER DUMP GARBAGE PICKUP");

  }

  // test on mouse click
  void testOnMouse(Tester t) {
    initData();
    // tests the submit button if nothing is correct
    ConnectionsWorld submit = this.w;
    this.w.onMouseClicked(new Posn(400, 400));
    t.checkExpect(this.w, submit);
    // tests the submit button if four selected tiles are correct
    // (check if answer group is drawn)
    this.w.theChosenGame.tiles.get(0).selected = true;
    this.w.theChosenGame.tiles.get(1).selected = true;
    this.w.theChosenGame.tiles.get(2).selected = true;
    this.w.theChosenGame.tiles.get(3).selected = true;
    this.w.onMouseClicked(new Posn(400, 400));
    ArrayList<Answer> answer = new ArrayList<Answer>();
    answer.add(game1Answer1);
    answer.add(game1Answer2);
    answer.add(game1Answer3);
    answer.add(game1Answer4);
    answer.get(0).shouldDraw = true;
    t.checkExpect(this.w.theChosenGame.answers, answer);

    // tests the submit button if four selected tiles are correct
    // (check if tile list is made correctly (correct "corrected" values are
    // toggled))
    ArrayList<Tile> tile = new ArrayList<Tile>();
    tile.add(this.thread);
    tile.add(this.string);
    tile.add(this.fiber);
    tile.add(this.strand);
    tile.add(this.monster);
    tile.add(this.dump);
    tile.add(this.garbage);
    tile.add(this.pickup);
    tile.add(this.jackson);
    tile.add(this.maroon);
    tile.add(this.mc);
    tile.add(this.benFolds);
    tile.add(this.abandon);
    tile.add(this.freedom);
    tile.add(this.spontaneity);
    tile.add(this.unrestraint);
    tile.get(0).corrected = true;
    tile.get(1).corrected = true;
    tile.get(2).corrected = true;
    tile.get(3).corrected = true;
    t.checkExpect(tile, this.newWorld.theChosenGame.tiles);

    initData();
    // checks if the correct tile is marked as selected when clicked
    this.w.onMouseClicked(new Posn(120, 150));
    t.checkExpect(this.w.theChosenGame.tiles.get(0).selected, true);

    // checks if the correct tile is marked as unselected when clicked (again)
    this.w.onMouseClicked(new Posn(120, 150));
    t.checkExpect(this.w.theChosenGame.tiles.get(0).selected, false);
  }

  // test on key event click
  void testOnKey(Tester t) {
    initData();
    // tests the method when the key "d" is clicked when none of the tiles
    // are selected
    ArrayList<Tile> tile = new ArrayList<Tile>();
    tile.add(this.thread);
    tile.add(this.string);
    tile.add(this.fiber);
    tile.add(this.strand);
    tile.add(this.monster);
    tile.add(this.dump);
    tile.add(this.garbage);
    tile.add(this.pickup);
    tile.add(this.jackson);
    tile.add(this.maroon);
    tile.add(this.mc);
    tile.add(this.benFolds);
    tile.add(this.abandon);
    tile.add(this.freedom);
    tile.add(this.spontaneity);
    tile.add(this.unrestraint);
    this.w.onKeyEvent("d");
    t.checkExpect(this.newWorld.theChosenGame.tiles, tile);
    // tests the method when the key "d" is clicked when some of the tiles
    // are selected (4 tiles)
    this.newWorld.theChosenGame.tiles.get(0).selected = true;
    this.newWorld.theChosenGame.tiles.get(13).selected = true;
    this.newWorld.theChosenGame.tiles.get(5).selected = true;
    this.newWorld.theChosenGame.tiles.get(11).selected = true;
    this.w.onKeyEvent("d");
    t.checkExpect(this.newWorld.theChosenGame.tiles, tile);

    // tests the method when the key "r" is clicked when the game has not been
    // altered
    ConnectionsWorld nothing = new ConnectionsWorld(this.testGame1);
    this.w.onKeyEvent("r");
    t.checkExpect(this.newWorld, nothing);

    // tests the method when the key is "r" and four correct answers
    // had been selected and entered - should return the original world
    this.newWorld.theChosenGame.tiles.get(0).selected = true;
    this.newWorld.theChosenGame.tiles.get(13).selected = true;
    this.newWorld.theChosenGame.tiles.get(5).selected = true;
    this.newWorld.theChosenGame.tiles.get(11).selected = true;
    this.newWorld.theChosenGame.tiles.get(0).corrected = true;
    this.newWorld.theChosenGame.tiles.get(13).corrected = true;
    this.newWorld.theChosenGame.tiles.get(5).corrected = true;
    this.newWorld.theChosenGame.tiles.get(11).corrected = true;
    this.w.onKeyEvent("r");
    t.checkExpect(this.newWorld, nothing);

  }

}
