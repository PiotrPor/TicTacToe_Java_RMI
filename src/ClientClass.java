import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ClientClass extends JFrame {
    private int ID;
    private ServerInterface serverForMe;
    private JButton[][] buttons;
    private JLabel labelForPlayer;
    private char playerSign;
    private char[][] board;
    private boolean gameStarted;
    private boolean isMyTurn;

    public ClientClass() {
        int buttonSize = 80;
        int marginSize = 20;
        int heightOfLabel = 30;
        int totalWindowWidth = 3*buttonSize+3*marginSize;
        int totalWindowHeight = 4*buttonSize+4*marginSize+heightOfLabel;
        setTitle("Tic-Tac-Toe Game");
        setSize(totalWindowWidth, totalWindowHeight);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(null);
        buttons = new JButton[3][3];
        playerSign = '-';
        gameStarted = false;
        isMyTurn = false;

        // add the JLabel component
        labelForPlayer = new JLabel("Creating...");
        labelForPlayer.setEnabled(true);
        labelForPlayer.setBounds(0,marginSize,totalWindowWidth,heightOfLabel);
        labelForPlayer.setHorizontalAlignment(SwingConstants.CENTER);
        labelForPlayer.setVisible(true);
        add(labelForPlayer);
        // Initialize buttons for the Tic-Tac-Toe grid
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                buttons[i][j] = new JButton("-");
                buttons[i][j].setFont(new Font("Arial", Font.PLAIN, 40));
                buttons[i][j].setFocusPainted(false);
                buttons[i][j].setEnabled(true); //TODO: maybe wait until there are 2 players?
                buttons[i][j].setBounds(
                        j*buttonSize+marginSize,
                        i*buttonSize+2*marginSize+heightOfLabel,
                        buttonSize,
                        buttonSize
                        ); //x y width height
                buttons[i][j].addActionListener(new ButtonClickListener(i, j));
                add(buttons[i][j]);
            }
        }
        // Register the client on the server
        labelForPlayer.setText("Created");
        System.out.println("Client's constructor finished.");
    }

    public void registerYourself() {
        System.out.println("Client will try to register on the server.");
        try {
            // Look up the server using RMI
            serverForMe = (ServerInterface) Naming.lookup("//localhost/TicTacToe");
            playerSign = serverForMe.registerPlayer(new ClientCallback());
            System.out.println("Client registered with sign \""+playerSign+"\" and ID="+ID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class ButtonClickListener implements ActionListener {
        private int row, col;

        public ButtonClickListener(int row, int col) {
            this.row = row;
            this.col = col;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println("Pressed button "+row+";"+col); //debug!!
            System.out.println("  gameStarted: "+gameStarted+" , isMyTurn: "+isMyTurn);
            if (gameStarted && isMyTurn) {
                try {
                    System.out.println("  Will invoke makeMove() method on the server");
                    if (serverForMe.makeMove(row, col, ID)) {
                        System.out.println("  Server added the new sign onto the board");
                        buttons[row][col].setEnabled(false);
                        isMyTurn = false;
                    }
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private class ClientCallback extends UnicastRemoteObject implements ClientInterface {
        public ClientCallback() throws RemoteException {
            super();
        }

        @Override
        public void updateStatus(String message) throws RemoteException {
            System.out.println("Client updateStatus() \""+message+"\". isMyTurn: "+isMyTurn);
            SwingUtilities.invokeLater(() -> {
                //TODO: later use "JOptionPane.showMessageDialog(this, message)" ??
                ClientClass.this.labelForPlayer.setText(message);
            });
        }

        @Override
        public void updateBoard(char[][] updatedBoard) throws RemoteException {
            SwingUtilities.invokeLater(() -> {
                // Update the client's board with the new state from the server
                ClientClass.this.board = updatedBoard;
                // Update the visual buttons to reflect the new board state
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        ClientClass.this.buttons[i][j].setText(String.valueOf(updatedBoard[i][j]));
                    }
                }
            });
        }

        @Override
        public void makeThemStartPlaying(Boolean doesHeBegin) throws RemoteException {
            gameStarted = true;
            isMyTurn = doesHeBegin;
            System.out.println("Server makes you start playing. isMyTurn: "+isMyTurn);
        }

        @Override
        public void tellItsTheirTurn(Boolean isItTheirTurn) throws RemoteException {
            isMyTurn = isItTheirTurn;
            System.out.println("Server tells you about your turn. isMyTurn: "+isMyTurn);
        }

        @Override
        public void setID(int newValue) throws RemoteException {
            ID = newValue;
        }

        @Override
        public int getID() throws RemoteException {
            return ID;
        }
    }
}
