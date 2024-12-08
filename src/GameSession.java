import java.rmi.RemoteException;

public class GameSession {
    private ClientInterface player1;
    private ClientInterface player2;
    private char[][] board;
    private char currentPlayerSign;
    private boolean gameStarted;

    public GameSession() {
        this.player1 = null;
        this.player2 = null;
        this.board = new char[3][3];
        this.currentPlayerSign = 'X';
        gameStarted = false;
    }

    public GameSession(ClientInterface player1, ClientInterface player2) {
        this.player1 = player1;
        this.player2 = player2;
        this.board = new char[3][3];
        this.currentPlayerSign = 'X';
        gameStarted = false;
    }

    public int tellHowManyPlayers() {
        if (player1 == null && player2 == null) {
            return 0;
        } else if ((player1 != null && player2 == null) || (player1 == null && player2 != null)) {
            return 1;
        } else {
            return 2;
        }
    }

    public char addPlayer(ClientInterface player) {
        if (player1 == null) {
            player1 = player;
            return 'X';
        } else if (player2 == null) {
            player2 = player;
            return 'O';
        }
        return '-';
    }

    public int isThisPlayerHere(int id) {
        int a=0, b=0;
        try {
            if(player1 != null) { a = player1.getID(); }
            if(player1 != null) { b = player2.getID(); }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        if(a == id) { return 1; }
        else if(b == id) { return 2; }
        else { return 0; }
    }

    private void initializeBoard() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = '-';
            }
        }
    }

    public void startGame() throws RemoteException {
        gameStarted = true;
        currentPlayerSign = 'X';
        player1.makeThemStartPlaying(true, player2.getID());
        player2.makeThemStartPlaying(false, player1.getID());
        initializeBoard();
        updateClients();
    }

    public void updateClients() throws RemoteException {
        player1.updateBoard(board);
        player2.updateBoard(board);
        if (currentPlayerSign == 'X') {
            player1.updateStatus("Your turn (X)");
            player1.tellItsTheirTurn(true);
            player2.updateStatus("Wait for opponent's move");
            player2.tellItsTheirTurn(false);
        } else {
            player2.updateStatus("Your turn (O)");
            player2.tellItsTheirTurn(true);
            player1.updateStatus("Wait for opponent's move");
            player1.tellItsTheirTurn(false);
        }
    }

    public void letPlayerLeave(int whichOne) throws RemoteException {
        if(whichOne == 1) {
            player1 = null;
            if(player2 != null) {
                player2.yourOpponentLeft();
                //player2.updateStatus("Your opponent has left the game");
                //player2.makeThemStartPlaying(false);
            }
        } else if(whichOne == 2) {
            player2 = null;
            if(player1 != null) {
                player1.yourOpponentLeft();
                //player1.updateStatus("Your opponent has left the game");
                //player1.makeThemStartPlaying(false);
            }
        }
        gameStarted = false; //TODO: needed?
    }

    public boolean performMove(int row, int col, int moverID) throws RemoteException {
        char playerSign = '-';
        if(isThisPlayerHere(moverID) == 1) { playerSign  = 'X'; }
        else if(isThisPlayerHere(moverID) == 2) { playerSign = 'O'; }
        //
        System.out.println("  Session: player wants to put "+playerSign+" on "+row+";"+col);
        if (!gameStarted) {
            return false;
        }
        if (board[row][col] == '-' && playerSign == currentPlayerSign) {
            System.out.println("  Session will put the sign onto the designated field");
            board[row][col] = playerSign;
            if (checkWin(playerSign)) {
                if (player1 != null && player2 != null) {
                    String winnerMessage = playerSign + " Wins!";
                    player1.updateStatus(winnerMessage);
                    player2.updateStatus(winnerMessage);
                }
                gameStarted = false;
            } else if (isDraw()) {
                if (player1 != null && player2 != null) {
                    player1.updateStatus("It's a Draw!");
                    player2.updateStatus("It's a Draw!");
                }
                gameStarted = false;
            } else {
                currentPlayerSign = (currentPlayerSign == 'X') ? 'O' : 'X';
                updateClients(); //TODO: needed?
            }
            // Update both clients' boards remotely
            if (player1 != null) player1.updateBoard(board);
            if (player2 != null) player2.updateBoard(board);

            return true;
        }
        System.out.println("  Session: new sign wasn't added");
        return false;
    }

    private boolean checkWin(char player) {
        // Check rows, columns, and diagonals
        for (int i = 0; i < 3; i++) {
            if ((board[i][0] == player && board[i][1] == player && board[i][2] == player) ||
                    (board[0][i] == player && board[1][i] == player && board[2][i] == player)) {
                return true;
            }
        }
        if ((board[0][0] == player && board[1][1] == player && board[2][2] == player) ||
                (board[0][2] == player && board[1][1] == player && board[2][0] == player)) {
            return true;
        }
        return false;
    }

    private boolean isDraw() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == '-') {
                    return false;
                }
            }
        }
        return true;
    }
}
