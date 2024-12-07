import java.rmi.Remote;
import java.rmi.RemoteException;

/*
Step 1: Create the Remote Interface (TicTacToeRemote.java)
This interface defines the remote methods that will be used to communicate between the client and the server.
*/

public interface ServerInterface extends Remote {

    // Make a move on the board (row, column)
    boolean makeMove(int row, int col, int moverID) throws RemoteException;

    // (Try to) add player to the game
    char registerPlayer(ClientInterface player) throws RemoteException;
}
