package client;

import com.google.gson.Gson;
import game.model.Coordinates;
import game.model.Position;
import server.ServerMessage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static final int PORT = 255;
    private static final String IP = "localhost";


    public static void main(String[] args) {
        Gson gson = new Gson();
        try (Socket socket = new Socket(IP, PORT);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            System.out.println(gson.fromJson(in.readObject().toString(), ServerMessage.class).getData());
            boolean isPlayer = in.readBoolean();
            if (isPlayer) {
                Scanner scanner = new Scanner(System.in);
                System.out.println("New game or load?(N/L)");
                String loadInfo = scanner.nextLine();
                boolean isLoad = loadInfo.toLowerCase().equals("l");
                out.writeBoolean(isLoad);
                out.flush();
            }
            Position position;
            do {
                position = gson.fromJson(in.readObject().toString(), Position.class);
                if (position.isHumanWin() || position.isHumanWin())
                    break;
                if (position.findFigure(Position.PLAYER) == null && isPlayer ||
                    position.findFigure(Position.TEAMMATE) == null && !isPlayer) {
                    position.enemyMove();
                    out.writeObject(gson.toJson(position));
                    out.flush();
                    continue;
                }
                position.print();
                if (isPlayer == position.isPlayerMove()) {
                    makePlayerMove(position);
                    position.enemyMove();
                    out.writeObject(gson.toJson(position));
                    out.flush();
                }
                System.out.println();
            } while (!(position.isHumanWin() || position.isHumanWin()));
            System.out.println(position.isHumanWin() ? "Win" : "Fail");
            position.print();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void makePlayerMove(Position position) {
        Scanner scanner = new Scanner(System.in);
        Scanner coordScanner;
        while (true) {
            String turn = scanner.nextLine();
            int currentPlayer = position.isPlayerMove() ? Position.PLAYER : Position.TEAMMATE;
            Coordinates currentPos = position.isPlayerMove() ? position.findFigure(Position.PLAYER) : position.findFigure(Position.TEAMMATE);
            switch (turn.toLowerCase()) {
                case "u":
                    position.shootUp(currentPos);
                    position.switchPlayerMove();
                    return;
                case "d":
                    position.shootDown(currentPos);
                    position.switchPlayerMove();
                    return;
                case "l":
                    position.shootLeft(currentPos);
                    position.switchPlayerMove();
                    return;
                case "r":
                    position.shootRight(currentPos);
                    position.switchPlayerMove();
                    return;
            }
            if (!turn.matches("[0-7]\\s+[0-7]")) {
                System.out.println("Incorrect move, try again");
                continue;
            }
            coordScanner = new Scanner(turn);
            int x = coordScanner.nextInt();
            int y = coordScanner.nextInt();
            if (position.getFigureByCoord(x, y) == Position.ENEMY || (x != currentPos.getX() && y != currentPos.getY())) {
                System.out.println("Incorrect move, try again");
                continue;
            }
            position.makeMove(currentPos, new Coordinates(x, y));
            position.switchPlayerMove();
            return;
        }
    }
}