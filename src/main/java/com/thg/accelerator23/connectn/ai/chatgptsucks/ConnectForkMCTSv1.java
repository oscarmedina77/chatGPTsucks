package com.thg.accelerator23.connectn.ai.chatgptsucks;

import com.thehutgroup.accelerator.connectn.player.*;
import com.thg.accelerator23.connectn.ai.chatgptsucks.analysis.BoardAnalyser;
import com.thg.accelerator23.connectn.ai.chatgptsucks.analysis.GameState;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ConnectForkMCTSv1 extends Player {
  private Board board;
  private int mark = 1;
  private GameConfig config;
  double Cp_default = 1.0;
//  StatePython currentState = new StatePython(getCounter(),
//                                              board,
//                                              mark,
//                                              config,
//                                              null,
//                                              false,
//                                              null,
//                                              null);

//  TODO Observation???
//  TODO global currentState?

  public ConnectForkMCTSv1(Counter counter) {
    //TODO: fill in your name here
    super(counter, ConnectForkMCTSv1.class.getName());
  }

  public Board addMoveToBoard(Board board, int move) {
    Board newBoard;
    try {
      newBoard = new Board(this.board, move, this.getCounter());
    } catch (InvalidMoveException e) {
      return null;
    }
    return newBoard;
  }

  public double[] checkFinishAndScore(Board board, int column, int mark, GameConfig config) {
    Counter winner = getCounter();

    GameState gameState = new GameState(winner);
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

  public int randomAction(Board board, GameConfig config) {
//    BoardAnalyser boardAnalyser = new BoardAnalyser(board.getConfig());
    List<Integer> availableMoves = new ArrayList<>();

    for (int i = 0; i < board.getConfig().getWidth(); i++) {
      try {
        new Board(board, i, this.getCounter());
        availableMoves.add(i);
      } catch (InvalidMoveException e) {
      }
    }
    System.out.println("****** randomAction returned random valid");
    return availableMoves.get(new Random().nextInt(availableMoves.size()));
  }

  public double defaultPolicySimulation(Board board, int mark, GameConfig config, GameState gameState) {
    double originalMark = mark;
    int column = randomAction(board, config);
//    int episodes = 1000;

    double[] finishScore = checkFinishAndScore(board, column, mark, config);

//   TODO this may get stuck, make new function?
//    makeMoveInSim(board, mark);
    makeMoveAltRandom(board);
    System.out.println("are we stuck lmao");

    int dfpsCounter = 0;
    while (finishScore[0] != 0.0) {
      dfpsCounter = dfpsCounter + 1;
      mark = opponentMark(mark);
      column = randomAction(board, config);
       //    makeMoveInSim(board, mark);
      makeMoveAltRandom(board);
      finishScore = checkFinishAndScore(board, column, mark, config);
    }
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

    StatePython currentState = new StatePython(getCounter(),
            board,
            mark,
            config,
            null,
            false,
            null,
            null);

    try {
      System.out.println("makeMove try failed :(");
      currentState = currentState.chooseChildViaAction(currentState.getActionTaken());
      currentState.setParent(null);

    } catch (Exception e) {
      currentState = new StatePython(getCounter(),
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
//    TODO - below from Oscar ConectFork, I need to return a Board in all returns
//          => use addMoveToBoard function
    BoardAnalyser boardAnalyser = new BoardAnalyser(board.getConfig());
    List<Integer> availableMoves =  new ArrayList<>();

    if (boardAnalyser.winningPositionAvailable(this.getCounter(), board)) {
      return addMoveToBoard(board, boardAnalyser.winningPosition(this.getCounter(), board));

    } else if (boardAnalyser.winningPositionAvailable(this.getCounter().getOther(), board)) {
      return addMoveToBoard(board, boardAnalyser.winningPosition(this.getCounter().getOther(), board));

    } else {
      for (int i = 0; i< board.getConfig().getWidth(); i ++){
        try {
          new Board(board, i, this.getCounter());
          availableMoves.add(i);
        } catch (InvalidMoveException e) {
        }
      }
      return addMoveToBoard(board, availableMoves.get(new Random().nextInt(availableMoves.size())));
    }
}

//  public Board makeMoveInSim(Board board, int mark) {
//    GameConfig config = board.getConfig();
//
//    StatePython currentState = new StatePython(getCounter(),
//            board,
//            mark,
//            config,
//            null,
//            false,
//            null,
//            null);
//
//    currentState.treeSingleRun();
//
//    currentState = currentState.choosePlayChild();
//
//    Board newBoardSim = null;
//    try {
//      newBoardSim = new Board(this.board, currentState.getActionTaken(), this.getCounter());
//    } catch (InvalidMoveException e) {
//      return newBoardSim;
//    }
//    return this.board;
//  }
}




