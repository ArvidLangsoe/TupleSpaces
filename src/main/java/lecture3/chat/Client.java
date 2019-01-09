package lecture3.chat;

import org.jspace.RemoteSpace;

import java.io.IOException;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws InterruptedException {
        Scanner input = new Scanner(System.in);
        RemoteSpace chat = null;
        String userName = "Arvid";

        try {
            chat = new RemoteSpace("tcp://localhost:9001/chat?keep");
        } catch (IOException e) {
            e.printStackTrace();
        }

        while(true) {
            String message = input.nextLine();
            chat.put(userName, message);
        }
    }
}
