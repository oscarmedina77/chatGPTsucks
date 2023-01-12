package com.thg.accelerator23.connectn.ai.chatgptsucks;

import com.thehutgroup.accelerator.connectn.player.Board;
import com.thehutgroup.accelerator.connectn.player.Counter;
import com.thehutgroup.accelerator.connectn.player.InvalidMoveException;
import com.thehutgroup.accelerator.connectn.player.Player;
import com.thg.accelerator23.connectn.ai.chatgptsucks.analysis.BoardAnalyser;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class ConnectFork extends Player {
  public ConnectFork(Counter counter) {
    //TODO: fill in your name here
    super(counter, ConnectFork.class.getName());
  }

  @Override
  public int makeMove(Board board) {
    BoardAnalyser boardAnalyser = new BoardAnalyser(board.getConfig());
    List<Integer> availableMoves =  new ArrayList<>();

    if (boardAnalyser.winningPositionAvailable(this.getCounter(), board)) {
      return boardAnalyser.winningPosition(this.getCounter(), board);
    } else if (boardAnalyser.winningPositionAvailable(this.getCounter().getOther(), board)) {
      return boardAnalyser.winningPosition(this.getCounter().getOther(), board);
    } else {
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
  }



