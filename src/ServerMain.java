public class ServerMain {
    public static void main(String[] args) {
        try {
            TicTacToeServer server = new TicTacToeServer();
            java.rmi.registry.LocateRegistry.createRegistry(1099); // Start RMI registry
            java.rmi.Naming.rebind("TicTacToe", server);
            System.out.println("Tic-Tac-Toe Server is ready.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
