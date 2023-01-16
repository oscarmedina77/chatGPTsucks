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
    public void setBoard(Board board) {this.board = board;}

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
                       int actionTaken)
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

    public int getActionTaken() {
        return actionTaken;
    }

    public boolean isExpandable(StatePython currentState) {
        try {
            currentState.getExpandableMoves().size();
        } catch (NullPointerException nullPointerException) {
            System.out.println("isExpandable found exception");
            return false;
        }

//        TODO can we just remove second condition?
        return !currentState.getIsTerminal() && currentState.getExpandableMoves().size() > 0;
    }

    public void expandSimulateChild(StatePython currentState) {
        Random rand = new Random();
        int randIndex = rand.nextInt(0, currentState.getExpandableMoves().size());
        int column = currentState.getExpandableMoves().get(randIndex);
        double simScore;

        Board childBoard;
        try {
            childBoard = new Board(currentState.getBoard(), column, currentState.getCounter());
        } catch (Exception e) {
            System.out.println("expandSimulateChild found exception");
            return;
        }

        double[] finishScore = checkFinishAndScore(currentState);

        boolean finishBool;
        finishBool = (finishScore[0] == 1);

        StatePython newChild = new StatePython(currentState.getCounter(),
                childBoard,
                opponentMark(currentState.getMark()),
                currentState.getConfig(),
                currentState,
                finishBool,
                finishScore[1],
                column);

        ArrayList<StatePython> oldChildren = currentState.getChildren();
        ArrayList<StatePython> newChildren = newChild.getChildren();
        ArrayList<StatePython> concatChildren = new ArrayList<>();

        concatChildren.addAll(oldChildren);
        concatChildren.addAll(newChildren);

        StatePython oldState = currentState;
        currentState.setChildren(concatChildren);

        try {
            simScore = currentState.getChildren().get(currentState.getChildren().size() - 1).simulate(oldState);
        }
        catch (Exception e) {
            return;
        }
        this.getChildren().get(currentState.getChildren().size() - 1).backPropagate(simScore, currentState);

        ArrayList<Integer> newMoves = currentState.getExpandableMoves();
        newMoves.remove(column);
        this.setExpandableMoves(newMoves);
    }

    public StatePython chooseStrongestChild(double Cp, StatePython currentState) {
        ArrayList<Double> childrenScores = new ArrayList<>();
        Double score = Double.POSITIVE_INFINITY;

        for (StatePython child : currentState.getChildren()) {
            score = uctScore(child.getNodeTotalScore(),
                    child.getNodeTotalVisits(),
                    currentState.getNodeTotalVisits(),
                    Cp);
            childrenScores.add(score);
        }
        if (score != Double.POSITIVE_INFINITY) {
            double maxScore = max(childrenScores);
            int bestChildIndex = childrenScores.indexOf(maxScore);
//            getChildren() ?
            return currentState.getChildren().get(bestChildIndex);
        }

        else {
//            TODO - successive fixes should now make else statement redundant?
            System.out.println("chooseStrongestChild returns parent");
            return currentState.getParent();
        }
    }

    public StatePython choosePlayChild(StatePython currentState) {
        ArrayList<Double> childrenScores = new ArrayList<>();
        Double ntScore = 0.0;

        for (StatePython child : currentState.getChildren()) {
            ntScore = child.getNodeTotalScore();
            System.out.println("child nodeTotalScore = " + ntScore);
            childrenScores.add(ntScore);
        }

        if (ntScore != 0.0) {
            double maxScore = max(childrenScores);
            int bestChildIndex = childrenScores.indexOf(maxScore);
            return currentState.getChildren().get(bestChildIndex);
        }
        else {
//            TODO - should we get here now?
            System.out.println("choosePlayChild skips to ELSE statement");
            try {
                return currentState.getParent();
            }
            catch (NullPointerException e) {
                return null;
            }
        }
    }

    public void treeSingleRun(StatePython currentState) {
        if (currentState.getIsTerminal()) {
            currentState.backPropagate(currentState.getTerminalScore(), currentState);
            return;
        }
        if (isExpandable(currentState)) {
            expandSimulateChild(currentState);
            return;
        }

        this.chooseStrongestChild(Cp_default, currentState).treeSingleRun(currentState);
    }

    public double simulate(StatePython currentState) {
        if (currentState.getIsTerminal()) {
            return currentState.getTerminalScore();
        }
        return opponentScore(currentState.defaultPolicySimulation(currentState.getBoard(), currentState.getMark(), currentState));
    }

    public void backPropagate(double simulationScore, StatePython currentState) {
        currentState.setNodeTotalScore(currentState.getNodeTotalScore() + simulationScore);
        currentState.setNodeTotalVisits((currentState.getNodeTotalVisits() + 1));
        if (!isNull(currentState.getActionTaken())) {
            currentState.parent.backPropagate(currentState.opponentScore(simulationScore), currentState);
        }
    }

    public StatePython chooseChildViaAction(int action, StatePython currentState) {
        for (StatePython child : currentState.getChildren()) {
            if (child.getActionTaken() == action) {
                return child;
            }
        }
//        TODO - return this.parent or null?
        return currentState.getParent();
//        return null;
    }
}
