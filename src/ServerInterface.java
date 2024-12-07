import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerInterface extends Remote {

    // Make a move on the board (row, column)
    boolean makeMove(int row, int col, int moverID) throws RemoteException;

    // (Try to) add player to the game
    char registerPlayer(ClientInterface player) throws RemoteException;
}
