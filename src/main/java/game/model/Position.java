package game.model;

import java.io.Serializable;
import java.util.Random;

public class Position implements Serializable {

    private static final int EMPTY_CELL = 0;
    public static final int PLAYER = 1;
    public static final int TEAMMATE = 2;
    public static final int ENEMY = 3;
    private static final int ALREADY_MOVED = 4;

    int[][] field;
    boolean isPlayerMove;

    public Position(int[][] field) {
        this.field = field;
        this.isPlayerMove = true;
        initGame();
    }

    private void initGame() {
        field[0][field.length - 1] = TEAMMATE;
        field[field.length - 1][0] = PLAYER;
        for (int i = 0; i < field.length; i++) {
            field[i][i] = ENEMY;
        }
        //field[0][0] = ENEMY;
    }

    public boolean isPlayerMove() {
        return isPlayerMove;
    }

    public void switchPlayerMove() {
        if (findFigure(PLAYER) != null && findFigure(TEAMMATE) != null) {
            this.isPlayerMove = !this.isPlayerMove;
        } else this.isPlayerMove = findFigure(PLAYER) != null;
    }

    public void makeMove(Coordinates from, Coordinates to) {
        int figure = getFigureByCoord(from);
        changeCell(from, EMPTY_CELL);
        changeCell(to, figure);
    }

    public void enemyMove() {
        Random random = new Random();
        for (int i = 0; i < field.length; i++) {
            for (int j = 0; j < field[0].length; j++) {
                if (field[i][j] == ENEMY) {
                    int rotation = random.nextInt(4);
                    int x = i;
                    int y = j;
                    if (rotation == 0) {
                        y--;
                    }
                    if (rotation == 1) {
                        x++;
                    }
                    if (rotation == 2) {
                        y++;
                    }
                    if (rotation == 3) {
                        x--;
                    }

                    if (x >= 0 && x <= field.length - 1 && y >= 0 && y <= field.length - 1) {
                        if (field[x][y] == EMPTY_CELL || field[x][y] == PLAYER || field[x][y] == TEAMMATE)
                            makeMove(new Coordinates(i, j), new Coordinates(x, y));
                        changeCell(new Coordinates(x, y), ALREADY_MOVED);
                    }
                }
            }
        }

        for (int i = 0; i < field.length; i++) {
            for (int j = 0; j < field[0].length; j++) {
                if (field[i][j] == ALREADY_MOVED)
                    changeCell(new Coordinates(i, j), ENEMY);
            }
        }
        if (findFigure(PLAYER) == null || findFigure(TEAMMATE) == null) {
            this.isPlayerMove = findFigure(PLAYER) != null;
        }
    }

    public void print() {
        for (int i = 0; i < field.length; i++) {
            for (int j = 0; j < field[0].length; j++) {
                System.out.print(field[i][j] + " ");
            }
            System.out.println();
        }
    }

    public int getFigureByCoord(Coordinates coordinates) {
        return field[coordinates.getX()][coordinates.getY()];
    }

    public int getFigureByCoord(int x, int y) {
        return field[x][y];
    }

    private void changeCell(Coordinates coordinates, int switchOn) {
        field[coordinates.getX()][coordinates.getY()] = switchOn;
    }

    private void changeCell(int x, int y, int switchOn) {
        field[x][y] = switchOn;
    }

    private boolean isHumanFigure(Coordinates coordinates) {
        return getFigureByCoord(coordinates) == PLAYER || getFigureByCoord(coordinates) == TEAMMATE;
    }

    public void shootUp(Coordinates coordinates) {
        if (isHumanFigure(coordinates)) {
            for (int i = coordinates.getX(); i >= 0; i--) {
                if (field[i][coordinates.getY()] == ENEMY) {
                    changeCell(i, coordinates.getY(), EMPTY_CELL);
                    break;
                }
            }
        }
    }

    public void shootDown(Coordinates coordinates) {
        if (isHumanFigure(coordinates)) {
            for (int i = coordinates.getX(); i <= 7; i++) {
                if (field[i][coordinates.getY()] == ENEMY) {
                    changeCell(i, coordinates.getY(), EMPTY_CELL);
                    break;
                }
            }
        }
    }

    public void shootRight(Coordinates coordinates) {
        if (isHumanFigure(coordinates)) {
            for (int i = coordinates.getY(); i <= 7; i++) {
                if (field[coordinates.getX()][i] == ENEMY) {
                    changeCell(coordinates.getX(), i, EMPTY_CELL);
                    break;
                }
            }
        }
    }

    public void shootLeft(Coordinates coordinates) {
        if (isHumanFigure(coordinates)) {
            for (int i = coordinates.getY(); i >= 0; i--) {
                if (field[coordinates.getX()][i] == ENEMY) {
                    changeCell(coordinates.getX(), i, EMPTY_CELL);
                    break;
                }
            }
        }
    }

    public boolean isHumanWin() {
        for (int i = 0; i < field.length; i++) {
            for (int j = 0; j < field[i].length; j++) {
                if (getFigureByCoord(i, j) == ENEMY)
                    return false;
            }
        }
        return true;
    }

    public boolean isEnemyWin() {
        for (int i = 0; i < field.length; i++) {
            for (int j = 0; j < field[i].length; j++) {
                if (getFigureByCoord(i, j) == PLAYER || getFigureByCoord(i, j) == TEAMMATE)
                    return false;
            }
        }
        return true;
    }

    public Coordinates findFigure(int figure) {
        for (int i = 0; i < field.length; i++) {
            for (int j = 0; j < field[i].length; j++) {
                if (getFigureByCoord(i, j) == figure)
                    return new Coordinates(i, j);
            }
        }
        return null;
    }
}
