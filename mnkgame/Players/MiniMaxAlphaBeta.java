package mnkgame;

import mnkgame.*;

public class MiniMaxAlphaBeta  implements MNKPlayer {

	private MNKBoard B;
	private MNKCellState myState;
	private MNKCellState yourState;
	private boolean meFirst;
	private Utility utility;

	/**
   * Default empty constructor
   */
	public MiniMaxAlphaBeta() {
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
		
		// If there is just one possible move, return immediately (match is over)
		if(FC.length == 1)
		{
			B.markCell(FC[0].i, FC[0].j);
			return FC[0];
		}

        //Select center cell as first move
		if(MC.length == 0){

			//Calculate and mark middle cell
			MNKCell MiddleCell = new MNKCell(B.M/2, B.N/2);
			B.markCell(MiddleCell.i, MiddleCell.j);

			//DEBUG
			Debug.PrintSummary();

			return MiddleCell;
		}

		//Use the first available cell if something goes wrong
		MNKCell BestMove = FC[0]; 

		//Initially assume the best move is the worst possible value and then use relaxing technique
		Integer MaxMoveValue = Integer.MIN_VALUE;

		//DEBUG
		Debug.Reset();

		//If found the best move possible (immediate win) return immediately
		if((meFirst && MC.length >= (B.K*2-2)) || (!meFirst && MC.length >= (B.K*2-1))){

			//Check if immediate win is possible
			MNKCell ImmediateWinCell = utility.checkImmediateWin(B);

			//Exit the loop if found way not to lose
			if(ImmediateWinCell != null){

				//Update local board with this move
				B.markCell(ImmediateWinCell.i, ImmediateWinCell.j); 

				//DEBUG
				Debug.FoundEnd(true);

				return ImmediateWinCell;
			}
		}
		
		//Check if there are already enough symbols placed for player2 to reach victory
		if((meFirst && MC.length >= (B.K*2-2)) || (!meFirst && MC.length >= (B.K*2-3))){

			//Check if immediate loss is possible
			MNKCell PreventLossCell = utility.checkImmediateLoss(B);

            //Immediate loss found, try intercepting it
            if (PreventLossCell != null) {

                // DEBUG
                Debug.FoundEnd(false);
                Debug.PrintSummary();

                //Update local board with this move
                B.markCell(PreventLossCell.i, PreventLossCell.j); 
                return PreventLossCell;
            }
		}
		

		/*CANNOT IMMEDIATELY WIN, PROCEED WITH MINIMAX*/

		//Cycle through all possible cells
		for(MNKCell d : FC) {

			//Mark this cell as player move (temp)
			B.markCell(d.i, d.j);

			//Apply minimax algorithm on the cell
            Integer MoveVal = miniMax(false, Integer.MIN_VALUE, Integer.MAX_VALUE,0);

			//DEBUG
			Debug.PrintMiddleCicle(B, d, MoveVal);

			//Rollback
			B.unmarkCell();

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

    public Integer miniMax(boolean maximizingPlayer, int alpha, int beta, int depth){

		//DEBUG
		Debug.IncreaseEvaluations();

		/*Each time we enter a recursive call, timeout should trigger 
		* a little bit earlier as we need to rollback all calls on stack when first triggered
		*/
		utility.TRIGGER_TIMEOUT_PERCENTAGE = utility.DEFAULT_TRIGGER_TIMEOUT_PERCENTAGE - (depth * 0.5);

		//Static evaluation of current game board
		int StaticEvaluation = utility.evaluateBoard(B, depth);

		//Base case, evaluation detected gameover or timeout soon
		if(B.gameState() != MNKGameState.OPEN || utility.isTimeExpiring())// || depth == 0)
		{
			//if(depth == 0)
			//	System.err.println("Depth reached 0");
			return StaticEvaluation;
		}

		//Our turn (Maximizing)
		if(maximizingPlayer){

			//Relax technique, assume the worst case scenario and relax on each step
			Integer MaxValue = Integer.MIN_VALUE;

			//Cycle through all possible moves
			MNKCell FC[] = B.getFreeCells();
			for(MNKCell current : FC) {

				
				//Mark this cell as player move
				B.markCell(current.i, current.j);

				//Recursively call minmax on this board scenario
				Integer boardValue = miniMax(false, alpha, beta, depth++);

				//Undo the move
				B.unmarkCell();

				//Check if found better value
				MaxValue = Math.max(MaxValue, boardValue);	

				//Prune if better result was available before, no need to continue searching
				alpha = Math.max(alpha, MaxValue);
				if (beta <= alpha) {
					Debug.Cuts++;
					return MaxValue;
				}
			}

			//Return the best value obtained
			return MaxValue;
		}
		//Opponent turn (minimizing)
		else{

			//Relax technique, assume the worst case scenario and relax on each step
			Integer MinValue = Integer.MAX_VALUE;

			//Cycle through all possible moves
			MNKCell FC[] = B.getFreeCells();
			for(MNKCell current : FC) {

				//Mark this cell as player move
				B.markCell(current.i, current.j);

				//Recursively call minmax on this board scenario
				Integer boardValue = miniMax(true, alpha, beta, depth++);

				//Undo the move
				B.unmarkCell();

				//Check if found better value
				MinValue = Math.min(MinValue, boardValue);	

				//Prune if better result was available before, no need to continue searching
				beta = Math.min(beta, MinValue);
				if (beta <= alpha) {
					Debug.Cuts++;
					return MinValue;
				}
			}

			return MinValue;
		}
    }

	public String playerName() {
		return "MMAB";
	}
}