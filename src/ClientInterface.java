import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientInterface extends Remote {

    // Force client to be informed about new state of board
    void updateBoard(char[][] board) throws RemoteException;

    // Causes a message/text to appear in client's window
    void updateStatus(String message) throws RemoteException;

    // inform client that the game has started
    void makeThemStartPlaying(Boolean doesHeBegin, int enemyID) throws RemoteException;

    // inform the player whether it's their turn
    void tellItsTheirTurn(Boolean isItTheirTurn) throws RemoteException;

    void yourOpponentLeft() throws RemoteException;

    void setID(int newValue) throws RemoteException;

    int getID() throws RemoteException;
}
