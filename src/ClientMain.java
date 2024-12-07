public class ClientMain {
    //in Client its contents were enclosed in "SwingUtilities.invokeLater(() -> {})"
    public static void main(String[] args) {
        TicTacToeClient client = new TicTacToeClient();
        client.setVisible(true);
        System.out.print("Client is created");
        client.registerYourself();
        System.out.print(" and registered on the server.\n");
    }
}
