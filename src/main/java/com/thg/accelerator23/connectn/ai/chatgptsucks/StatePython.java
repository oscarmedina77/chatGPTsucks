package com.thg.accelerator23.connectn.ai.chatgptsucks;

import com.thehutgroup.accelerator.connectn.player.Board;
import com.thehutgroup.accelerator.connectn.player.GameConfig;
import com.thehutgroup.accelerator.connectn.player.Position;
import com.thg.accelerator23.connectn.ai.chatgptsucks.analysis.BoardAnalyser;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

public class StatePython {
    private Board board;
    private int mark;
    private GameConfig config;
    private ArrayList<Objects> children;
    private ArrayList<Objects> parent;
    private int nodeTotalScore;
    private int nodeTotalVisits;
    private ArrayList<Integer> availableMoves;
    private ArrayList<Integer> expandableMoves;
    private boolean isTerminal;
    private int terminalScore;
    private int actionTaken;
    private Position position;

    public StatePython(Board board,
                       int mark,
                       GameConfig config,
                       ArrayList<Objects> parent,
                       boolean isTerminal,
                       int terminalScore,
                       int actionTaken,
                       Position position) {

        BoardAnalyser BoardAnalyser = new BoardAnalyser(config);

//        Does board need to be copied like in the Python script?
        this.board = board;
        this.mark = mark;
        this.config = config;
        this.children = new ArrayList<>();
        this.parent = parent;
        this.nodeTotalScore = 0;
        this.nodeTotalVisits = 0;
        this.availableMoves = BoardAnalyser.validPositions(board, position);
        this.expandableMoves = (ArrayList<Integer>)availableMoves.clone();
        this.isTerminal = isTerminal;
        this.terminalScore = terminalScore;
        this.actionTaken = actionTaken;
    }

    public boolean isExpandable() {
        return !isTerminal && expandableMoves.size() > 0;
    }

    public void expandSimulateChild() {
        Random rand = new Random();
        int randIndex = rand.nextInt(expandableMoves.size());
        int column = expandableMoves.get(randIndex);

//       board as copy again?
        Board childBoard = board;
    }
}
