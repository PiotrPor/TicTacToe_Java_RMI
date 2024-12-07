import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class ServerClass extends UnicastRemoteObject implements ServerInterface {
    private int howManyPlayersJoined;
    private List<ClientInterface> players;
    private List<GameSession> gameSessions;

    // Constructor
    public ServerClass() throws RemoteException {
        super();
        howManyPlayersJoined = 0;
        players = new ArrayList<>();
        gameSessions = new ArrayList<>();
    }

    @Override
    public boolean makeMove(int row, int col, int moverID) throws RemoteException {
        System.out.println("Player "+moverID+" wants to put a sign on "+row+" ; "+col);
        boolean successful = false;
        for(GameSession gra : gameSessions) {
            if(gra.isThisPlayerHere(moverID) > 0) {
                System.out.println("  Found the game where he is");
                successful = gra.performMove(row, col, moverID);
                break;
            }
        }
        return successful;
    }

    @Override
    public char registerPlayer(ClientInterface player) throws RemoteException {
        System.out.println("Server tries to register a new player");
        char assignedSign = '-';
        howManyPlayersJoined += 1;
        player.setID(howManyPlayersJoined);
        players.add(player);
        if (gameSessions.isEmpty() || areAllSessionsFull()) {
            System.out.println("  Creating a new game session");
            GameSession newGame = new GameSession();
            newGame.addPlayer(player);
            gameSessions.add(newGame);
            assignedSign = 'X';
            player.updateStatus("Waiting for the second player to join");
        } else {
            System.out.println("  Adding player to the existing game session");
            for(GameSession gra : gameSessions) {
                if (gra.tellHowManyPlayers() < 2) {
                    assignedSign = gra.addPlayer(player);

                    if(assignedSign == 'X') {
                        System.out.println("  registered as 1st player");
                    } else if(assignedSign == 'O') {
                        System.out.println("  registered as 2nd player");
                    }

                    if(gra.tellHowManyPlayers() == 2) {
                        System.out.println("  Because there are 2 players registered it's time to start the game");
                        gra.startGame();
                    }
                    break;
                }
            }
        }
        return assignedSign;
    }

    private boolean areAllSessionsFull() {
        for(GameSession gra : gameSessions) {
            if (gra.tellHowManyPlayers() < 2) {
                return false;
            }
        }
        return true;
    }
}
