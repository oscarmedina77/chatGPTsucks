package com.thg.accelerator23.connectn.ai.chatgptsucks;

import com.thehutgroup.accelerator.connectn.player.*;
import com.thg.accelerator23.connectn.ai.chatgptsucks.analysis.GameState;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ConnectForkMCTSv1 extends Player {
  public Double Cp_default = 1.0;

  public ConnectForkMCTSv1(Counter counter) {
    //TODO: fill in your name here
    super(counter, ConnectForkMCTSv1.class.getName());
  }

  public Board addMoveToBoard(Board board, int move, StatePython currentState) {
    Board newBoard;
    try {
      newBoard = new Board(board, move, currentState.getCounter());
    } catch (InvalidMoveException e) {
      return null;
    }
    return newBoard;
  }

  public double[] checkFinishAndScore(StatePython currentState) {
    Counter counter = currentState.getCounter();
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

  public double defaultPolicySimulation(Board board, int mark, StatePython currentState) {
    double originalMark = mark;

    double[] finishScore = checkFinishAndScore(currentState);

    currentState.setBoard(makeMoveAltRandom(board, currentState));

    int dfpsCounter = 0;
    while (finishScore[0] != 1.0) {
      dfpsCounter = dfpsCounter + 1;
      mark = opponentMark(mark);
      currentState.makeMoveAltRandom(board, currentState);
      finishScore = checkFinishAndScore(currentState);
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
    double T_max = 9000;
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
      System.out.println("makeMove first try actionTaken = " + currentState.getActionTaken());
      currentState.chooseChildViaAction(currentState.getActionTaken(), currentState);
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
      System.out.println("makeMove first try failed");
    }

    int makeMoveCounter = 0;
    while (System.currentTimeMillis() - initTimeMilSecs <= T_max) {
      currentState.treeSingleRun(currentState);
      makeMoveCounter = makeMoveCounter + 1;
    }
    System.out.println("makeMoveCounter = " + makeMoveCounter);

    try {
      currentState.choosePlayChild(currentState);
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
  public Board makeMoveAltRandom(Board board, StatePython currentState) {
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
          new Board(board, i, currentState.getCounter());
          availableMoves.add(i);
        } catch (InvalidMoveException e) {
        }
      }
      return addMoveToBoard(board, availableMoves.get(new Random().nextInt(availableMoves.size())), currentState);
    }
  }





