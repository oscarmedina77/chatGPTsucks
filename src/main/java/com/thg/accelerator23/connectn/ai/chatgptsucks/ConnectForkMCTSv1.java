package com.thg.accelerator23.connectn.ai.chatgptsucks;

import com.thehutgroup.accelerator.connectn.player.*;
import com.thg.accelerator23.connectn.ai.chatgptsucks.analysis.GameState;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ConnectForkMCTSv1 extends Player {
  public double Cp_default = 1.0;

  public ConnectForkMCTSv1(Counter counter) {
    //TODO: fill in your name here
    super(counter, ConnectForkMCTSv1.class.getName());
  }

  public Board addMoveToBoard(Board board, int move) {
    Board newBoard;
    try {
      newBoard = new Board(board, move, this.getCounter());
    } catch (InvalidMoveException e) {
      return null;
    }
    return newBoard;
  }

  public double[] checkFinishAndScore() {
    Counter counter = this.getCounter();
    GameState gameState = new GameState(counter);

    if (gameState.isWin()) {
      return new double[]{1, 1};
    }
    if (gameState.isDraw()) {
      return new double[]{1, 0.5};
    } else {
      return new double[]{0, 0.5};
    }
  }

  public Double uctScore(double nodeTotalScore, int nodeTotalVisits, int parentTotalVisits, double Cp) {
    if (nodeTotalVisits == 0) {
      return Double.POSITIVE_INFINITY;
    }
    return nodeTotalScore / nodeTotalVisits + Cp * Math.sqrt(2 * Math.log(parentTotalVisits) / nodeTotalVisits);
  }

  public int opponentMark(int mark) {
    return 3 - mark;
  }

  public double opponentScore(double score) {
    return 1 - score;
  }

  public double defaultPolicySimulation(Board board, int mark) {
    double originalMark = mark;

    double[] finishScore = this.checkFinishAndScore();
    this.makeMoveAltRandom(board);

    int dfpsCounter = 0;
    while (finishScore[0] != 1.0) {
      dfpsCounter = dfpsCounter + 1;
      mark = this.opponentMark(mark);
      this.makeMoveAltRandom(board);
      finishScore = this.checkFinishAndScore();
    }

//    TODO - WHY DOES THIS NEVER PRINT?
    System.out.println("dfpsCounter = " + dfpsCounter);

    if (mark == originalMark) {
      return finishScore[1];
    }

    return opponentScore(finishScore[1]);
  }

  @Override
  public int makeMove(Board board) {

    double initTimeMilSecs = System.currentTimeMillis();
    double T_max = 900;
    GameConfig config = board.getConfig();

    int mark = 1;

    StatePython currentState = new StatePython(this.getCounter(),
            board,
            mark,
            config,
            null,
            false,
            null,
            null);

    try {
      currentState = currentState.chooseChildViaAction(currentState.getActionTaken());
      currentState.setParent(null);

    } catch (Exception e) {
      currentState = new StatePython(this.getCounter(),
              board,
              mark,
              config,
              null,
              false,
              null,
              null);
    }

    int makeMoveCounter = 0;
    while (System.currentTimeMillis() - initTimeMilSecs <= T_max) {
      currentState.treeSingleRun();
      makeMoveCounter = makeMoveCounter + 1;
    }
    System.out.println("makeMoveCounter = " + makeMoveCounter);

    try {
      currentState = currentState.choosePlayChild();
      return currentState.getActionTaken();
    } catch (NullPointerException npE) {
      List<Integer> availableMoves = new ArrayList<>();
      for (int i = 0; i < board.getConfig().getWidth(); i++) {
        try {
          new Board(board, i, this.getCounter());
          availableMoves.add(i);
        } catch (InvalidMoveException e) {
        }
      }
      System.out.println("makeMove returned random valid");
      return availableMoves.get(new Random().nextInt(availableMoves.size()));
    }
  }
  public Board makeMoveAltRandom(Board board) {
//    BoardAnalyser boardAnalyser = new BoardAnalyser(board.getConfig());
    List<Integer> availableMoves =  new ArrayList<>();

//    TODO - not sure if having "intelligent" random helps here?
//    if (boardAnalyser.winningPositionAvailable(this.getCounter(), board)) {
//      return addMoveToBoard(board, boardAnalyser.winningPosition(this.getCounter(), board));
//
//    } else if (boardAnalyser.winningPositionAvailable(this.getCounter().getOther(), board)) {
//      return addMoveToBoard(board, boardAnalyser.winningPosition(this.getCounter().getOther(), board));
//
//    } else {
      for (int i = 0; i < board.getConfig().getWidth(); i++){
        try {
          new Board(board, i, this.getCounter());
          availableMoves.add(i);
        } catch (InvalidMoveException e) {
        }
      }
      return addMoveToBoard(board, availableMoves.get(new Random().nextInt(availableMoves.size())));
    }
  }





