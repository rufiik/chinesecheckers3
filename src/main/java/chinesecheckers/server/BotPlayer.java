package chinesecheckers.server;

import java.io.IOException;
import java.net.Socket;

public class BotPlayer extends ClientHandler {
    public BotPlayer(Socket socket, int playerId, int maxPlayers, String variant) throws IOException {
        super(socket, playerId, maxPlayers, variant);
    }
}
