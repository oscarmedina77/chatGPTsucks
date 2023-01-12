package com.thg.accelerator23.connectn.ai.chatgptsucks;

import com.thehutgroup.accelerator.connectn.player.Board;
import com.thehutgroup.accelerator.connectn.player.Counter;
import com.thehutgroup.accelerator.connectn.player.InvalidMoveException;
import com.thehutgroup.accelerator.connectn.player.Player;
import com.thg.accelerator23.connectn.ai.chatgptsucks.analysis.BoardAnalyser;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;



public class ConnectForkMCTSv0 extends Player {
  private static Board board;

  public ConnectForkMCTSv0(Counter counter) {
    //TODO: fill in your name here
    super(counter, ConnectForkMCTSv0.class.getName());
  }

  @Override
  public int makeMove(Board board) {
    BoardAnalyser boardAnalyser = new BoardAnalyser(board.getConfig());
    List<Integer> availableMoves = new ArrayList<>();

    return 3;
  }
}




