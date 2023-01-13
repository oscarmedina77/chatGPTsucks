package com.thg.accelerator23.connectn.ai.chatgptsucks;

import com.thehutgroup.accelerator.connectn.player.Board;
import com.thehutgroup.accelerator.connectn.player.Counter;
import com.thehutgroup.accelerator.connectn.player.GameConfig;
import com.thehutgroup.accelerator.connectn.player.InvalidMoveException;
import com.thg.accelerator23.connectn.ai.chatgptsucks.analysis.GameState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import static java.util.Objects.isNull;

public class StatePython extends ConnectForkMCTSv0 {
    private Board board;
    private int mark;
    private GameConfig config;
    private ArrayList<StatePython> children;

    public void setParent(StatePython parent) {
        this.parent = parent;
    }

    private StatePython parent;
    private double nodeTotalScore;
    private int nodeTotalVisits;
    private final ArrayList<Integer> availableMoves;
    private final ArrayList<Integer> expandableMoves;
    private final boolean isTerminal;
    private final Double terminalScore;
    private final Integer actionTaken;

    public StatePython(Counter counter,
                       Board board,
                       int mark,
                       GameConfig config,
                       StatePython parent,
                       Boolean isTerminal,
                       Double terminalScore,
                       Integer actionTaken)
        {
        super(counter);

//        BoardAnalyser boardAnalyser = new BoardAnalyser(config);
        ArrayList<Integer> availableMoves = new ArrayList<>();

//        Does board need to be copied like in the Python script?
        this.board = getBoard();
        this.mark = mark;
        this.config = config;
        this.children = new ArrayList<>();
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
//        this.availableMoves = getAvailableMoves();
        this.expandableMoves = this.availableMoves;
        this.isTerminal = isTerminal;
        this.terminalScore = terminalScore;
        this.actionTaken = actionTaken;
    }

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

//    public ArrayList<Integer> getAvailableMoves() {
//        return availableMoves;
//    }

    public ArrayList<Integer> getExpandableMoves() {
        return expandableMoves;
    }

    public Boolean isTerminal() {
        return isTerminal;
    }

    public Double getTerminalScore() {
        return terminalScore;
    }

    public Integer getActionTaken() {
        return actionTaken;
    }

    public boolean isExpandable() {
        try {
            this.getExpandableMoves().size();
        } catch (NullPointerException nullPointerException) {
            return false;
        }

//        TODO can we just remove second condition?
        return !this.isTerminal && this.getExpandableMoves().size() > 0;
    }

    public void expandSimulateChild() {
        Random rand = new Random();
        int randIndex = rand.nextInt(0, this.expandableMoves.size());
        int column = this.getExpandableMoves().get(randIndex);

//     TODO  board as copy again? - OK?
        Board childBoard = null;
        try {
            childBoard = new Board(this.board, this.getActionTaken(), this.getCounter());
        } catch (Exception e) {
            return;
        }

//      TODO might get us stuck - OK?
        System.out.println("exMoves size =  " + this.expandableMoves.size());
        Board childBoardSim = makeMoveInSim(childBoard, mark);

//        TODO - now covered by returning board above?
//        availableMoves.add(moveSim);
//        int moveSimValid = availableMoves.get(new Random().nextInt(availableMoves.size()));
//
//        Board childBoardSim = null;
//        try {
//            childBoardSim = new Board(childBoard, moveSimValid, this.getCounter());
//        } catch (InvalidMoveException e) {
//            return;
//        }

        double[] finishScore = checkFinishAndScore(childBoardSim, column, this.mark, this.getConfig());

        boolean finishBool;
        finishBool = finishScore[0] == 0;

        StatePython currentState = new StatePython(getCounter(),
                this.getBoard(),
                mark,
                config,
                parent,
                isTerminal,
                terminalScore,
                actionTaken);

        StatePython newChild = new StatePython(getCounter(),
                childBoard,
                opponentMark(mark),
                config,
                currentState,
                finishBool,
                finishScore[1],
                actionTaken);

        ArrayList<StatePython> newChildren = newChild.getChildren();
        this.children.addAll(newChildren);

        double simScore = this.children.get(this.children.size() - 1).simulate();
        this.children.get(this.children.size() - 1).backPropagate(simScore);

        this.expandableMoves.remove(column);
    }

    public StatePython chooseStrongestChild(double Cp) {
        ArrayList<Double> childrenScores = new ArrayList<>();
        Double score = 0.0;

        for (StatePython child : this.getChildren()) {
            score = uctScore(child.getNodeTotalScore(),
                    child.getNodeTotalVisits(),
                    this.getNodeTotalVisits(),
                    Cp);
            childrenScores.add(score);
        }
        if (score != 0.0) {
            double maxScore = Collections.max(childrenScores);
            int bestChildIndex = childrenScores.indexOf(maxScore);
//            getChildren() ?
            return this.children.get(bestChildIndex);
        }

//       TODO - will this force the MC to go back up a level and try again?
//            - or are we stuck in a loop somewhere else?

        else {
            return this.getParent();
        }
    }

    public StatePython choosePlayChild() {
        ArrayList<Double> childrenScores = new ArrayList<>();
        Double ntScore = 0.0;

        for (StatePython child : this.getChildren()) {
            ntScore = child.getNodeTotalScore();
            System.out.println("child nodeTotalScore = " + ntScore);
            childrenScores.add(ntScore);
        }

        if (ntScore != 0) {
            double maxScore = Collections.max(childrenScores);
            int bestChildIndex = childrenScores.indexOf(maxScore);
//             getChildren() ?
            return this.children.get(bestChildIndex);
        }
        else {
            try {
                return this.getParent();
            }
            catch (NullPointerException e) {
                return null;
            }
        }
    }

    public void treeSingleRun() {
        if (this.isTerminal()) {
            this.backPropagate(this.getTerminalScore());
//            System.out.println("stuck Terminal");
            return;
        }
        if (this.isExpandable()) {
            this.expandSimulateChild();
//            System.out.println("stuck Expandable");
            return;
        }
//        System.out.println("stuck at end");
//        System.out.println("isTerminal = " + isTerminal());
//        System.out.println("isExpandable = " + isExpandable());
        this.chooseStrongestChild(Cp_default).treeSingleRun();
    }

    public double simulate() {
        if (this.isTerminal()) {
            return this.terminalScore;
        }
//        TODO - leave out?
//        this.gameState = new GameState(winner);
        return opponentScore(defaultPolicySimulation(this.board, this.mark, this.config, new GameState(this.getCounter())));
    }

    public void backPropagate(double simulationScore) {
        this.nodeTotalScore += simulationScore;
        this.nodeTotalVisits += 1;
        if (!isNull(this.parent)) {
            this.parent.backPropagate(opponentScore(simulationScore));
        }
    }

    public StatePython chooseChildViaAction(int action) {
        for (StatePython child : this.children) {
            if (child.actionTaken == action) {
                return child;
            }
        }
//        TODO - return this.parent or null ?
        return this.getParent();
//        return null;
    }
}
