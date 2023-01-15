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

        ArrayList<Integer> availableMoves = new ArrayList<>();

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

    public ArrayList<Integer> getExpandableMoves() {
        return this.expandableMoves;
    }

    public Boolean getIsTerminal() {
        return this.isTerminal;
    }

    public Double getTerminalScore() {
        return this.terminalScore;
    }

    public Integer getActionTaken() {
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
        double simScore;

        Board childBoard;
        try {
            childBoard = new Board(this.getBoard(), column, this.getCounter());
        } catch (Exception e) {
            System.out.println("expandSimulateChild found exception");
            return;
        }

        double[] finishScore = this.checkFinishAndScore();

        boolean finishBool;
        finishBool = (finishScore[0] == 1);

        StatePython currentState = new StatePython(this.getCounter(),
                this.getBoard(),
                this.getMark(),
                this.getConfig(),
                this.getParent(),
                this.getIsTerminal(),
                this.getTerminalScore(),
                this.getActionTaken());

        StatePython newChild = new StatePython(this.getCounter(),
                childBoard,
                opponentMark(this.getMark()),
                this.getConfig(),
                currentState,
                finishBool,
                finishScore[1],
                column);

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

        else {
//            TODO - successive fixes should now make else statement redundant?
            System.out.println("chooseStrongestChild returns parent");
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
            return;
        }
        if (this.isExpandable()) {
            this.expandSimulateChild();
            return;
        }

        this.chooseStrongestChild(Cp_default).treeSingleRun();
    }

    public double simulate() {
        if (this.getIsTerminal()) {
            return this.getTerminalScore();
        }
        return opponentScore(this.defaultPolicySimulation(this.getBoard(), this.getMark()));
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
//        TODO - return this.parent or null?
        return this.getParent();
//        return null;
    }
}
