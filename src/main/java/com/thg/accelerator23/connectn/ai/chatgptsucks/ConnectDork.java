package com.thg.accelerator23.connectn.ai.chatgptsucks;
import java.util.ArrayList;
import java.util.List;
import java.util.ListResourceBundle;
import java.util.Random;
import java.util.random.*;

import com.thehutgroup.accelerator.connectn.player.Board;
import com.thehutgroup.accelerator.connectn.player.Counter;
import com.thehutgroup.accelerator.connectn.player.InvalidMoveException;
import com.thehutgroup.accelerator.connectn.player.Player;
import com.thg.accelerator23.connectn.ai.chatgptsucks.analysis.BoardAnalyser;


public class ConnectDork extends Player {
    public ConnectDork(Counter counter) {
        //TODO: fill in your name here
        super(counter, ConnectDork.class.getName());
    }

    @Override
    public int makeMove(Board board) {
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
}