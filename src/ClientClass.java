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
    private JLabel labelAboutOpponent;
    private JButton buttonToExit;
    private char playerSign;
    private char[][] board;
    private boolean gameStarted;
    private boolean isMyTurn;

    public ClientClass() {
        int gameButtonSize = 80;
        int marginSize = 20;
        int heightOfLabel = 30;
        int exitButtonWidth = (int)(2*gameButtonSize); //120? 160?
        int exitButtonHeight = 50;
        int totalWindowWidth = 3*gameButtonSize+3*marginSize;
        int totalWindowHeight = 4*gameButtonSize+4*marginSize+2*heightOfLabel;
        setTitle("Tic-Tac-Toe Java RMI Client");
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
        //second JLabel
        labelAboutOpponent = new JLabel(" ");
        labelAboutOpponent.setEnabled(true);
        labelAboutOpponent.setBounds(0,2*marginSize,totalWindowWidth,heightOfLabel);
        labelAboutOpponent.setHorizontalAlignment(SwingConstants.CENTER);
        labelAboutOpponent.setVisible(true);
        add(labelAboutOpponent);
        // Initialize buttons for the Tic-Tac-Toe grid
        int a,b;
        for (a = 0; a < 3; a++) {
            for (b = 0; b < 3; b++) {
                buttons[a][b] = new JButton("-");
                buttons[a][b].setFont(new Font("Arial", Font.PLAIN, 40));
                buttons[a][b].setFocusPainted(false);
                buttons[a][b].setEnabled(false); //TODO: maybe wait until there are 2 players?
                buttons[a][b].setBounds(
                        b*gameButtonSize+marginSize,
                        a*gameButtonSize+2*marginSize+2*heightOfLabel,
                        gameButtonSize,
                        gameButtonSize
                        ); //x y width height
                buttons[a][b].addActionListener(new ButtonClickListener(a, b));
                add(buttons[a][b]);
            }
        }
        // add JButton for leavin the game
        buttonToExit = new JButton("Exit");
        buttonToExit.setFont(new Font("Arial", Font.PLAIN, 30));
        buttonToExit.setVisible(false); //for now
        buttonToExit.setEnabled(false); //for now
        buttonToExit.setBounds(
                marginSize+gameButtonSize/2,
                3*gameButtonSize+3*marginSize+2*heightOfLabel,
                exitButtonWidth,
                exitButtonHeight
                );
        buttonToExit.addActionListener(new ExitClickListener());
        add(buttonToExit);
        // tell them the client object is ready
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
            setTitle("Tic-Tac-Toe Player "+ID);
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

    private class ExitClickListener implements ActionListener {
        public ExitClickListener(){
            //empty
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                serverForMe.removeMe(ID);
                System.exit(0);
            } catch (RemoteException ex) {
                ex.printStackTrace();
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
            //TODO: later use "JOptionPane.showMessageDialog(this, message)" ??
            ClientClass.this.labelForPlayer.setText(message);
        }

        @Override
        public void updateBoard(char[][] updatedBoard) throws RemoteException {
            // Update the client's board with the new state from the server
            ClientClass.this.board = updatedBoard;
            // Update the visual buttons to reflect the new board state
            int a,b;
            for (a = 0; a < 3; a++) {
                for (b = 0; b < 3; b++) {
                    ClientClass.this.buttons[a][b].setText(String.valueOf(updatedBoard[a][b]));
                }
            }
        }

        @Override
        public void makeThemStartPlaying(Boolean doesHeBegin, int enemyID) throws RemoteException {
            gameStarted = true;
            isMyTurn = doesHeBegin;
            buttonToExit.setVisible(true);
            buttonToExit.setEnabled(true);
            int a,b;
            for (a = 0; a < 3; a++) {
                for (b = 0; b < 3; b++) {
                    buttons[a][b].setEnabled(true);
                }
            }
            ClientClass.this.labelAboutOpponent.setText("Opponent: player_"+enemyID);
            System.out.println("Server makes you start playing. isMyTurn: "+isMyTurn);
        }

        @Override
        public void tellItsTheirTurn(Boolean isItTheirTurn) throws RemoteException {
            isMyTurn = isItTheirTurn;
            System.out.println("Server tells you about your turn. isMyTurn: "+isMyTurn);
        }

        @Override
        public void yourOpponentLeft() throws RemoteException {
            gameStarted = false;
            isMyTurn = false;
            ClientClass.this.labelForPlayer.setText("Your opponent has left the game");
            ClientClass.this.labelAboutOpponent.setText(" ");
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
