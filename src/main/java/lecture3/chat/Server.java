package lecture3.chat;

import org.jspace.FormalField;
import org.jspace.SequentialSpace;
import org.jspace.SpaceRepository;

public class Server {

    public static void main(String[] args) throws InterruptedException {
        // create repository
        SpaceRepository repository = new SpaceRepository();

        // Create a local space for the chat messages
        SequentialSpace chat = new SequentialSpace();

        // Add the space to the repository
        repository.add("chat", chat);

        // Open a gate
        repository.addGate("tcp://localhost:9001/?keep");



        // Keep reading chat messages and printing them
        while (true) {
            Object[] t = chat.get(new FormalField(String.class), new FormalField(String.class));
            System.out.println(t[0] + ": " + t[1]);
        }

    }
}
