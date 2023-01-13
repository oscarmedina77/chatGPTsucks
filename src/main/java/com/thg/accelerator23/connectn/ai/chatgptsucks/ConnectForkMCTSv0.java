package com.thg.accelerator23.connectn.ai.chatgptsucks;

import com.thehutgroup.accelerator.connectn.player.*;
import com.thg.accelerator23.connectn.ai.chatgptsucks.analysis.BoardAnalyser;
import com.thg.accelerator23.connectn.ai.chatgptsucks.analysis.GameState;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ConnectForkMCTSv0 extends Player {
  private Board board;
  private int mark = 0;
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

  public ConnectForkMCTSv0(Counter counter) {
    //TODO: fill in your name here
    super(counter, ConnectForkMCTSv0.class.getName());
  }

  public double[] checkFinishAndScore(Board board, int column, int mark, GameConfig config) {
    Counter winner = getCounter();

    GameState gameState = new GameState(winner);
    if (gameState.isWin()) {
      return new double[]{1, 1};
    }
    if (gameState.isDraw()) {
      return new double[]{1, 0.5};
    }
    else {
      return new double[]{0, 0.5};
    }
  }

  public Double uctScore(double nodeTotalScore, int nodeTotalVisits, int parentTotalVisits, double Cp) {
    if (nodeTotalVisits == 0) {
      return Double.POSITIVE_INFINITY;
    }
    return nodeTotalScore / nodeTotalVisits + Cp * Math.sqrt(2*Math.log(parentTotalVisits) / nodeTotalVisits);
  }

  public int opponentMark(int mark) {
    return 3 - mark;
  }

  public double opponentScore(double score) {
    return 1 - score;
  }

  public int randomAction(Board board, GameConfig config) {
//    BoardAnalyser boardAnalyser = new BoardAnalyser(board.getConfig());
    List<Integer> availableMoves =  new ArrayList<>();

    for (int i = 0; i< board.getConfig().getWidth(); i ++){
      try {
        new Board(board, i, this.getCounter());
        availableMoves.add(i);
      } catch (InvalidMoveException e) {
      }
    }
    return availableMoves.get(new Random().nextInt(availableMoves.size()));
  }

  public double defaultPolicySimulation(Board board, int mark, GameConfig config, GameState gameState) {
    double originalMark = mark;
    int column = randomAction(board, config);
    int episodes = 10;

    double[] finishScore = checkFinishAndScore(board, column, mark, config);

//   TODO this may get stuck, make new function?
    makeMoveInSim(board, mark, episodes);
    System.out.println("are we stuck lmao");

    while (!gameState.isEnd()) {
      mark = opponentMark(mark);
      column = randomAction(board, config);
      makeMoveInSim(board, mark, episodes);
      finishScore = checkFinishAndScore(board, column, mark, config);
    }

    if (mark == originalMark) {
      return finishScore[1];
    }

    return opponentScore(finishScore[1]);
  }

  @Override
  public int makeMove(Board board) {

    double initTimeMilSecs = System.currentTimeMillis();
//    double EMPTY = 0;
    double T_max = 1000;
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

//    TODO - needed?
//    BoardAnalyser boardAnalyser = new BoardAnalyser(config);
//    List<Integer> availableMoves = new ArrayList<>();

//    TODO - may not be fully necessary
//    Board intState = null;
//    StatePython newState = null;

    try {
//      TODO - needed?
//      intState = new Board(intState, currentState.getActionTaken(), this.getCounter());
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

    while (System.currentTimeMillis() - initTimeMilSecs <= T_max) {
//      System.out.println("Time in makeMove while loop = " + (System.currentTimeMillis() - initTimeMilSecs));
      currentState.treeSingleRun();
    }

//    if board not empty???
    try {
      currentState = currentState.choosePlayChild();
      return currentState.getActionTaken();
    }
    catch (NullPointerException npE) {
//      return random move for first move, does this work?
      List<Integer> availableMoves =  new ArrayList<>();
      for (int i = 0; i< board.getConfig().getWidth(); i ++){
        try {
          new Board(board, i, this.getCounter());
          availableMoves.add(i);
        } catch (InvalidMoveException e) {
        }
      }
      return availableMoves.get(new Random().nextInt(availableMoves.size()));
    }
  }

  public Board makeMoveInSim(Board board, int mark, int episodes) {

//    double initTimeMilSecsSim = System.currentTimeMillis();
//    double EMPTY = 0;
//    double T_max = 10;
    GameConfig config = board.getConfig();

    StatePython currentState = new StatePython(getCounter(),
            board,
            mark,
            config,
            null,
            false,
            null,
            null);

//    BoardAnalyser boardAnalyser = new BoardAnalyser(config);
//    List<Integer> availableMoves = new ArrayList<>();

//    TODO - may not be fully necessary?
//    try {
//      currentState = currentState.chooseChildViaAction(findActionTakenByOpponent(...));
//    }

//    TODO DON'T FORGET CODE AFTER STATE CLASS - done? I think...
    for (int ep = 0; ep <= episodes; ep++) {
      currentState.treeSingleRun();
    }

    currentState = currentState.choosePlayChild();

    Board newBoardSim = null;
    try {
      newBoardSim = new Board(this.board, currentState.getActionTaken(), this.getCounter());
    } catch (InvalidMoveException e) {
      return newBoardSim;
    }
    return this.board;
  }
}




