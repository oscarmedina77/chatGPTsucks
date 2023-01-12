package com.thg.accelerator23.connectn.ai.chatgptsucks;

import com.thehutgroup.accelerator.connectn.player.*;
import com.thg.accelerator23.connectn.ai.chatgptsucks.analysis.BoardAnalyser;

import javax.lang.model.type.NullType;
import java.util.*;

import static java.util.Objects.isNull;

public class StatePython extends ConnectForkMCTSv0 {
    private Board board;
    private int mark;
    private GameConfig config;
    private ArrayList<StatePython> children;
    private StatePython parent;
    private double nodeTotalScore;

    public Board getBoard() {
        return board;
    }

    public int getMark() {
        return mark;
    }

    public GameConfig getConfig() {
        return config;
    }

    public ArrayList<StatePython> getChildren() {
        return children;
    }

    public StatePython getParent() {
        return parent;
    }

    public double getNodeTotalScore() {
        return nodeTotalScore;
    }

    public int getNodeTotalVisits() {
        return nodeTotalVisits;
    }

    public ArrayList<Integer> getAvailableMoves() {
        return availableMoves;
    }

    public ArrayList<Integer> getExpandableMoves() {
        return expandableMoves;
    }

    public boolean isTerminal() {
        return isTerminal;
    }

    public int getTerminalScore() {
        return terminalScore;
    }

    public int getActionTaken() {
        return actionTaken;
    }

    private int nodeTotalVisits;
    private ArrayList<Integer> availableMoves;
    private ArrayList<Integer> expandableMoves;
    private boolean isTerminal;
    private Integer terminalScore;
    private Integer actionTaken;

    public StatePython(Counter counter,
                       Board board,
                       int mark,
                       GameConfig config,
                       StatePython parent,
                       boolean isTerminal,
                       Integer terminalScore,
                       Integer actionTaken)
        {
        super(counter);

        BoardAnalyser boardAnalyser = new BoardAnalyser(config);
        ArrayList<Integer> availableMoves = new ArrayList<>();

//        Does board need to be copied like in the Python script?
        this.board = board;
        this.mark = mark;
        this.config = config;
        this.children = new ArrayList<StatePython>();
        this.parent = parent;
        this.nodeTotalScore = 0;
        this.nodeTotalVisits = 0;

        for (int i = 0; i< board.getConfig().getWidth(); i ++) {
            try {
                new Board(board, i, this.getCounter());
                availableMoves.add(i);
            } catch (InvalidMoveException e) {
            }
        }
        this.availableMoves = availableMoves;

        this.expandableMoves = (ArrayList<Integer>)availableMoves.clone();
        this.isTerminal = isTerminal;
        this.terminalScore = terminalScore;
        this.actionTaken = actionTaken;
    }

    public boolean isExpandable() {
        return !this.isTerminal && this.expandableMoves.size() > 0;
    }

    public void expandSimulateChild() {
        Random rand = new Random();
        int randIndex = rand.nextInt(this.expandableMoves.size());
        int column = this.expandableMoves.get(randIndex);

//       board as copy again?
        Board childBoard = this.board;

//        might get us stuck...


//        check finish and score function here

        StatePython currentState = new StatePython(getCounter(),
                board,
                mark,
                config,
                parent,
                isTerminal,
                terminalScore,
                actionTaken);

        StatePython newState1 = new StatePython(getCounter(),
                board,
                mark,
                config,
                currentState,
                isTerminal,
                terminalScore,
                actionTaken);

        StatePython newState2 = new StatePython(getCounter(),
                childBoard,
//                mark needs to be opponentMark(mark) from a function added later
                mark,
                config,
                newState1,
                isTerminal,
                terminalScore,
                actionTaken);

        ArrayList<StatePython> newChildren = newState2.getChildren();
        this.children.addAll(newChildren);

        this.expandableMoves.remove(column);
    }

    public StatePython chooseStrongestChild(double Cp) {
//        ArrayList<Double> childrenScores = uctScore(...) - ADD LATER
//        double maxScore = Collections.max(childrenScores);
//        int bestChildIndex = childrenScores.indexOf(maxScore);
//        return this.children.get(bestChildIndex);
        return this.children.get(0);
    }

    public StatePython choosePlayChild() {
        ArrayList<Double> childrenScores = new ArrayList<>();

        for (StatePython child : this.children) {
            childrenScores.add(child.nodeTotalScore);
        }

        double maxScore = Collections.max(childrenScores);
        int bestChildIndex = childrenScores.indexOf(maxScore);
        return this.children.get(bestChildIndex);
    }

    public Object treeSingleRun() {
        if (this.isTerminal) {
//            this.backpropagate(this.terminalScore); ADD
            return null;
        }
//        if (this.isExpandable()) {
//            this.expandSimulateChild()
//            return null;
//        }
//        this.chooseStrongestChild(Cp_default).treeSingleRun(); where does Cp default come in?
        return null;
    }

    public double simulate() {
        if (this.isTerminal) {
            return this.terminalScore;
        }
//        return - ADD opponentScore function
        return 0.0;
    }

    public void backPropagate(double simulationScore) {
        this.nodeTotalScore += simulationScore;
        this.nodeTotalVisits += 1;
        if (!isNull(this.parent)) {
//            this.parent.backPropagate(*function*); opponentScore
        }
    }

    public StatePython chooseChildViaAction(int action) {
        for (StatePython child : this.children) {
            if (child.actionTaken == action) {
                return child;
            }
        }
        return null;
    }
}
