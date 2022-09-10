package mnkgame;

import mnkgame.*;
public class Utility {

    public MNKGameState myWin;
    public MNKGameState yourWin;
    private final int MAX_VALUE = 10;
    public long timerStart = 0;
    public int TIMEOUT;
	public double DEFAULT_TRIGGER_TIMEOUT_PERCENTAGE=90;
	public double TRIGGER_TIMEOUT_PERCENTAGE=90;
    
    public int Factorial(int n) {

        int result = 1;

        try {
            for (int i = 2; i <= n; i++)
                result = result * i;

        } catch (Exception ex) {
            return result; // maximum value reached
        }

        return result;
    }

    //IDEA: at the beginning implement a normal minimax algorithm with 1 win 0 lose, next
	// try to use more values such as 0.5 if the move is making 2/3 marks lined up and player is going to win (opposite for adversary)
    public int evaluateBoard(MNKBoard B, int depth) {

        // Game over
        if (B.gameState() == myWin)
            return MAX_VALUE;// + depth;
        else if (B.gameState() == yourWin)
            return -MAX_VALUE;// - depth;

        // Draw or not defined
        return 0;
    }

    //Return the only cell to select to prevent immediate loss if possible
	public MNKCell checkImmediateLoss(MNKBoard B){

		MNKCell FC[] = B.getFreeCells();

		//In our turn let's select 2 free cells (in case the one randomly selected by us is already the one leading opponent to win)
		//On each of the two cells selected play any other move by the opponent and see if his move lead him to victory
		//If only one move leads him to victory return that cell
		//We can assume that there are at least 2 free cells because if there was only one this method couldn't be called
		//The cost of the operation is not high: O(2*M*N) (actually lower because some cells are already selected)
		for(int i = 0; i <2; i++){

			//Select first available cell
			B.markCell(FC[i].i, FC[i].j);
			int WinningCellsCount = 0; //Increment on each move that does not end in a lost match
			MNKCell WinningCellForP2 = null;
			MNKCell FC2[] = B.getFreeCells();
			for(MNKCell current : FC2) {

				//No need to continue searching, there are more than 1 cell leading P2 not to win
				if(WinningCellsCount > 1)
					break;

				//Mark this cell as our move
				MNKGameState GameState = B.markCell(current.i, current.j);

				//Detect move outcome
				if(GameState == yourWin)
				{
					//System.err.println("P2 wins with " + current.i + " " + current.j);
					WinningCellForP2 = current; //By selecting this cell P2 does win
					WinningCellsCount++;
				}

				//Unselect
				B.unmarkCell();
			}

			//Unselect cell selected from us
			B.unmarkCell();

			//Maybe we found a cell that leads him to victory
			if(WinningCellsCount == 1 && WinningCellForP2 != null)
				return WinningCellForP2;
			//else if(WinningCellsCount > 2)

			//Reset and try again
			WinningCellsCount = 0;
		}

		//We did not find a winning cell for P2
		return null;
	}

	//Return the only cell to select to immediately win
	public MNKCell checkImmediateWin(MNKBoard B){

		MNKCell FC[] = B.getFreeCells();

		for(MNKCell current : FC) {

			//Mark this cell as our move
			MNKGameState GameState = B.markCell(current.i, current.j);

			B.unmarkCell();

			//Found immediate win
			if(GameState == myWin)
				return current;
		}

		//If we reached this far we can't win this turn
		return null;
	}

	 // If the cell is occupied by P1 return 0, otherwhise (P2) return 1
	 public static int pieceId(MNKCellState state) {
        if (state == MNKCellState.P1)
            return 0;
        else if (state == MNKCellState.P2)
            return 1;
        else
            return -1;
    }

    public boolean isTimeExpiring(){
		boolean Expiring = (System.currentTimeMillis()-timerStart)/1000.0 > TIMEOUT*(TRIGGER_TIMEOUT_PERCENTAGE/100.0);
		return Expiring;
	}
}
