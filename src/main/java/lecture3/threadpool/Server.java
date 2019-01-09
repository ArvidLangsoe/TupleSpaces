package lecture3.threadpool;

import org.jspace.*;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class Server {
    static ScheduledExecutorService scheduler;

    public static void main(String[] args) {
        scheduler= Executors.newScheduledThreadPool(2);

        try {

            // Create a repository
            SpaceRepository repository = new SpaceRepository();

            // Create a local space for the chat messages
            SequentialSpace lobby = new SequentialSpace();

            // Add the space to the repository
            repository.add("lobby",lobby);

            // Set the URI of the chat space
            String uri = "tcp://127.0.0.1:9001/lobby?keep";

            // Open a gate
            repository.addGate("tcp://127.0.0.1:9001/?keep");
            System.out.println("Opening repository gate at " + uri + "...");

            // This space room identifiers to port numbers
            SequentialSpace rooms = new SequentialSpace();

            // Keep serving requests to enter chatrooms
            while (true) {

                // roomN will be used to ensure every chat space has a unique name
                Integer roomC = 0;

                String roomURI;


                while (true) {
                    // Read request
                    Object[] request = lobby.get(new ActualField("enter"),new FormalField(String.class), new FormalField(String.class));
                    String who = (String) request[1];
                    String roomID = (String) request[2];
                    System.out.println(who + " requesting to enter " + roomID + "...");

                    // If room exists just prepare the response with the corresponding URI
                    Object[] the_room = rooms.queryp(new ActualField(roomID),new FormalField(Integer.class));
                    if (the_room != null) {
                        roomURI = "tcp://127.0.0.1:9001/chat" + the_room[1] + "?keep";
                    }
                    // If the room does not exist, create the room and launch a room handler
                    else {
                        System.out.println("Creating room " + roomID + " for " + who + " ...");
                        roomURI = "tcp://127.0.0.1:9001/chat" + roomC + "?keep";
                        System.out.println("Setting up chat space " + roomURI + "...");
                        Runnable roomHandler =new roomHandler(roomID,"chat"+roomC,roomURI,repository);
                        scheduler.scheduleAtFixedRate(roomHandler,0,50, MILLISECONDS);

                        rooms.put(roomID,roomC);
                        roomC++;
                    }

                    // Sending response back to the chat client
                    System.out.println("Telling " + who + " to go for room " + roomID + " at " + roomURI + "...");
                    lobby.put("roomURI", who, roomID, roomURI);
                }


            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class roomHandler implements Runnable {

    private Space chat;
    private String roomID;
    private String spaceID;
    private int messageNum=0;

    public roomHandler(String roomID, String spaceID, String uri, SpaceRepository repository) {

        this.roomID = roomID;
        this.spaceID = spaceID;

        // Create a local space for the chatroom
        chat = new SequentialSpace();

        // Add the space to the repository
        repository.add(this.spaceID, chat);

        try {
            chat.put("msgNum",0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {
        try {

            while(true) {
                // Keep reading chat messages and printing them
                Object[] message = chat.getp(new FormalField(String.class), new FormalField(String.class));
                if (message != null) {
                    System.out.println("ROOM " + roomID + " | " + message[0] + ":" + message[1]);
                    chat.get(new ActualField("msgNum"),new FormalField(Integer.class));
                    chat.put(messageNum++, message[0], message[1]);
                    chat.put("msgNum",messageNum);
                }
                else{
                    break;
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }
}
