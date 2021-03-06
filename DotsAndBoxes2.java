/*
  Java 1.1 AWT Applet - Dots and Boxes Game (Lines and Boxes)
  Written by: Keith Fenske, http://www.psc-consulting.ca/fenske/
  Saturday, 17 January 2004
  Java class name: DotsAndBoxes2
  Copyright (c) 2004 by Keith Fenske.  Released under GNU Public License.

  This is a graphical Java 1.1 AWT (GUI) applet to play the pencil-and-paper
  game of "Dots and Boxes", also known as "Lines and Boxes".  The game board is
  a rectangular grid.  Dots are shown for the corners of the board squares.
  Players take turns drawing the lines that surround each square: top, left,
  right, and bottom.  The player who completes a square by clicking on the
  fourth line gets that square.  At the end of the game, the player with the
  most squares wins.  You may run this program as a stand-alone application, or
  as an applet on the following web page:

      Dots and Boxes, Lines and Boxes - by: Keith Fenske
      http://www.psc-consulting.ca/fenske/dotbox2a.htm

  Your squares are shown in black and the computer's squares are shown in
  white.  You may change the number of rows and columns.  The difficulty level
  may be selected as "easy" (computer moves randomly) or "medium" (computer
  tries to complete squares with three lines, and avoids squares with two
  lines).  More difficult levels have not been implemented, although the
  program does contain hooks for two additional levels called "hard" and
  "expert".  Some versions of "Dots and Boxes" force players to take particular
  moves, such as completing a board square if three lines are already present.
  This version has no such restrictions.  As a result, not completing a board
  square can become a useful strategy towards the end of the game!

  GNU General Public License (GPL)
  --------------------------------
  DotsAndBoxes2 is free software: you can redistribute it and/or modify it
  under the terms of the GNU General Public License as published by the Free
  Software Foundation, either version 3 of the License or (at your option) any
  later version.  This program is distributed in the hope that it will be
  useful, but WITHOUT ANY WARRANTY, without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
  Public License for more details.

  You should have received a copy of the GNU General Public License along with
  this program.  If not, see the http://www.gnu.org/licenses/ web page.

  -----------------------------------------------------------------------------

  Programming Notes:

  Since every line is shared by two squares (except for lines on the outside
  edges of the board), assign all horizontal lines to the top edge of a square
  and all vertical lines to the left edge of a square.  This makes the program
  data more consistent.

  There is one more column of vertical lines than there are columns of board
  squares, so add an invisible column of board squares on the right side to
  hold the extra "left" lines.  Similarly, add an invisible row of board
  squares on the bottom to hold the extra "top" lines.

  Mouse clicks on the board lines are made easier by having large corner dots
  (with a diameter much bigger than the width of the lines) and using the dot
  size as an detection width for clicks on the lines.  As an added convenience,
  once a board square has three complete lines, the user can click directly on
  the board square to complete it.

  This game doesn't have a "Skip Turn" button because once all board squares
  have at least two lines, the player who moves next generally loses....

  -----------------------------------------------------------------------------

  Java Applet Notes:

  The recommended way of writing applets is to use Java Swing, according to Sun
  Microsystems, the creators and sponsors of Java.  Unfortunately, most web
  browsers don't support Swing unless you download a recent copy of the Java
  run-time environment from Sun.  This leaves a Java programmer with two
  choices:

  (1) Write applets using only old features found in the AWT interface.  The
      advantage, if you can see it this way, is that the programmer gets a
      detailed opportunity to interact with the graphical interface.  (Joke.)

  (2) Force users to visit http://java.sun.com/downloads/ to download and
      install a newer version of Java.  However, forcing anyone to download
      something before they can visit your web page is a poor idea.

  A worse idea is new browsers that don't have any Java support at all, unless
  the user first downloads Sun Java.  Microsoft stopped distributing their
  version of Java in 2003 starting with Windows XP SP1a (February), then
  Windows 2000 SP4 (June).  Until Microsoft and Sun resolve their various
  lawsuits -- or until Microsoft agrees to distribute an unaltered version of
  Sun Java -- there will be an increasing number of internet users that have
  *no* version of Java installed on their machines!

  The design considerations for this applet are as follows:

  (1) The applet should run on older browsers as-is, without needing any
      additional downloads and/or features.  The minimum target is JDK1.1 which
      is Microsoft Internet Explorer 5.0 (Windows 98) and Netscape 4.7/4.8 (JDK
      1.1.5 from 1997).

  (2) Unlike the previous Life3 and TicTacToe4 applets, this applet uses more
      than one class.  A second class, a subclass of Canvas, is used to better
      draw and accept mouse input on the game board.  To run this applet on a
      web page, DotsAndBoxes2 should be loaded from a JAR (Java archive) file.

  (3) The default background in the Sun Java applet viewer is white, but most
      web browsers use light grey.  To get the background color that you want,
      you must setBackground() on components or fillRect() with the color of
      your choice.

  (4) A small main() method is included with a WindowAdapter subclass, so that
      this program can be run as an application.  The default window size and
      position won't please everyone.
*/

import java.applet.*;             // older Java applet support
import java.awt.*;                // older Java GUI support
import java.awt.event.*;          // older Java GUI event support

public class DotsAndBoxes2
             extends Applet
             implements ActionListener, ItemListener, Runnable
{
  /* constants */

  static final String beginMessage = "Click the mouse on a line of your choice.  You are the black squares.";
  static final int canvasBorder = 10; // empty pixels around game board
  static final int DefCOLS = 6;   // default number of columns in game board
  static final int DefROWS = 3;   // default number of rows in game board
  static final String noMessage = " "; // message text when nothing to say
  static final String[] rowColumnCounters = {"2", "3", "4", "5", "6", "8",
    "12", "18"};

  static final Color BACKGROUND = new Color(255, 204, 204); // light pink
  static final Color ColorCOMPUTER = new Color(204, 255, 255); // light cyan
  static final Color ColorDOT = new Color(204, 153, 153); // darker pink
  static final Color ColorEMPTY = BACKGROUND;
  static final Color ColorHOVER = new Color(102, 102, 255); // light blue
  static final Color ColorLINE = ColorDOT;
  static final Color ColorUSER = new Color(51, 51, 51); // dark grey

  static final int GameCHECKCOMP = 101; // checking computer's move
  static final int GameCHECKUSER = 102; // checking user's move
  static final int GameFINISH = 103;  // game is finished (no moves allowed)
  static final int GameWAITCOMP = 104; // waiting for computer to move
  static final int GameWAITUSER = 105; // waiting for user to move

  static final int INVALID = -1;      // general flag for illegal value

  static final int LevelEASY = 201;   // computer moves randomly
  static final int LevelMEDIUM = 202; // computer finishes squares, avoids twos
  static final int LevelHARD = 203;   // an actual strategy - not implemented
  static final int LevelEXPERT = 204; // for tournament play - not implemented
  static final String LevelStringEASY = "Easy";
  static final String LevelStringMEDIUM = "Medium";
  static final String LevelStringHARD = "Hard";
  static final String LevelStringEXPERT = "Expert";

  static final int LineEMPTY = 301;   // no line
  static final int LineFULL = 302;    // line has been drawn
  static final int LineHOVER = 303;   // mouse is over empty line

  static final int SquareCOMPUTER = 401; // computer occupies this position
  static final int SquareEMPTY = 402; // board position is empty
  static final int SquareHOVER = 403; // mouse hover on empty/valid user move
  static final int SquareUSER = 404;  // user occupies this board position

  static final int TypeHZLINE = 501;  // user clicked on a horizontal line
  static final int TypeSQUARE = 502;  // user clicked on a board square
  static final int TypeVTLINE = 503;  // user clicked on a vertical line

  /* class variables */

  /* instance variables, including shared GUI components */

  Canvas boardCanvas;             // where we draw the game board
  int boardDotSize;               // diameter of board dots (in pixels), as set
                                  // ... by most recent boardUpdate()
  int boardGridStep;              // calculated size of each board position, as
                                  // ... set by most recent boardUpdate(),
                                  // ... including one set of dots and lines
  int[][] boardLeft;              // left (vertical) lines (LineXXX)
  int boardLeftMargin;            // adjusted left margin to center game board,
                                  // ... as set by most recent boardUpdate()
  int[][] boardLeftOld;           // previous values in <boardLeft>
  int boardLineWidth;             // width of board lines (in pixels), as set
                                  // ... by most recent boardUpdate()
  int[][] boardSquare;            // center portion of board squares (PlayXXX)
  int[][] boardSquareOld;         // previous values in <boardSquare>
  int boardSymbolSize;            // pixels for each position's symbol, as set
                                  // ... by most recent boardUpdate()
  int[][] boardTop;               // top (horizontal) lines (LineXXX)
  int boardTopMargin;             // adjusted top margin to center game board,
                                  // ... as set by most recent boardUpdate()
  int[][] boardTopOld;            // previous values in <boardTop>
  Thread clockThread;             // clock thread for delaying computer's move
  Choice columnCounter;           // column counter (number of columns)
  boolean eraseFlag;              // true if background should be erased on
                                  // ... next call to boardUpdate()
  int gameState;                  // state variable for current game
  int hoverCol;                   // mouse hover column number
  int hoverRow;                   // mouse hover row number
  int hoverType;                  // mouse hover is TypeXXX
  Choice levelChoice;             // how user selects difficulty level
  int levelFlag;                  // how difficult the computer plays
  Label messageText;              // information or status message for user
  int mouseCol;                   // mouse click column number
  int mouseRow;                   // mouse click row number
  int mouseType;                  // mouse click is TypeXXX
  int numCols = DefCOLS;          // number of columns in current game board
  int numRows = DefROWS;          // number of rows in current game board
  Choice rowCounter;              // row counter (number of rows)
  Button startButton;             // "New Game" button


/*
  init() method

  Initialize this applet (equivalent to the main() method in an application).
  Please note the following about writing applets:

  (1) An Applet is an AWT Component just like a Button, Frame, or Panel.  It
      has a width, a height, and you can draw on it (given a proper graphical
      context, as in the paint() method).

  (2) Applets shouldn't attempt to exit, such as by calling the System.exit()
      method, because this isn't allowed on a web page.
*/
  public void init()
  {
    /* Intialize our own data before creating the GUI interface. */

    boardDotSize = 0;             // in case mouse moves before board paints,
    boardGridStep = 1;            // ... these values will invalidate any
    boardLeftMargin = 0;          // ... conversion of mouse coordinates to
    boardLineWidth = 0;           // ... board positions (row, column)
    boardSymbolSize = 0;
    boardTopMargin = 0;

    clearBoard();                 // clear (create) the game board

    /* Create the GUI interface as a series of little panels inside bigger
    panels.  The intermediate panel names (panel1, panel2, etc) are of no
    importance and hence are only numbered. */

    /* Make a horizontal panel to hold the difficulty level, row counter,
    column counter, and "New Game" button. */

    levelChoice = new Choice();
    levelChoice.add(LevelStringEASY);
    levelChoice.add(LevelStringMEDIUM);
//  levelChoice.add(LevelStringHARD); // not implemented
//  levelChoice.add(LevelStringEXPERT); // not implemented
    levelChoice.select(LevelStringMEDIUM); // select must be same as flag
    levelFlag = LevelMEDIUM;      // flag must be same as choice select
    levelChoice.addItemListener((ItemListener) this);

    Panel panel1 = new Panel(new FlowLayout(FlowLayout.CENTER, 0, 0));
    Label label1 = new Label("Rows: ", Label.RIGHT);
    label1.setBackground(BACKGROUND);
    panel1.add(label1);
    rowCounter = new Choice();
    for (int i = 0; i < rowColumnCounters.length; i ++)
      rowCounter.add(rowColumnCounters[i]);
    rowCounter.select(String.valueOf(DefROWS));
    rowCounter.addItemListener((ItemListener) this);
    panel1.add(rowCounter);

    Panel panel2 = new Panel(new FlowLayout(FlowLayout.CENTER, 0, 0));
    Label label2 = new Label("Columns: ", Label.RIGHT);
    label2.setBackground(BACKGROUND);
    panel2.add(label2);
    columnCounter = new Choice();
    for (int i = 0; i < rowColumnCounters.length; i ++)
      columnCounter.add(rowColumnCounters[i]);
    columnCounter.select(String.valueOf(DefCOLS));
    columnCounter.addItemListener((ItemListener) this);
    panel2.add(columnCounter);

    Label label3 = new Label(" "); // a cheap separator
    label3.setBackground(BACKGROUND);

    startButton = new Button("New Game");
    startButton.addActionListener((ActionListener) this);

    Panel panel3 = new Panel(new FlowLayout(FlowLayout.CENTER, 10, 5));
    panel3.add(levelChoice);
    panel3.add(panel1);
    panel3.add(panel2);
    panel3.add(label3);
    panel3.add(startButton);

    /* Put the message field under the counters/buttons. */

    Panel panel4 = new Panel(new GridLayout(2, 1, 0, 5));
    panel4.add(panel3);
//  messageText = new Label(beginMessage, Label.CENTER);
    messageText = new Label("Dots and Boxes (Java applet).  Copyright (c) 2004 by Keith Fenske.  GNU Public License.", Label.CENTER);
    // JDK1.1 note: replace Font(null,...) with Font("Default",...)
    messageText.setFont(new Font("Default", Font.PLAIN, 14));
    messageText.setBackground(BACKGROUND);
    panel4.add(messageText);
    panel4.setBackground(BACKGROUND); // for Netscape 4.7/4.8 (JDK1.1)

    /* Put the buttons and message field on top of a canvas for the game board,
    giving the game board the remaining window space.  We set the applet to
    have a BorderLayout and put <boardCanvas> in the center, which allows the
    canvas to expand and contract with the applet's window size.  Note that the
    DotsAndBoxes2Board class assumes that a DotsAndBoxes2 object is the parent
    container of <boardCanvas>. */

    this.setLayout(new BorderLayout(5, 5));
    this.add(panel4, BorderLayout.NORTH);
    boardCanvas = new DotsAndBoxes2Board();
    boardCanvas.addMouseListener((MouseListener) boardCanvas);
    boardCanvas.addMouseMotionListener((MouseMotionListener) boardCanvas);
    this.add(boardCanvas, BorderLayout.CENTER);
    this.setBackground(BACKGROUND);
    this.validate();              // do the window layout

    /* Now let the GUI interface run the game. */

  } // end of init() method


/*
  main() method

  Applets only need an init() method to start execution.  This main() method is
  a wrapper that allows the same applet code to run as an application.
*/
  public static void main(String[] args)
  {
    Applet appletPanel;           // the target applet's window
    Frame mainFrame;              // this application's window

    mainFrame = new Frame(
      "Dots and Boxes, Lines and Boxes - by: Keith Fenske");
    mainFrame.addWindowListener(new DotsAndBoxes2Window());
    mainFrame.setLayout(new BorderLayout(5, 5));
    mainFrame.setLocation(new Point(50, 50)); // top-left corner of app window
    mainFrame.setSize(700, 500);  // initial size of application window
    appletPanel = new DotsAndBoxes2(); // create instance of target applet
    mainFrame.add(appletPanel, BorderLayout.CENTER); // give applet full frame
    mainFrame.validate();         // do the application window layout
    appletPanel.init();           // initialize applet
    mainFrame.setVisible(true);   // show the application window

  } // end of main() method

// ------------------------------------------------------------------------- //

/*
  actionPerformed() method

  This method is called when the user clicks on the "New Game" button.
*/
  public void actionPerformed(ActionEvent event)
  {
    Object source = event.getSource(); // where the event came from
    if (source == startButton)
    {
      /* The user clicked the "New Game" button and wants to start over. */

      clearBoard();               // start a new game board
      messageText.setText(beginMessage);
      eraseFlag = true;           // boardUpdate() must redraw game board
      boardCanvas.repaint();      // redraw the game board
    }
    else
    {
      System.out.println(
        "error in actionPerformed(): ActionEvent not recognized: " + event);
    }
  } // end of actionPerformed() method


/*
  boardMouseClicked() method

  This method is called by our dummy Canvas class (DotsAndBoxes2Board) to
  process mouse clicks on the game board, in the context of the main
  DotsAndBoxes2 class.  We must determine:

  (1) if the user is allowed to choose a board position (user's turn to move);
  (2) which position the mouse is pointing at;
  (3) if the position is empty (available); and
  (4) if choosing the position ends the game.

  Our calculations use several global variables set by the boardUpdate()
  method.
*/
  public void boardMouseClicked(MouseEvent event, Canvas canvas)
  {
    boolean changeFlag;           // true if we change game board

    /* Where did the user click? */

    mouseToBoard(event.getX(), event.getY()); // convert (x, y) coordinates
                                  // sets <mouseCol>, <mouseRow>, <mouseType>

    /* Now start checking if this mouse click is a legal move. */

    if (gameState == GameFINISH)
    {
      /* There is no active game, so mouse clicks aren't useful. */

      messageText.setText("This game is finished.  You must start a new game before you can move again.");
    }
    else if (gameState != GameWAITUSER)
    {
      /* It's the computer's turn to move, not the user's. */

      messageText.setText("Sorry, it's not your turn to move.  The computer is thinking.");
    }
    else if (mouseType == INVALID) // if click is outside game board
    {
      /* Ignore clicks that are not directly on a board position. */

      messageText.setText("Please click on a board line or an almost complete square.");
    }
    else
    {
      /* What did the user click on?  On a horizontal or vertical line?  On a
      board square? */

      changeFlag = false;         // assume no changes to game board
      switch (mouseType)
      {
        case TypeHZLINE:          // horizontal line
          if ((boardTop[mouseRow][mouseCol] != LineEMPTY)
            && (boardTop[mouseRow][mouseCol] != LineHOVER))
          {
            messageText.setText("Sorry, that horizontal line has already been chosen.");
          }
          else
          {
            boardTop[mouseRow][mouseCol] = LineFULL;
            changeFlag = true;    // game board has changed
          }
          break;

        case TypeSQUARE:          // board square
          if ((boardSquare[mouseRow][mouseCol] != SquareEMPTY)
            && (boardSquare[mouseRow][mouseCol] != SquareHOVER))
          {
            messageText.setText("Sorry, that board square is already complete.");
          }
          else
          {
            /* This square is empty, but does it have exactly three lines? */

            if (countLines(mouseRow, mouseCol) != 3)
            {
              messageText.setText("You can only click on a board square if it already has three lines.");
            }
            else                  // three lines found, complete the fourth
            {
              if (boardTop[mouseRow][mouseCol] != LineFULL)
                boardTop[mouseRow][mouseCol] = LineFULL;
              else if (boardLeft[mouseRow][mouseCol] != LineFULL)
                boardLeft[mouseRow][mouseCol] = LineFULL;
              else if (boardLeft[mouseRow][mouseCol + 1] != LineFULL)
                boardLeft[mouseRow][mouseCol + 1] = LineFULL;
              else
                boardTop[mouseRow + 1][mouseCol] = LineFULL;
              changeFlag = true;  // game board has changed
            }
          }
          break;

        case TypeVTLINE:
          if ((boardLeft[mouseRow][mouseCol] != LineEMPTY)
            && (boardLeft[mouseRow][mouseCol] != LineHOVER))
          {
            messageText.setText("Sorry, that vertical line has already been chosen.");
          }
          else
          {
            boardLeft[mouseRow][mouseCol] = LineFULL;
            changeFlag = true;    // game board has changed
          }
          break;
      }

      /* Did we change the game board?  If so, is the game over?  Does the user
      get another turn? */

      if (changeFlag)
      {
        messageText.setText(noMessage); // clear any previous message text
        boardCanvas.repaint();    // redraw the game board
        gameState = GameCHECKUSER; // check result of user's move
        clockThread = new Thread(this); // do checking in a separate thread
        clockThread.start();      // start the run() clock thread
      }
    }
  } // end of boardMouseClicked() method


/*
  boardMouseMoved() method

  This method is called by our dummy Canvas class (DotsAndBoxes2Board) to
  process mouse movement across the game board, in the context of the main
  DotsAndBoxes2 class.  If the user pauses over an empty position that is a
  valid move for the user, then we highlight that position in a different
  color.

  Our calculations use several global variables set by the boardUpdate()
  method.
*/
  public void boardMouseMoved(MouseEvent event, Canvas canvas)
  {
    boolean repaintFlag;          // true if board should be repainted

    /* Where is the mouse hovering?  Over a horizontal or vertical line?  On a
    board square? */

    mouseToBoard(event.getX(), event.getY()); // convert (x, y) coordinates
                                  // sets <mouseCol>, <mouseRow>, <mouseType>
    if ((hoverCol == mouseCol)
      && (hoverRow == mouseRow)
      && (hoverType == mouseType))
    {
      /* No change as far as we are concerned.  Do nothing.  This method is
      called often, so we want to exit quickly if there is nothing to do. */
    }
    else
    {
      /* Mouse has moved to a new board position. */

      repaintFlag = false;      // assume board doesn't need to be updated

      /* Clear previous mouse hover, if any. */

      switch (hoverType)
      {
        case TypeHZLINE:
          if (boardTop[hoverRow][hoverCol] == LineHOVER)
          {
            boardTop[hoverRow][hoverCol] = LineEMPTY;
            repaintFlag = true;   // game board should be updated
          }
          break;

        case TypeSQUARE:
          if (boardSquare[hoverRow][hoverCol] == SquareHOVER)
          {
            boardSquare[hoverRow][hoverCol] = SquareEMPTY;
            repaintFlag = true;   // game board should be updated
          }
          break;

        case TypeVTLINE:
          if (boardLeft[hoverRow][hoverCol] == LineHOVER)
          {
            boardLeft[hoverRow][hoverCol] = LineEMPTY;
            repaintFlag = true;   // game board should be updated
          }
          break;
      }

      /* Set new mouse hover, if this is a valid move for the user. */

      if (gameState == GameWAITUSER)  // is it the user's turn?
      {
        /* It's the user's turn to move, but is the mouse over a valid move? */

        switch (mouseType)
        {
          case TypeHZLINE:
            if (boardTop[mouseRow][mouseCol] == LineEMPTY)
            {
              boardTop[mouseRow][mouseCol] = LineHOVER;
              repaintFlag = true; // game board should be updated
            }
            break;

          case TypeSQUARE:
            if (boardSquare[mouseRow][mouseCol] == SquareEMPTY)
            {
              /* This square is empty, but does it have exactly three lines? */

              if (countLines(mouseRow, mouseCol) == 3)
              {
                boardSquare[mouseRow][mouseCol] = SquareHOVER;
                repaintFlag = true; // game board should be updated
              }
            }
            break;

          case TypeVTLINE:
            if (boardLeft[mouseRow][mouseCol] == LineEMPTY)
            {
              boardLeft[mouseRow][mouseCol] = LineHOVER;
              repaintFlag = true; // game board should be updated
            }
            break;
        }
      }

      /* Save current mouse position and type as the new hover position and
      type, so we can exit quickly from this method most of the time. */

      hoverCol = mouseCol;
      hoverRow = mouseRow;
      hoverType = mouseType;

      /* Redraw the game board, if necessary. */

      if (repaintFlag)            // should game board be updated?
        boardCanvas.repaint();    // yes, redraw the game board
    }
  } // end of boardMouseMoved() method


/*
  boardPaint() method

  This method is called by our dummy Canvas class (DotsAndBoxes2Board) to
  redraw the game board, in the context of the main DotsAndBoxes2 class.  There
  is a separation between paint() and update() methods.  Paint() methods are
  called when a window is first created, gets resized, or needs to be redrawn
  after being overwritten.  Update() methods are called when the window exists
  and is valid, but the contents may have changed.

  Simple applets only have a paint() method, which erases the window and
  redraws all components each time.  However, this applet updates a dynamic
  game board.  If the background is erased each time, then the display will
  "flicker" because of a short period of time after the old game board
  disappears before the new game board is drawn.  Since the new game board goes
  in exactly the same place as the old board, erasing the background is not
  necessary.  To avoid flicker, this applet separates the paint() and update()
  methods.
 */
  void boardPaint(
    Graphics gr,                  // graphics context
    Canvas canvas)                // passed reference for <boardCanvas>
  {
    eraseFlag = true;             // boardUpdate() must redraw game board
    boardUpdate(gr, canvas);      // all work is done in boardUpdate() so that
                                  // ... the background is only erased and
                                  // ... redrawn when necessary
  } // end of boardPaint() method


/*
  boardUpdate() method

  This method is called by our dummy Canvas class (DotsAndBoxes2Board) to
  update the game board, in the context of the main DotsAndBoxes2 class.  There
  is a separation between paint() and update() methods.  Paint() methods are
  called when a window is first created, gets resized, or needs to be redrawn
  after being overwritten.  Update() methods are called when the window exists
  and is valid, but the contents may have changed.

  Several global variables are set for later use by the mouse listener to
  determine where board positions are located.

  When an applet runs on a web page, the initial window size is chosen by the
  web page's HTML code and can't be changed by the applet.  Applets running
  outside of a web page (such as with Sun's applet viewer) can change their
  window size at any time.  The user may enlarge or reduce the window to make
  it fit better on his/her display.  Hence, while this applet doesn't attempt
  to change the window size, it must accept that the window size may be
  different each time the paint() method is called.  A good applet redraws its
  components to fit the window size.
*/
  void boardUpdate(
    Graphics gr,                  // graphics context
    Canvas canvas)                // passed reference for <boardCanvas>
  {
    int boardHeight;              // height (in pixels) of actual game board
    int boardWidth;               // width (in pixels) of actual game board
    int col;                      // temporary column number (index)
    int corner;                   // how much we round corners on rectangle
    int hz;                       // temporary number of horizontal pixels
    int lineLength;               // length of lines in pixels
    int lineOffset;               // pixels from dot coordinate to lines
    int row;                      // temporary row number (index)
    int vt;                       // temporary number of vertical pixels
    int[] xlist;                  // x coordinates for polygon shape
    int[] ylist;                  // y coordinates for polygon shape

    if (eraseFlag)                // only if requested
    {
      /* If the current message field is a complaint from us about the applet
      window being too small, then clear the message text.  Should the window
      problem persist, we will regenerate the error message anyway. */

      if (messageText.getText().startsWith("Applet window"))
        messageText.setText(noMessage);

      /* Clear the entire board canvas (including any defined borders) to our
      own background color. */

      gr.setClip(null);           // turn off clipping region, so that
                                  // ... everything we do here gets displayed
      gr.setColor(BACKGROUND);
      // JDK1.1 note: replace canvas.getWidth() with canvas.getSize().width
      // JDK1.1 note: replace canvas.getHeight() with canvas.getSize().height
      gr.fillRect(0, 0, canvas.getSize().width, canvas.getSize().height);

      /* Calculate the size of the game board (in pixels) using the size of
      <boardCanvas> minus a predefined border. */

      // JDK1.1 note: replace canvas.getWidth() with canvas.getSize().width
      boardWidth = canvas.getSize().width - (2 * canvasBorder);
      boardWidth = (boardWidth > 0) ? boardWidth : 0;
                                  // don't allow negative values
      // JDK1.1 note: replace canvas.getHeight() with canvas.getSize().height
      boardHeight = canvas.getSize().height - (2 * canvasBorder);
      boardHeight = (boardHeight > 0) ? boardHeight : 0;
                                  // don't allow negative values

      /* Estimate the size of each board position.  Dots and lines are
      proportional to the size of each board square, with a minimum size in
      pixels.  Board squares must be kept ... square.  The diameter of the dots
      is assumed to be bigger than the width of the lines. */

      hz = boardWidth / numCols;  // first estimate of pixels per column
      vt = boardHeight / numRows; // first estimate of pixels per row
      boardGridStep = (hz < vt) ? hz : vt; // minimum becomes first estimate

      boardDotSize = (int) (boardGridStep * 0.20);
                                  // diameter of dots in pixels
      boardDotSize = (boardDotSize > 3) ? boardDotSize : 3;
                                  // minimum of three pixels

      boardLineWidth = (int) (boardGridStep * 0.08);
                                  // width of lines in pixels
      boardLineWidth = (boardLineWidth > 2) ? boardLineWidth : 2;
                                  // minimum of two pixels

      hz = (boardWidth - boardDotSize) / numCols; // second column estimate
      vt = (boardHeight - boardDotSize) / numRows; // second row estimate
      boardGridStep = (hz < vt) ? hz : vt; // second estimate per position

      boardSymbolSize = boardGridStep - boardDotSize;
                                  // pixels for each position's player symbol
      boardSymbolSize = (boardSymbolSize > 10) ? boardSymbolSize : 10;
                                  // minimum of ten pixels
      boardGridStep = boardSymbolSize + boardDotSize;
                                  // final step size will be positive

      /* Compute a new left margin and top margin so that our game board will
      be centered on the canvas. */

      hz = (boardWidth - boardDotSize - (numCols * boardGridStep)) / 2;
      if (hz < 0)
      {
        messageText.setText("Applet window is too narrow to display " + numCols
          + " columns.");
        hz = 0;                   // reset and continue
      }
      boardLeftMargin = canvasBorder + hz; // plus defined left border

      vt = (boardHeight - boardDotSize - (numRows * boardGridStep)) / 2;
      if (vt < 0)
      {
        messageText.setText("Applet window is too short to display " + numRows
          + " rows.");
        vt = 0;                   // reset and continue
      }
      boardTopMargin = canvasBorder + vt; // plus defined top border

      /* Draw the dots, the only unchanging part of this game board.  The
      "dots" are actually drawn as 8-sided polygons (octagons) so that the
      lines will join up exactly on each edge. */

      gr.setColor(ColorDOT);
      lineOffset = (boardDotSize - boardLineWidth) / 2;
                                  // pixel offset from dots to lines
      vt = boardTopMargin;        // y coordinate of first dot in first row
      xlist = new int[8];         // eight points needed to describe octagon
      ylist = new int[8];
      for (row = 0; row <= numRows; row ++)
      {
        hz = boardLeftMargin;     // x coordinate of first dot in first column
        for (col = 0; col <= numCols; col ++)
        {
          xlist[0] = hz + lineOffset; // first x coordinate, clockwise
          xlist[1] = xlist[0] + boardLineWidth;
          xlist[2] = hz + boardDotSize;
          xlist[3] = xlist[2];
          xlist[4] = xlist[1];
          xlist[5] = xlist[0];
          xlist[6] = hz;
          xlist[7] = xlist[6];

          ylist[0] = vt;          // first y coordinate, clockwise
          ylist[1] = ylist[0];
          ylist[2] = vt + lineOffset;
          ylist[3] = ylist[2] + boardLineWidth;
          ylist[4] = vt + boardDotSize;
          ylist[5] = ylist[4];
          ylist[6] = ylist[3];
          ylist[7] = ylist[2];

          gr.fillPolygon(xlist, ylist, 8); // draw dot as a filled polygon

          hz += boardGridStep;    // x coordinate for next column
        }
       vt += boardGridStep;       // y coordinate for next row
      }

    } // end if eraseFlag

    /* Draw the horizontal lines (if any).  Don't draw lines that haven't
    changed. */

    lineLength = boardGridStep - boardDotSize; // length of lines in pixels
    lineOffset = (boardDotSize - boardLineWidth) / 2;
                                  // pixel offset from dots to lines
    vt = boardTopMargin + lineOffset;
                                  // y coordinate of first line above first row
    for (row = 0; row <= numRows; row ++)
    {
      hz = boardLeftMargin + boardDotSize;
                                  // x coordinate of first line in first column
      for (col = 0; col < numCols; col ++)
      {
        /* Display this line if (1) the background has been erased, or (2) if
        the line differs from last time we drew the line. */

        if (eraseFlag || (boardTop[row][col] != boardTopOld[row][col]))
        {
          switch (boardTop[row][col])
          {
            case LineEMPTY:        // no line
              gr.setColor(ColorEMPTY);
              break;

            case LineFULL:        // line has been drawn
              gr.setColor(ColorLINE);
              break;

            case LineHOVER:       // mouse is over empty line
              gr.setColor(ColorHOVER);
              break;

            default:
              System.out.println("error in boardUpdate(): bad boardTop["
                + row + "][" + col + "] = " + boardTop[row][col]);
          }
          gr.fillRect(hz, vt, lineLength, boardLineWidth); // rectangle
        }
        boardTopOld[row][col] = boardTop[row][col]; // save for next time
        hz += boardGridStep;      // x coordinate for next column
      }
      vt += boardGridStep;        // y coordinate for next row
    }

    /* Draw the vertical lines (if any).  Don't draw lines that haven't
    changed. */

    vt = boardTopMargin + boardDotSize;
                                  // y coordinate of first line above first row
    for (row = 0; row < numRows; row ++)
    {
      hz = boardLeftMargin + lineOffset;
                                  // x coordinate of first line in first column
      for (col = 0; col <= numCols; col ++)
      {
        /* Display this line if (1) the background has been erased, or (2) if
        the line differs from last time we drew the line. */

        if (eraseFlag || (boardLeft[row][col] != boardLeftOld[row][col]))
        {
          switch (boardLeft[row][col])
          {
            case LineEMPTY:        // no line
              gr.setColor(ColorEMPTY);
              break;

            case LineFULL:        // line has been drawn
              gr.setColor(ColorLINE);
              break;

            case LineHOVER:       // mouse is over empty line
              gr.setColor(ColorHOVER);
              break;

            default:
              System.out.println("error in boardUpdate(): bad boardLeft["
                + row + "][" + col + "] = " + boardLeft[row][col]);
          }
          gr.fillRect(hz, vt, boardLineWidth, lineLength); // rectangle
        }
        boardLeftOld[row][col] = boardLeft[row][col]; // save for next time
        hz += boardGridStep;      // x coordinate for next column
      }
      vt += boardGridStep;        // y coordinate for next row
    }

    /* Draw the completed board squares (if any).  Don't draw positions that
    haven't changed. */

    corner = (int) (boardSymbolSize * 0.70); // rounded corners on rectangle
    vt = boardTopMargin + boardDotSize;
                                  // y coordinate of first square in first row
    for (row = 0; row < numRows; row ++)
    {
      hz = boardLeftMargin + boardDotSize;
                                  // x coordinate of first square in first column
      for (col = 0; col < numCols; col ++)
      {
        /* Display this game board position if (1) the background has been
        erased, or (2) if the position differs from last time we drew the
        board. */

        if (eraseFlag || (boardSquare[row][col] != boardSquareOld[row][col]))
        {
          switch (boardSquare[row][col])
          {
            case SquareCOMPUTER:  // computer's position
              gr.setColor(ColorCOMPUTER);
              break;

            case SquareEMPTY:     // empty position
              gr.setColor(ColorEMPTY);
              break;

            case SquareHOVER:     // mouse over valid user move
              gr.setColor(ColorHOVER);
              break;

            case SquareUSER:      // user's position
              gr.setColor(ColorUSER);
              break;

            default:
              System.out.println("error in boardUpdate(): bad boardSquare["
                + row + "][" + col + "] = " + boardSquare[row][col]);
          }
          gr.fillRoundRect(hz, vt, boardSymbolSize, boardSymbolSize, corner,
            corner);              // draw position as rounded rectangle
        }
        boardSquareOld[row][col] = boardSquare[row][col]; // save for next time
        hz += boardGridStep;      // x coordinate for next column
      }
      vt += boardGridStep;        // y coordinate for next row
    }

    /* The game board has now been redrawn.  Clear the erase flag, if any. */

    eraseFlag = false;            // erased board has now been redrawn

  } // end of boardUpdate() method


/*
  checkBoard() method

  This method is called after the computer or user makes a move.  It checks if
  the game is finished, if the same player gets another move (after completing
  a square), or if the next move passes to the other player.  We return this
  information in an updated <gameState> variable.
*/
  void checkBoard()
  {
    int col;                      // temporary column number (index)
    int emptyLines;               // total number of empty lines
    int newComp;                  // number of new squares completed by computer
    int newUser;                  // number of new squares completed by user
    int row;                      // temporary row number (index)
    int totalComp;                // total squares completed by computer
    int totalUser;                // total squares completed by user

    /* Check if we were called incorrectly.  This shouldn't happen once the
    program is finished, but may happen during debugging. */

    if ((gameState != GameCHECKCOMP) && (gameState != GameCHECKUSER))
    {
      System.out.println("error in checkBoard(): bad gameState = "
        + gameState);
    }

    /* Check if any empty squares have been completed.  Assign proper credit
    for new squares, and flash them on the display. */

    newComp = newUser = 0;        // assume nobody completed a square
    for (row = 0; row < numRows; row ++)
      for (col = 0; col < numCols; col ++)
      {
        if ((boardSquare[row][col] == SquareEMPTY)
          || (boardSquare[row][col] == SquareHOVER))
        {
          if (countLines(row, col) == 4) // has empty square been completed?
          {
            flashPosition(TypeSQUARE, row, col); // yes, flash display
            if (gameState == GameCHECKCOMP) // assign credit
            {
              boardSquare[row][col] = SquareCOMPUTER;
              newComp ++;         // one more computer square this turn
            }
            else
            {
              boardSquare[row][col] = SquareUSER;
              newUser ++;         // one more user square this turn
            }
          }
        }
      }
    if ((newComp > 0) || (newUser > 0)) // did we change the board?
      boardCanvas.repaint();      // yes, redraw the game board

    /* Count the number of empty lines still available for play.  The game is
    over if nobody can make a move. */

    emptyLines = 0;               // assume nothing
    for (row = 0; row <= numRows; row ++)
      for (col = 0; col < numCols; col ++)
        if (boardTop[row][col] != LineFULL)
          emptyLines ++;          // one more empty horizontal line
    for (row = 0; row < numRows; row ++)
      for (col = 0; col <= numCols; col ++)
        if (boardLeft[row][col] != LineFULL)
          emptyLines ++;          // one more empty vertical line

    if (emptyLines == 0)
    {
      /* No more moves are possible.  Find the winner by counting how many
      squares each player has. */

      totalComp = totalUser = 0;  // assume nobody completed a square
      for (row = 0; row < numRows; row ++)
        for (col = 0; col < numCols; col ++)
        {
          switch (boardSquare[row][col])
          {
            case SquareCOMPUTER:
              totalComp ++;       // one more square for the computer
              break;

            case SquareUSER:
              totalUser ++;       // one more square for the user
              break;

            default:
              System.out.println("error in checkBoard(): bad boardSquare["
                + row + "][" + col + "] = " + boardSquare[row][col]);
              break;
          }
        }

      /* Tell the user the final result. */

      if (totalComp > totalUser)
      {
        messageText.setText("Game over.  Computer wins, " + totalComp
          + " squares to your " + totalUser + " squares.");
      }
      else if (totalComp < totalUser)
      {
        messageText.setText("You win with " + totalUser
          + " squares to the computer's " + totalComp + " squares.");
      }
      else // (totalComp == totalUser)
      {
        messageText.setText("Game is tied.  We both have " + totalComp
          + " squares.");
      }
      gameState = GameFINISH;     // game is over
    }

    /* Game is not over.  Does the computer get another turn? */

    else if (newComp > 0)
    {
      gameState = GameWAITCOMP;   // yes, waiting for computer to move
    }

    /* Does the user get another turn? */

    else if (newUser > 0)
    {
      messageText.setText("You finished a square.  Move again!");
      gameState = GameWAITUSER;   // yes, waiting for user to move
    }

    /* No special moves, no end game.  Give the next player a turn. */

    else if (gameState == GameCHECKCOMP)
      gameState = GameWAITUSER;   // from computer to user
    else
      gameState = GameWAITCOMP;   // from user to computer

  } // end checkBoard() method


/*
  clearBoard() method

  Create a new game board, or clear the existing game board to all empty
  positions.  This method should not do any GUI calls such as repaint()
  because:

  (1) clearBoard() is called by init() before the GUI interface is established;
      and
  (2) There are several methods that make changes to GUI objects after calling
      clearBoard() and before they are ready to repaint.  If clearBoard()
      forced a repaint, then too many unnecessary paint operations would be
      performed.
*/
  void clearBoard()
  {
    int col;                      // temporary column number (index)
    int row;                      // temporary row number (index)

    /* Allocate new arrays if there are no previous arrays, or if the sizes
    have changed.  Note that the internal game board has an extra column on the
    right and an extra row on the bottom to make programming logic easier. */

    if ((boardSquare == null)    // if no previous game board
      || (boardSquare.length != (numRows + 1)) // if new number of rows
      || (boardSquare[0].length != (numCols + 1))) // if new number of columns
    {
      boardLeft = new int[numRows + 1][numCols + 1]; // vertical lines
      boardLeftOld = new int[numRows + 1][numCols + 1]; // previous values
      boardSquare = new int[numRows + 1][numCols + 1]; // board squares
      boardSquareOld = new int[numRows + 1][numCols + 1]; // previous values
      boardTop = new int[numRows + 1][numCols + 1]; // horizontal lines
      boardTopOld = new int[numRows + 1][numCols + 1]; // previous values
    }

    /* Initialize the arrays to default values. */

    for (row = 0; row <= numRows; row ++)
      for (col = 0; col <= numCols; col ++)
      {
        boardLeft[row][col] = LineEMPTY; // no vertical lines yet
        boardLeftOld[row][col] = INVALID; // invalidate previous board
        boardSquare[row][col] = SquareEMPTY; // make all positions empty
        boardSquareOld[row][col] = INVALID; // invalidate previous board
        boardTop[row][col] = LineEMPTY; // no horizontal lines yet
        boardTopOld[row][col] = INVALID; // invalidate previous board
      }

    /* Set the initial game state. */

    gameState = GameWAITUSER;     // user's turn to move
    hoverCol = hoverRow = hoverType = INVALID; // mouse is not hovering

  } // end of clearBoard() method


/*
  countLines() method

  Count the number of filled lines around a board square (from 0 to 4).  The
  code for this is ugly and error-prone, so it is centralized here to prevent
  mistakes from having multiple copies.  This method returns zero for row and
  column numbers that are not full board squares, which makes programming
  easier for methods that call countLines().
*/
  int countLines(
    int row,                      // row number (index)
    int col)                      // column number (index)
  {
    int lines;                    // number of lines around one board square

    if ((row < 0) || (row >= numRows) || (col < 0) || (col >= numCols))
      lines = 0;                  // edges are not full board squares
    else
      lines = ((boardTop[row][col] == LineFULL) ? 1 : 0) // top
              + ((boardLeft[row][col] == LineFULL) ? 1 : 0) // left
              + ((boardLeft[row][col + 1] == LineFULL) ? 1 : 0) // right
              + ((boardTop[row + 1][col] == LineFULL) ? 1 : 0); // bottom

    return lines;                 // tell the caller what we found

  } // end of countLines() method


/*
  flashPosition() method

  Highlight a board position (line or square) briefly, to show where the user
  finishes a square, the computer chooses a line, or the computer finishes a
  square.  The position is shown in the hover color, the empty color, the hover
  color again, and then the empty color.  The position is left in the empty
  state, and the caller must change it to whatever final state it should be.

  This method must not be called from the regular GUI thread, because the
  repaint() calls will fail and the GUI thread will be blocked until this
  method completes.
*/
  void flashPosition(
    int type,                     // <TypeHZLINE>, <TypeSQUARE>, <TypeVTLINE>
    int row,                      // row number (index)
    int col)                      // column number (index)
  {
    switch (type)
    {
      case TypeHZLINE:
        boardTop[row][col] = LineHOVER;
        boardCanvas.repaint();    // redraw the game board
        sleep(600);               // 600 milliseconds (0.6 seconds)
        boardTop[row][col] = LineEMPTY;
        boardCanvas.repaint();    // redraw the game board
        sleep(400);               // 400 milliseconds (0.4 seconds)
        boardTop[row][col] = LineHOVER;
        boardCanvas.repaint();    // redraw the game board
        sleep(600);               // 600 milliseconds (0.6 seconds)
        boardTop[row][col] = LineEMPTY;
        sleep(200);               // 200 milliseconds (0.2 seconds)
        break;

      case TypeSQUARE:
        boardSquare[row][col] = SquareHOVER;
        boardCanvas.repaint();    // redraw the game board
        sleep(600);               // 600 milliseconds (0.6 seconds)
        boardSquare[row][col] = SquareEMPTY;
        boardCanvas.repaint();    // redraw the game board
        sleep(400);               // 400 milliseconds (0.4 seconds)
        boardSquare[row][col] = SquareHOVER;
        boardCanvas.repaint();    // redraw the game board
        sleep(600);               // 600 milliseconds (0.6 seconds)
        boardSquare[row][col] = SquareEMPTY;
        sleep(200);               // 200 milliseconds (0.2 seconds)
        break;

      case TypeVTLINE:
        boardLeft[row][col] = LineHOVER;
        boardCanvas.repaint();    // redraw the game board
        sleep(600);               // 600 milliseconds (0.6 seconds)
        boardLeft[row][col] = LineEMPTY;
        boardCanvas.repaint();    // redraw the game board
        sleep(400);               // 400 milliseconds (0.4 seconds)
        boardLeft[row][col] = LineHOVER;
        boardCanvas.repaint();    // redraw the game board
        sleep(600);               // 600 milliseconds (0.6 seconds)
        boardLeft[row][col] = LineEMPTY;
        sleep(200);               // 200 milliseconds (0.2 seconds)
        break;

      default:
        System.out.println("error in flashPosition(): bad type = " + type);
    }
  } // end of flashPosition() method


/*
  itemStateChanged() method

  This method is called when the user changes the difficulty level, the number
  of rows, or the number of columns.  We assume that any value returned from
  the GUI is in the proper range.
*/
  public void itemStateChanged(ItemEvent event)
  {
    Object source = event.getSource(); // where the event came from
    if (source == columnCounter)  // new number of columns?
    {
      numCols = Integer.parseInt(columnCounter.getSelectedItem());
      clearBoard();               // start a new game board
      messageText.setText("New game board is " + numRows + " rows by "
        + numCols + " columns.");
      eraseFlag = true;           // boardUpdate() must redraw game board
      boardCanvas.repaint();      // redraw the game board
    }
    else if (source == levelChoice) // new level of difficulty
    {
      String level = levelChoice.getSelectedItem();
      if (level.equals(LevelStringEASY))
        levelFlag = LevelEASY;
      else if (level.equals(LevelStringMEDIUM))
        levelFlag = LevelMEDIUM;
      else if (level.equals(LevelStringHARD))
        levelFlag = LevelHARD;
      else if (level.equals(LevelStringEXPERT))
        levelFlag = LevelEXPERT;
      else
      {
        System.out.println(
          "error in itemStateChanged(): bad difficulty level \"" + level
          + "\"");
        levelFlag = LevelMEDIUM;  // reset to medium level of difficulty
      }
    }
    else if (source == rowCounter) // new number of rows?
    {
      numRows = Integer.parseInt(rowCounter.getSelectedItem());
      clearBoard();               // start a new game board
      messageText.setText("New game board is " + numRows + " rows by "
        + numCols + " columns.");
      eraseFlag = true;           // boardUpdate() must redraw game board
      boardCanvas.repaint();      // redraw the game board
    }
    else
    {
      System.out.println(
        "error in itemStateChanged(): ItemEvent not recognized: " + event);
    }
  } // end of itemStateChanged() method


/*
  mouseToBoard() method

  Convert a mouse click with coordinates (x, y) on the board canvas into column
  <mouseCol> and row <mouseRow> numbers, along with a flag <mouseType> to
  indicate if the user was clicking on a horizontal line (top side of a board
  square), vertical line (left side), or a board square.  If the click isn't
  directly on a recognized position, <INVALID> is returned for all values.

  Our calculations use several global variables set by the boardUpdate()
  method.
*/
  void mouseToBoard(int x, int y)
  {
    int col;                      // calculate column number
    int colExtra;                 // remainder from column calculation
    int pixels;                   // temporary number of pixels
    int row;                      // calculate row number
    int rowExtra;                 // remainder from row calculation

    /* Convert the (x, y) coordinates into row and column numbers, with a
    little extra information to tell us if the user was clicking on a board
    position, or if the clicks are on an inner or outer border. */

    pixels = x - boardLeftMargin;
    col = pixels / boardGridStep;
    colExtra = pixels % boardGridStep;

    pixels = y - boardTopMargin;
    row = pixels / boardGridStep;
    rowExtra = pixels % boardGridStep;

    if ((col >= 0)                // click on a horizontal line?
      && (col < numCols)
      && (colExtra > boardDotSize)
      && (colExtra < (boardDotSize + boardSymbolSize))
      && (row >= 0)
      && (row <= numRows)         // one extra row for bottom of board
      && (rowExtra > 0)
      && (rowExtra < boardDotSize))
    {
      mouseCol = col;             // save mouse column number
      mouseRow = row;             // save mouse row number
      mouseType = TypeHZLINE;     // user clicked on horizontal line
    }

    else if ((col >= 0)           // click on a vertical line?
      && (col <= numCols)         // one extra column for right side of board
      && (colExtra > 0)
      && (colExtra < boardDotSize)
      && (row >= 0)
      && (row < numRows)
      && (rowExtra > boardDotSize)
      && (rowExtra < (boardDotSize + boardSymbolSize)))
    {
      mouseCol = col;             // save mouse column number
      mouseRow = row;             // save mouse row number
      mouseType = TypeVTLINE;     // user clicked on vertical line
    }

    else if ((col >= 0)           // click on a board square?
      && (col < numCols)
      && (colExtra > boardDotSize)
      && (colExtra < (boardDotSize + boardSymbolSize))
      && (row >= 0)
      && (row < numRows)
      && (rowExtra > boardDotSize)
      && (rowExtra < (boardDotSize + boardSymbolSize)))
    {
      mouseCol = col;             // save mouse column number
      mouseRow = row;             // save mouse row number
      mouseType = TypeSQUARE;     // user clicked on board square
    }

    else                          // mouse click was not recognized
    {
      mouseCol = mouseRow = mouseType = INVALID;
    }
  } // end of mouseToBoard() method


/*
  moveComputer() method

  This method is called to make the computer's move.  It is not responsible for
  checking the user's move, or the result after the computer's move.
*/
  void moveComputer()
  {
    switch (levelFlag)            // who makes the move depends upon difficulty
    {
      case LevelEASY:             // less than medium
        moveEasy();               // implemented
        break;

      case LevelMEDIUM:           // more than easy, less than hard
        moveMedium();             // implemented
        break;

      case LevelHARD:             // more than medium, less than expert
        moveHard();               // currently not implemented
        break;

      case LevelEXPERT:           // more than hard
        moveExpert();             // currently not implemented
        break;

      default:
        System.out.println("error in moveComputer(): bad levelFlag = "
            + levelFlag);
        break;
    }
  } // end of moveComputer() method


/*
  moveEasy() method

  This method makes the computer's move for the "easy" level by randomly
  selecting any unused line.  There is at least one empty line, or else we
  wouldn't have been called.
*/
  void moveEasy()
  {
    int choice;                   // random index in <list>
    int col;                      // temporary column number (index)
    int[][] list;                 // list of possible moves
    int listSize;                 // number of items in <list>
    int row;                      // temporary row number (index)
    int type;                     // <TypeHZLINE> or <TypeVTLINE>

    /* Initialize a list of valid moves containing the line type, row, and
    column. */

    list = new int[2 * (numRows + 1) * (numCols * 1)][3];
                                  // enough for whole board, in two directions
    listSize = 0;                 // nothing used in list yet

    /* Add empty horizontal lines to the list. */

    for (row = 0; row <= numRows; row ++)
      for (col = 0; col < numCols; col ++)
        if (boardTop[row][col] != LineFULL)
        {
          list[listSize][0] = TypeHZLINE; // mark as horizontal line
          list[listSize][1] = row; // save row number
          list[listSize][2] = col; // save column number
          listSize ++;            // increment number of items in list
        }

    /* Add empty vertical lines to the list. */

    for (row = 0; row < numRows; row ++)
      for (col = 0; col <= numCols; col ++)
        if (boardLeft[row][col] != LineFULL)
        {
          list[listSize][0] = TypeVTLINE; // mark as horizontal line
          list[listSize][1] = row; // save row number
          list[listSize][2] = col; // save column number
          listSize ++;            // increment number of items in list
        }

    /* Select a move at random. */

    choice = (int) (Math.random() * listSize);
    type = list[choice][0];       // get selected line type
    row = list[choice][1];        // get selected row number
    col = list[choice][2];        // get selected column number
    flashPosition(type, row, col); // flash display
    switch (type)                 // was it a horizontal or vertical line?
    {
      case TypeHZLINE:            // horizontal line
        boardTop[row][col] = LineFULL;
        break;

      case TypeVTLINE:            // vertical line
        boardLeft[row][col] = LineFULL;
        break;

      default:
        System.out.println("error in moveEasy(): bad line type = " + type);
        break;
    }
    boardCanvas.repaint();        // redraw the game board

  } // end of moveEasy() method


/*
  moveExpert() method

  This method makes the computer's move for the "expert" level of difficulty
  (more difficult than "hard").  It is currently unimplemented and defaults to
  the same as the "medium" level.

  Any method to play at the "hard" or "expert" levels will be large and should
  be called as a static method in another Java class in a separate file.  This
  will distinguish the main graphical interface from the added non-graphical
  move strategy.
*/
  void moveExpert()
  {
    System.out.println("moveExpert called, but not implemented");
    moveMedium();                 // default to "medium"

  } // end of moveExpert() method


/*
  moveHard() method

  This method makes the computer's move for the "hard" level of difficulty
  (more difficult than "medium" and easier than "expert").  It is currently
  unimplemented and defaults to the same as the "medium" level.

  Any method to play at the "hard" or "expert" levels will be large and should
  be called as a static method in another Java class in a separate file.  This
  will distinguish the main graphical interface from the added non-graphical
  move strategy.
*/
  void moveHard()
  {
    System.out.println("moveHard called, but not implemented");
    moveMedium();                 // default to "medium"

  } // end of moveHard() method


/*
  moveMedium() method

  This method makes the computer's move for the "medium" level.  moveMedium()
  is similar to moveEasy() in that we build up a list of possible moves.  The
  essential difference is that we apply a rough measure to the list and only
  keep moves with a high "value".  For each empty line, we count the number of
  existing lines around squares that share the same line.  When looking at a
  single square, points are awarded as follows:

    lines   points    explanation
      0       0       Basis for comparison and must be zero.  This gives the
                      same weight to truly empty squares, bottom and right
                      edges, and non-existent squares above and left of the
                      board.
      1       1       Encourage computer to build near user, so that the game
                      has more interaction.
      2      -2       Avoid squares that already have two lines, because if we
                      add a third line, then the user will claim the square in
                      the next move.
      3       3       Try to complete squares that already have three lines.
      4      (*)      Major internal logic error if a board square is empty but
                      has four edges!

  Most edges are shared by two squares.  We can do a small amount of planning
  by recognizing that when an empty line separates a square with three lines
  from a square with two lines, this square with two lines is actually good for
  us because we can take *both* squares in sequence.  Of course, two squares
  with three lines each would be even better!  The <weights> array below is a
  *ranking* of the relative board values, rather than some absolute measure.

  There is no claim that these point values are optimal.  They quickly do a
  reasonable job with requiring extensive lookahead.  The drawback is that
  without lookahead, the computer gives away too many squares towards the end
  of the game.  Correcting this is what the "hard" and "expert" levels are for!
*/
  void moveMedium()
  {
    int choice;                   // random index in <list>
    int col;                      // temporary column number (index)
    int[][] list;                 // list of possible moves
    int listSize;                 // number of items in <list>
    int maxValue;                 // maximum value found so far
    int row;                      // temporary row number (index)
    int type;                     // <TypeHZLINE> or <TypeVTLINE>
    int value;                    // value of a single line
    final int[][] weights = {{ 0,  1, -2,  3}, // index by 0, 1, 2, 3 lines
                             { 1,  2, -1,  4}, // must be symmetrical
                             {-2, -1, -3,  5}, // ranking from -3 to +6
                             { 3,  4,  5,  6}};

    /* Initialize a list of valid moves containing the line type, row, and
    column. */

    list = new int[2 * (numRows + 1) * (numCols * 1)][3];
                                  // enough for whole board, in two directions
    listSize = 0;                 // nothing in list yet
    maxValue = -999;              // any move is better than this!

    /* Add empty horizontal lines to the list. */

    for (row = 0; row <= numRows; row ++)
      for (col = 0; col < numCols; col ++)
        if (boardTop[row][col] != LineFULL)
        {
          /* Estimate this line's value. */

          value = weights[countLines(row, col)][countLines(row - 1, col)];
                                  // this square and square above

          /* If the value is greater than the previous maximum, clear list. */

          if (value > maxValue)
          {
            listSize = 0;         // ignore anything previous in list
            maxValue = value;     // save new maximum value
          }

          /* If the value is equal to the previous or new maximum, add to the
          list. */

          if (value >= maxValue)
          {
            list[listSize][0] = TypeHZLINE; // mark as horizontal line
            list[listSize][1] = row; // save row number
            list[listSize][2] = col; // save column number
            listSize ++;            // increment number of items in list
          }
        }

    /* Add empty vertical lines to the list. */

    for (row = 0; row < numRows; row ++)
      for (col = 0; col <= numCols; col ++)
        if (boardLeft[row][col] != LineFULL)
        {
          /* Estimate this line's value. */

          value = weights[countLines(row, col)][countLines(row, col - 1)];
                                  // this square and square to the left

          /* If the value is greater than the previous maximum, clear list. */

          if (value > maxValue)
          {
            listSize = 0;         // ignore anything previous in list
            maxValue = value;     // save new maximum value
          }

          /* If the value is equal to the previous or new maximum, add to the
          list. */

          if (value >= maxValue)
          {
            list[listSize][0] = TypeVTLINE; // mark as horizontal line
            list[listSize][1] = row; // save row number
            list[listSize][2] = col; // save column number
            listSize ++;            // increment number of items in list
          }
        }

    /* Select a move at random. */

    choice = (int) (Math.random() * listSize);
    type = list[choice][0];       // get selected line type
    row = list[choice][1];        // get selected row number
    col = list[choice][2];        // get selected column number
    flashPosition(type, row, col); // flash display
    switch (type)                 // was it a horizontal or vertical line?
    {
      case TypeHZLINE:            // horizontal line
        boardTop[row][col] = LineFULL;
        break;

      case TypeVTLINE:            // vertical line
        boardLeft[row][col] = LineFULL;
        break;

      default:
        System.out.println("error in moveMedium(): bad line type = " + type);
        break;
    }
    boardCanvas.repaint();        // redraw the game board

  } // end of moveMedium() method


/*
  paint() method

  This applet doesn't have paint() or update() methods because all drawing is
  done by components (Button, Canvas, Panel, etc).  The game board is a
  subclass of Canvas that passes its paint() and update() calls to boardPaint()
  and boardUpdate() in the main DotsAndBoxes2 class.
*/


/*
  run() method

  This method executes as a separate thread after the user makes a move.  The
  run() thread has the same context as the regular GUI thread (the applet), and
  allows the program to perform some animation using delays followed by
  repaint() calls.

  Note:  Please don't confuse the thread run() method with the applet start()
  and stop() methods.
*/
  public void run()
  {
    /* If the user just moved, then check whether the user gets to move again
    because he/she completed a square. */

    if (gameState == GameCHECKUSER)
    {
      checkBoard();               // check result of user's move
    }

    /* The game might be over (GameFINISH), or the user may have another move
    (GameWAITUSER), or it may be the computer's turn to move (GameWAITCOMP).
    If the computer completes a square, the computer gets another move. */

    sleep(500 + (int) (Math.random() * 800)); // delay 0.5 to 1.3 seconds
    while (gameState == GameWAITCOMP)
    {
      moveComputer();             // make the computer's move
      messageText.setText(noMessage); // clear any previous message text
      gameState = GameCHECKCOMP;  // set flag saying who just moved
      checkBoard();               // check result of computer's move
    }

    /* Now it's the user's turn to move again, or the game is finished.  Return
    from this method, which closes the run() thread. */

  } // end of run() method


/*
  sleep() method

  Sleep (delay) for the given number of milliseconds.  This method should only
  be called from a run() thread, and must not be called from the regular GUI
  thread; otherwise, the GUI thread will be blocked until this method returns.

  The delay must be at least 5 ms for Netscape 4.7/4.8 (JDK1.1).  Anything less
  is treated as no delay.
*/
  void sleep(int delay)
  {
    try { Thread.sleep(delay); }  // sleep (delay)
        catch (InterruptedException e) { /* do nothing */ }

  } // end of sleep() method


/*
  update() method

  This applet doesn't have paint() or update() methods because all drawing is
  done by components (Button, Canvas, Panel, etc).  The game board is a
  subclass of Canvas that passes its paint() and update() calls to boardPaint()
  and boardUpdate() in the main DotsAndBoxes2 class.
*/

} // end of DotsAndBoxes2 class

// ------------------------------------------------------------------------- //

/*
  DotsAndBoxes2Board class

  Create a subclass of Canvas for the game board, so that we can take over from
  the regular mouse and paint routines.  These Canvas methods pass back their
  arguments to methods in DotsAndBoxes2 so that they can be processed in the
  context of the main DotsAndBoxes2 class.

  Note that the DotsAndBoxes2Board subclass assumes that a DotsAndBoxes2 object
  is the parent container of <boardCanvas>.
*/

class DotsAndBoxes2Board
      extends Canvas
      implements MouseListener, MouseMotionListener
{
  public void mouseClicked(MouseEvent event)
  {
    ((DotsAndBoxes2) this.getParent()).boardMouseClicked(event, this);
                                  // pass back (1) mouse event and (2) object
                                  // reference for <boardCanvas>
  }

  public void mouseDragged(MouseEvent event) { }
  public void mouseEntered(MouseEvent event) { }
  public void mouseExited(MouseEvent event) { }

  public void mouseMoved(MouseEvent event)
  {
    ((DotsAndBoxes2) this.getParent()).boardMouseMoved(event, this);
                                  // pass back (1) mouse event and (2) object
                                  // reference for <boardCanvas>
  }

  public void mousePressed(MouseEvent event) { }
  public void mouseReleased(MouseEvent event) { }

  public void paint(Graphics gr)
  {
    ((DotsAndBoxes2) this.getParent()).boardPaint(gr, this);
                                  // pass back (1) graphics context and (2)
                                  // object reference for <boardCanvas>
  }

  public void update(Graphics gr)
  {
    ((DotsAndBoxes2) this.getParent()).boardUpdate(gr, this);
                                  // pass back (1) graphics context and (2)
                                  // object reference for <boardCanvas>
  }

} // end of DotsAndBoxes2Board class

// ------------------------------------------------------------------------- //

/*
  DotsAndBoxes2Window class

  This applet can also be run as an application by calling the main() method
  instead of the init() method.  As an application, it must exit when its main
  window is closed.  A window listener is necessary because EXIT_ON_CLOSE is a
  JFrame option in Java Swing, not a basic AWT Frame option.  It is easier to
  extend WindowAdapter here with one method than to implement all methods of
  WindowListener in the main applet.
*/

class DotsAndBoxes2Window extends WindowAdapter
{
  public void windowClosing(WindowEvent event)
  {
    System.exit(0);               // exit from this application
  }
} // end of DotsAndBoxes2Window class

/* Copyright (c) 2004 by Keith Fenske.  Released under GNU Public License. */
