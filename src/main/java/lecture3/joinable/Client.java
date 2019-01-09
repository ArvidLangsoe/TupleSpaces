package lecture3.joinable;

import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;

public class Client {

    public static void main(String[] args) {

        try {

            // Set the URI of the loby of the chat server
            String uri = "tcp://127.0.0.1:9001/lobby?keep";

            // Connect to the remote lobby
            System.out.println("Connecting to lobby " + uri + "...");
            RemoteSpace lobby = new RemoteSpace(uri);

            // Read user name from the console
            System.out.print("Enter your name: ");
            BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
            String name = input.readLine();

            // Read chatroom from the console
            System.out.print("Select a chat room: ");
            String chatroom = input.readLine();

            // Send request to enter chatroom
            lobby.put("enter",name,chatroom);

            // Get response with chatroom URI
            Object[] response = lobby.get(new ActualField("roomURI"), new ActualField(name), new ActualField(chatroom), new FormalField(String.class));
            String chatroom_uri = (String) response[3];
            System.out.println("Connecting to chat space " + chatroom_uri);
            RemoteSpace chatroom_space = new RemoteSpace(chatroom_uri);

            // Keep sending whatever the user types
            System.out.println("Start chatting...");
            new Thread(new Printer(chatroom_space)).start();
            while(true) {
                String message = input.readLine();
                chatroom_space.put(name, message);
            }


        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}

class Printer implements Runnable{

    private RemoteSpace rmSpace;
    private int msgNum=0;

    Printer(RemoteSpace rmSpace){
        this.rmSpace = rmSpace;
        try {
            Object[] msgNum= rmSpace.query(new ActualField("msgNum"),new FormalField(Integer.class));
            this.msgNum=(Integer) msgNum[1];
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while(true){
            try {
                Object[] message = rmSpace.query(new ActualField(msgNum++),new FormalField(String.class), new FormalField(String.class));
                System.out.println(message[1]+": "+message[2]);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
