package mnkgame;

import javax.swing.plaf.synth.SynthStyle;

import mnkgame.*;

public class NegaMax  implements MNKPlayer {

	private MNKBoard B;
	private MNKCellState myState;
	private MNKCellState yourState;
	private boolean meFirst;
	private Utility utility;

	/**
   * Default empty constructor
   */
	public NegaMax() {
	}

	public void initPlayer(int M, int N, int K, boolean first, int timeout_in_secs) {
		
		utility = new Utility();

		//LOCAL BOARD
		B       = new MNKBoard(M,N,K); 

		//Possible gamestates
		utility.myWin   = first ? MNKGameState.WINP1 : MNKGameState.WINP2; 
		utility.yourWin = first ? MNKGameState.WINP2 : MNKGameState.WINP1;

		//Save if we are the first player
		meFirst = first;

		//If the function takes longer than the set timeout an exception is thrown
		utility.TIMEOUT = timeout_in_secs;	

		//How the cells are marked in this match
		myState = meFirst ? MNKCellState.P1 : MNKCellState.P2;
		yourState = meFirst ? MNKCellState.P2 : MNKCellState.P1;
	}

	public MNKCell selectCell(MNKCell[] FC, MNKCell[] MC) {

		//Start timer
		utility.timerStart = System.currentTimeMillis();

		//Update local board with last move from opponent
		if(MC.length > 0) {
			MNKCell c = MC[MC.length-1]; // Recover move from opponent
			B.markCell(c.i,c.j);         // Save the last move in the local MNKBoard
		}

		//Use the first available cell if something goes wrong
		MNKCell BestMove = FC[0]; 

		//Initially assume the best move is the worst possible value and then use relaxing technique
		Integer MaxMoveValue = Integer.MIN_VALUE;

		//DEBUG
		Debug.Reset();
		

		/*CANNOT IMMEDIATELY WIN, PROCEED WITH MINIMAX*/

		//Cycle through all possible cells
		for(MNKCell d : FC) {

			B.markCell(d.i, d.j);

			//Apply minimax algorithm on the cell
            Integer MoveVal = -negaMax(-1, Integer.MAX_VALUE, Integer.MIN_VALUE);

			B.unmarkCell();

			//DEBUG
			Debug.PrintMiddleCicle(B, d, MoveVal);

			//Check if found a better move
			if(MoveVal > MaxMoveValue){
				MaxMoveValue = MoveVal;
				BestMove = d;
			}

			//Check timeout
			if(utility.isTimeExpiring())
			{
				Debug.SolvedGame = false;
				break;
			}
			else
            utility.TRIGGER_TIMEOUT_PERCENTAGE = utility.DEFAULT_TRIGGER_TIMEOUT_PERCENTAGE; //Reset default timeout trigger
		}

		//Return the result
		B.markCell(BestMove.i, BestMove.j); //Update local board with this move
		
		//DEBUG
		Debug.PrintSummary();

		return BestMove; //Update game board
	}

    public Integer negaMax(int sign, int alpha, int beta){

		//Base case, evaluation detected gameover or timeout soon
		if(B.gameState() != MNKGameState.OPEN || utility.isTimeExpiring())// || depth == 0)
		{
			//if(depth == 0)
			//	System.err.println("Depth reached 0");
			return utility.evaluateBoard(B,0)*sign;
		}

			//Relax technique, assume the worst case scenario and relax on each step
			Integer bestValue = Integer.MIN_VALUE;

			//Cycle through all possible moves
			MNKCell FC[] = B.getFreeCells();
			for(MNKCell current : FC) {

				//Mark this cell as player move
				B.markCell(current.i, current.j);

				//Recursively call minmax on this board scenario
				bestValue = Math.max(bestValue, -negaMax(-sign, -alpha, -beta));
				alpha = Math.max(alpha, bestValue);

				//Revert move 
				B.unmarkCell();
		
				if(alpha >= beta)
					break;
			}

			//Return the best value obtained
			return bestValue;
    }

	public String playerName() {
		return "MMAB";
	}
}