package com.thg.accelerator23.connectn.ai.chatgptsucks;
import java.util.Random;
import java.util.random.*;

import com.thehutgroup.accelerator.connectn.player.Board;
import com.thehutgroup.accelerator.connectn.player.Counter;
import com.thehutgroup.accelerator.connectn.player.Player;


public class ConnectFork extends Player {
  public ConnectFork(Counter counter) {
    //TODO: fill in your name here
    super(counter, ConnectFork.class.getName());
  }

  @Override
  public int makeMove(Board board) {
    int move;
    int width = 10;
    Random rand = new Random();
    move = rand.nextInt(width);
    return move;
  }
}
