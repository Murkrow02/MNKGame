package mnkgame;

import mnkgame.*;

public class Utility {

    public MNKGameState myWin;
    public MNKGameState yourWin;
	public MNKCellState myMark;
    public MNKCellState yourMark;
    private final int MAX_VALUE = 100;
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


    public int evaluateBoard(MNKBoard B, int depth) {

        //Immediately return if gameover
        if (B.gameState() == myWin)
            return MAX_VALUE;// + depth;
        else if (B.gameState() == yourWin)
            return -MAX_VALUE;// - depth;


		return 0;
    }

	public int evaluateBoard2(MNKBoard B, WinCounters counters){
		
		int ourWins = 0;
		int hisWins = 0;

		//Start from scratch, set all win counters by evaluating all cells they control
		for (var counter : counters.WinCounters) {

			int P1_OK = 0;
			int P2_OK = 0;

			//Empty counter
			if(counter.CellsToCheck == null)
				continue;

			//Check all the cells controlled by this WinCounter
			for (var cellToCheck : counter.CellsToCheck) {
				
				//P1 count
				if(B.B[cellToCheck.i][cellToCheck.j] == MNKCellState.P2)
					P1_OK = 0; //P2 sign on this cell, reset
				else
					P1_OK++; //Free or P1 sign, can go on

				//P2 count
				if(B.B[cellToCheck.i][cellToCheck.j] == MNKCellState.P1)
					P2_OK = 0; //P1 sign on this cell, reset
				else
					P2_OK++; //Free or P2 sign, can go on

				//Evaluations
				if(P1_OK == B.K){
					counter.P1Wins++; //We found a possible win
					P1_OK = 0;
				}
				if(P2_OK == B.K)
				{
					counter.P2Wins++; //He found a possible win
					P2_OK = 0;
				}

				//Check if no need to go further (implement later by saving cell controlled count and incrementing local variable)
				// int left_until_end = B.M-j; //How many spaces until end of row
				// if(left_until_end+WE_OK<B.K && left_until_end+HIM_OK<B.K){

				// 	//Neither us nor him can win on this row, no need to go further
				// 	continue;
				// }
			}
		}

			
		Debug.printGameState(B);
		System.out.println(counters.Score(true));

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
	

    public boolean isTimeExpiring(){
		boolean Expiring = (System.currentTimeMillis()-timerStart)/1000.0 > TIMEOUT*(TRIGGER_TIMEOUT_PERCENTAGE/100.0);
		return Expiring;
	}
}
