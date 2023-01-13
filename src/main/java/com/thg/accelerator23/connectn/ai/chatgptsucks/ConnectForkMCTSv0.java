package com.thg.accelerator23.connectn.ai.chatgptsucks;

import com.thehutgroup.accelerator.connectn.player.*;
import com.thg.accelerator23.connectn.ai.chatgptsucks.analysis.BoardAnalyser;
import com.thg.accelerator23.connectn.ai.chatgptsucks.analysis.GameState;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.ToDoubleBiFunction;

public class ConnectForkMCTSv0 extends Player {
  private static Board board;
  double Cp_default = 1;

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
    return nodeTotalScore / nodeTotalVisits + Cp + Math.sqrt(2*Math.log(parentTotalVisits / nodeTotalVisits));
  }

  public int opponentMark(int mark) {
    return 3 - mark;
  }

  public double opponentScore(double score) {
    return 1 - score;
  }

  public int randomAction(Board board, GameConfig config) {
    BoardAnalyser boardAnalyser = new BoardAnalyser(board.getConfig());
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

  public double defaultPolicySimulation(Board board, int mark, GameConfig config, GameState gameState, Counter winner) {
    double originalMark = mark;
    int column = randomAction(board, config);

    double[] finishScore = checkFinishAndScore(board, column, mark, config);

//   TODO this may get stuck, make new function?
    makeMove(board);
    System.out.println("are we stuck lmao");

    while (!gameState.isEnd()) {
      mark = opponentMark(mark);
      column = randomAction(board, config);
      makeMove(board);
      finishScore = checkFinishAndScore(board, column, mark, config);
    }

    if (mark == originalMark) {
      return finishScore[1];
    }

    return opponentScore(finishScore[1]);

  }

  public int findActionTakenByOpponent(Board newBoard, Board oldBoard, GameConfig config) {
//    TODO Is this fully necessary, or used to speed up?
    return 0;
  }
  @Override
  public int makeMove(Board board) {

    double initTimeMilSecs = System.currentTimeMillis();
//    double EMPTY = 0;
    double T_max = 10;
    GameConfig config = board.getConfig();

    int mark = 0;

    StatePython currentState = new StatePython(getCounter(),
                                    board,
                                    mark,
                                    config,
                                    null,
                                    false,
                                    null,
                                    null);

    BoardAnalyser boardAnalyser = new BoardAnalyser(config);
    List<Integer> availableMoves = new ArrayList<>();

//    TODO May not be fully necessary
//    try {
//      currentState = currentState.chooseChildViaAction(findActionTakenByOpponent(...));
//    }

//    TODO DON'T FORGET CODE AFTER STATE CLASS
    while (System.currentTimeMillis() - initTimeMilSecs <= T_max) {
      currentState.treeSingleRun();
    }

    return currentState.getActionTaken();
  }
}




