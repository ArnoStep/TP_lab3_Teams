package server;

import com.google.gson.Gson;
import game.model.Position;
import net.jimblackler.jsonschemafriend.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Server {
    private static final int PORT = 255;
    private static final int MAX_CONNECTIONS = 2;
    private static ObjectOutputStream playerOut;
    private static ObjectInputStream playerIn;
    private static ObjectOutputStream teammateOut;
    private static ObjectInputStream teammateIn;
    private static final String SAVE_FILE_PATH = "C:\\Work and projects\\JavaProjects\\TP_lab2_Teams\\src\\main\\resources\\save.txt";


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

            Position position;
            playerOut.writeObject(gson.toJson(new ServerMessage(0, "You're figure is 1, go first")));
            playerOut.writeBoolean(true);
            playerOut.flush();
            boolean isLoad = playerIn.readBoolean();
            teammateOut.writeObject(gson.toJson(new ServerMessage(0, "You're figure is 2, wait first player turn")));
            teammateOut.writeBoolean(false);
            teammateOut.flush();
            if (!isLoad) {
                position = new Position(new int[8][8]);
            } else {
                FileReader fileReader = new FileReader(SAVE_FILE_PATH);
                StringBuilder fileContent = new StringBuilder();
                Scanner scan = new Scanner(fileReader);
                while (scan.hasNextLine()) {
                    fileContent.append(scan.nextLine());
                }
                fileReader.close();
                position = gson.fromJson(fileContent.toString(), Position.class);
            }
            playerOut.writeObject(gson.toJson(position));
            playerOut.flush();
            teammateOut.writeObject(gson.toJson(position));
            teammateOut.flush();
            //System.out.println(gson.toJson(position));

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
                if (schemaValidationTest(gson.toJson(position))) {
                    saveGame(position);
                } else {
                    System.out.println("Incorrect format to save game");
                }
            }
        } catch (ClassNotFoundException | GenerationException e) {
            e.printStackTrace();
        }
    }

    private static void saveGame(Position position) {
        try {
            FileWriter fileWriter = new FileWriter(SAVE_FILE_PATH);
            fileWriter.write(new Gson().toJson(position));
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean schemaValidationTest(String json) throws IOException, GenerationException {
        SchemaStore schemaStore = new SchemaStore(); // Initialize a SchemaStore.
        Schema schema = schemaStore.loadSchemaJson(getSchemaFormFile()); // Load the schema.
        Validator validator = new Validator(); // Create a validator.
        try {
            validator.validateJson(schema, json);
            System.out.println("Correct json for save");
            return true;
        } catch (ValidationException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String getSchemaFormFile() throws IOException {
        FileReader fileReader = new FileReader("C:\\Work and projects\\JavaProjects\\TP_lab2_Teams\\src\\main\\resources\\schema.txt");
        StringBuilder fileContent = new StringBuilder();
        Scanner scan = new Scanner(fileReader);
        while (scan.hasNextLine()) {
            fileContent.append(scan.nextLine());
        }
        fileReader.close();
        return fileContent.toString();
    }
}
