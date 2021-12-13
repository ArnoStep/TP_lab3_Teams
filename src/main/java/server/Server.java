package server;

import com.google.gson.Gson;
import game.model.Position;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static final int PORT = 255;
    private static final int MAX_CONNECTIONS = 2;
    private static ObjectOutputStream playerOut;
    private static ObjectInputStream playerIn;
    private static ObjectOutputStream teammateOut;
    private static ObjectInputStream teammateIn;


    public static void main(String[] args) throws IOException {
        Gson gson = new Gson();
        try (ServerSocket serverSocket = new ServerSocket(PORT, MAX_CONNECTIONS)) {
            Socket player = serverSocket.accept();
            playerOut = new ObjectOutputStream(player.getOutputStream());
            playerIn = new ObjectInputStream(player.getInputStream());

            System.out.println("New connection: " + player.getInetAddress());
            System.out.println("Waiting second player");

            Socket teammate = serverSocket.accept();
            teammateOut = new ObjectOutputStream(teammate.getOutputStream());
            teammateIn = new ObjectInputStream(teammate.getInputStream());

            System.out.println("New connection: " + teammate.getInetAddress());
            System.out.println("Ready to init game");

            Position position = new Position(new int[8][8]);
            playerOut.writeObject(gson.toJson(new ServerMessage(0,"You're figure is 1, go first")));
            playerOut.writeBoolean(true);
            playerOut.writeObject(gson.toJson(position));
            playerOut.flush();
            teammateOut.writeObject(gson.toJson(new ServerMessage(0,"You're figure is 2, wait first player turn")));
            teammateOut.writeBoolean(false);
            teammateOut.writeObject(gson.toJson(position));
            //System.out.println(gson.toJson(position));
            teammateOut.flush();

            try {
                do {
                    position = gson.fromJson(playerIn.readObject().toString(), Position.class);
                    System.out.println(position.isPlayerMove() ? "Teammate:" : "Player:");
                    position.print();
                    teammateOut.writeObject(gson.toJson(position));
                    teammateOut.flush();
                    position = gson.fromJson(teammateIn.readObject().toString(), Position.class);
                    System.out.println(position.isPlayerMove() ? "Teammate:" : "Player:");
                    position.print();
                    playerOut.writeObject(gson.toJson(position));
                    playerOut.flush();
                } while (!position.isEnemyWin() || !position.isHumanWin());
            } catch (IOException e) {
                System.out.println("Connection refused");
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
