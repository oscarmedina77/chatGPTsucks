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

public class StatePython extends ConnectForkMCTSv1 {
    private Board board;
    private int mark;
    private GameConfig config;
    public ArrayList<StatePython> children;

    public void setParent(StatePython parent) {
        this.parent = parent;
    }

    public StatePython parent;
    public double nodeTotalScore;
    public int nodeTotalVisits;
    private final ArrayList<Integer> availableMoves;
    public final ArrayList<Integer> expandableMoves;
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
        this.board = board;
        this.mark = mark;
        this.config = config;
        this.children = new ArrayList<>();
        this.parent = parent;
        this.nodeTotalScore = 0;
        this.nodeTotalVisits = 0;

        for (int i = 0; i < board.getConfig().getWidth(); i++) {
            try {
                new Board(board, i, this.getCounter());
                availableMoves.add(i);
            } catch (InvalidMoveException e) {
            }
        }
        this.availableMoves = availableMoves;
//        this.availableMoves = getAvailableMoves();
        this.expandableMoves = availableMoves;
        this.isTerminal = isTerminal;
        this.terminalScore = terminalScore;
        this.actionTaken = actionTaken;
    }

    public Board getBoard() {
        return this.board;
    }

    public int getMark() {
        return this.mark;
    }

    public GameConfig getConfig() {
        return this.config;
    }

    public ArrayList<StatePython> getChildren() {
        return this.children;
    }

    public StatePython getParent() {
        return this.parent;
    }

    public double getNodeTotalScore() {
        return this.nodeTotalScore;
    }

    public int getNodeTotalVisits() {
        return this.nodeTotalVisits;
    }

//    public ArrayList<Integer> getAvailableMoves() {
//        return availableMoves;
//    }

    public ArrayList<Integer> getExpandableMoves() {
        return this.expandableMoves;
    }

    public Boolean getIsTerminal() {
        System.out.println("getIsTerminal returns " + this.isTerminal);
        return this.isTerminal;
    }

    public Double getTerminalScore() {
        System.out.println("getTerminalScore returns " + this.terminalScore);
        return this.terminalScore;
    }

    public Integer getActionTaken() {
        System.out.println("getActionTaken returns " + this.actionTaken);
        return this.actionTaken;
    }

    public boolean isExpandable() {
        try {
            this.getExpandableMoves().size();
        } catch (NullPointerException nullPointerException) {
            System.out.println("isExpandable found exception");
            return false;
        }

//        TODO can we just remove second condition?
        return !this.getIsTerminal() && this.getExpandableMoves().size() > 0;
    }

    public void expandSimulateChild() {
        Random rand = new Random();
        int randIndex = rand.nextInt(0, this.getExpandableMoves().size());
        int column = this.getExpandableMoves().get(randIndex);
        double simScore = 0.0;

//     TODO  board as copy again? - OK?
        Board childBoard;
        try {
//            childBoard = new Board(this.getParent().getBoard(), this.getActionTaken(), this.getCounter());
            childBoard = this.getBoard();
        } catch (Exception e) {
            System.out.println("expandSimulateChild found exception");
            return;
        }

//      TODO might get us stuck - OK?
        System.out.println("exMoves size =  " + this.getExpandableMoves().size());
//        Board childBoardSim = makeMoveInSim(childBoard, mark);
        Board childBoardSim = makeMoveAltRandom(this.getBoard());

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

        double[] finishScore = checkFinishAndScore(childBoardSim, column, this.getMark(), this.getConfig());

        boolean finishBool;
        finishBool = finishScore[0] == 0;

        StatePython currentState = new StatePython(getCounter(),
                this.getBoard(),
                this.getMark(),
                this.getConfig(),
                this.getParent(),
                this.getIsTerminal(),
                this.getTerminalScore(),
                this.getActionTaken());

        StatePython newChild = new StatePython(getCounter(),
                childBoard,
                opponentMark(this.getMark()),
                this.getConfig(),
                currentState,
                finishBool,
                finishScore[1],
                this.getActionTaken());

        ArrayList<StatePython> newChildren = newChild.getChildren();
        this.children.addAll(newChildren);

        try {
            simScore = this.getChildren().get(this.getChildren().size() - 1).simulate();
        }
        catch (Exception e) {
            return;
        }

        this.children.get(this.getChildren().size() - 1).backPropagate(simScore);
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
            return this.getChildren().get(bestChildIndex);
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
            return this.getChildren().get(bestChildIndex);
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
        if (this.getIsTerminal()) {
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
        if (this.getIsTerminal()) {
            return this.getTerminalScore();
        }
//        TODO - leave out?
//        this.gameState = new GameState(winner);
        return opponentScore(defaultPolicySimulation(this.getBoard(), this.getMark(), this.getConfig(), new GameState(this.getCounter())));
    }

    public void backPropagate(double simulationScore) {
        this.nodeTotalScore += simulationScore;
        this.nodeTotalVisits += 1;
        if (!isNull(this.getActionTaken())) {
            this.parent.backPropagate(opponentScore(simulationScore));
        }
    }

    public StatePython chooseChildViaAction(int action) {
        for (StatePython child : this.getChildren()) {
            if (child.getActionTaken() == action) {
                return child;
            }
        }
//        TODO - return this.parent or null ?
        return this.getParent();
//        return null;
    }
}
