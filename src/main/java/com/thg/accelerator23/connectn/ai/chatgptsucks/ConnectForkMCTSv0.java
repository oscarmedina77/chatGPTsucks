package com.thg.accelerator23.connectn.ai.chatgptsucks;

import com.thehutgroup.accelerator.connectn.player.Board;
import com.thehutgroup.accelerator.connectn.player.Counter;
import com.thehutgroup.accelerator.connectn.player.Player;
import com.thg.accelerator23.connectn.ai.chatgptsucks.analysis.BoardAnalyser;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ConnectForkMCTSv0 extends Player {
  private static Board board;

//  Observation???
//  global current_state?

  public ConnectForkMCTSv0(Counter counter) {
    //TODO: fill in your name here
    super(counter, ConnectForkMCTSv0.class.getName());
  }

  @Override
  public int makeMove(Board board) {

    LocalDateTime initTime = java.time.LocalDateTime.now();
    double EMPTY = 0;
    int T_max = 10;
    double Cp_default = 1;

    BoardAnalyser boardAnalyser = new BoardAnalyser(board.getConfig());
    List<Integer> availableMoves = new ArrayList<>();



//    DON'T FORGET CODE AFTER STATE CLASS

    return 3;
  }
}




