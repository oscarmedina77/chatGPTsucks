package com.thg.accelerator23.connectn.ai.chatgptsucks;

import com.thehutgroup.accelerator.connectn.player.Board;
import com.thehutgroup.accelerator.connectn.player.Counter;
import com.thehutgroup.accelerator.connectn.player.GameConfig;
import com.thehutgroup.accelerator.connectn.player.InvalidMoveException;

import java.util.ArrayList;
import java.util.Random;

import static java.util.Collections.max;
import static java.util.Objects.isNull;

public class StatePython extends ConnectForkMCTSv1 {
    public Board board;
    public int mark;
    public GameConfig config;
    public ArrayList<StatePython> children;

    public void setParent(StatePython parent) {
        this.parent = parent;
    }
    public void setChildren(ArrayList<StatePython> children) {
        this.children = children;
    }
    public void setNodeTotalScore(Double nodeTotalScore) { this.nodeTotalScore = nodeTotalScore;}
    public void setNodeTotalVisits(int nodeTotalVisits) {this.nodeTotalVisits = nodeTotalVisits;}
    public void setExpandableMoves(ArrayList<Integer> moves) {this.expandableMoves = moves;}
    public StatePython parent;
    public Double nodeTotalScore;
    public int nodeTotalVisits;
    public ArrayList<Integer> availableMoves;
    public ArrayList<Integer> expandableMoves;
    public boolean isTerminal;
    public Double terminalScore;
    public Integer actionTaken;

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
        this.nodeTotalScore = 0.0;
        this.nodeTotalVisits = 0;

        for (int i = 0; i < board.getConfig().getWidth(); i++) {
            try {
                new Board(board, i, this.getCounter());
                availableMoves.add(i);
            } catch (InvalidMoveException e) {
            }
        }
        this.availableMoves = availableMoves;
        this.expandableMoves = new ArrayList<>();
        this.expandableMoves.addAll(availableMoves);
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

    public ArrayList<Integer> getExpandableMoves() {
        return expandableMoves;
    }

    public Boolean getIsTerminal() {
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

        currentState.setChildren(this.getChildren());
        currentState.setNodeTotalScore(this.getNodeTotalScore());
        currentState.setNodeTotalVisits(this.getNodeTotalVisits());

        StatePython newChild = new StatePython(this.getCounter(),
                childBoard,
                this.opponentMark(this.getMark()),
                this.getConfig(),
                currentState,
                finishBool,
                finishScore[1],
                column);

        ArrayList<StatePython> oldChildren = currentState.getChildren();
        ArrayList<StatePython> newChildren = newChild.getChildren();
        ArrayList<StatePython> concatChildren = new ArrayList<>();

        concatChildren.addAll(oldChildren);
        concatChildren.addAll(newChildren);

        this.setChildren(concatChildren);

        try {
            simScore = this.getChildren().get(this.getChildren().size() - 1).simulate();
        }
        catch (Exception e) {
            return;
        }
        this.getChildren().get(this.getChildren().size() - 1).backPropagate(simScore);

        ArrayList<Integer> newMoves = this.getExpandableMoves();
        newMoves.remove(column);
        this.setExpandableMoves(newMoves);
    }

    public StatePython chooseStrongestChild(double Cp) {
        ArrayList<Double> childrenScores = new ArrayList<>();
        Double score = Double.POSITIVE_INFINITY;

        for (StatePython child : this.getChildren()) {
            score = this.uctScore(child.getNodeTotalScore(),
                    child.getNodeTotalVisits(),
                    this.getNodeTotalVisits(),
                    Cp);
            childrenScores.add(score);
        }
        if (score != Double.POSITIVE_INFINITY) {
            double maxScore = max(childrenScores);
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

        if (ntScore != 0.0) {
            double maxScore = max(childrenScores);
            int bestChildIndex = childrenScores.indexOf(maxScore);
            return this.getChildren().get(bestChildIndex);
        }
        else {
//            TODO - should we get here now?
            System.out.println("choosePlayChild skips to ELSE statement");
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
        return this.opponentScore(this.defaultPolicySimulation(this.getBoard(), this.getMark()));
    }

    public void backPropagate(double simulationScore) {
        this.setNodeTotalScore(this.getNodeTotalScore() + simulationScore);
        this.setNodeTotalVisits((this.getNodeTotalVisits() + 1));
        if (!isNull(this.getActionTaken())) {
            this.parent.backPropagate(this.opponentScore(simulationScore));
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
