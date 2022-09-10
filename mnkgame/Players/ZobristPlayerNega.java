package mnkgame;

import java.util.Hashtable;
import mnkgame.*;

public class ZobristPlayerNega implements MNKPlayer {

	private MNKBoard B;
	private MNKCellState myState;
	private MNKCellState yourState;
	private boolean meFirst;
	private Utility utility;

	/* This tri-dimensional array stores M*N random values, is used 
	   by the game board hashing function to create a unique hash of 
	   each possible game state
	*/
	public ZobristTable ZT;

	/**
   * Default empty constructor
   */
	public ZobristPlayerNega() {
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

		//Initialize Zobrist table
		ZT = new ZobristTable(M,N);

		//Initialize hash table 
		int MaxTableSize = 1<<20; // 2^20
		int TableSize = 1<<M*N; // 2^M*N, this size is related to board size

		//Creating a huge hashtable could crash the application
		if(TableSize > MaxTableSize)
			TableSize = MaxTableSize;

		//Init hashtable with desired size
		ZT.EvaluatedStates = new Hashtable<Long, Integer>(TableSize);
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

		//Return immediately by intercepting player2 winning move before he does
		MNKCell PreventLossCell = null;
		
		//Check if there are already enough symbols placed for player2 to reach victory
		if((meFirst && MC.length >= (B.K*2-2)) || (!meFirst && MC.length >= (B.K*2-3))){

			//Check if immediate loss is possible
			PreventLossCell = utility.checkImmediateLoss(B);
		}
		

		/*CANNOT IMMEDIATELY WIN, PROCEED WITH MINIMAX*/

		//Cycle through all possible cells
		for(MNKCell d : FC) {

			//Mark this cell as player move (temp)
			B.markCell(d.i, d.j);

			//Apply negamax algorithm on the cell
            Integer MoveVal = negaMax(Integer.MIN_VALUE, Integer.MAX_VALUE, null, 0);

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

		//Select center cell as first move (placed here to do computation and fill hashtable anyway before first move)
		if(MC.length == 0){

			//Calculate and mark middle cell
			MNKCell MiddleCell = new MNKCell(B.M/2, B.N/2);
			B.markCell(MiddleCell.i, MiddleCell.j);

			//DEBUG
			Debug.PrintSummary();

			return MiddleCell;
		}

		/*Select cell that would make the opponent win as is for sure the best move 
			(placed here to do computation and fill hashtable anyway)*/
		if(PreventLossCell != null){
			
			//DEBUG
			Debug.FoundEnd(false);
			Debug.PrintSummary();

			B.markCell(PreventLossCell.i, PreventLossCell.j); //Update local board with this move
			return PreventLossCell;
		}

		//Return the result
		B.markCell(BestMove.i, BestMove.j); //Update local board with this move
		
		//DEBUG
		Debug.PrintSummary();

		return BestMove; //Update game board
	}

	public Integer negaMax(int alpha, int beta, Long previousHash, int depth){

		//DEBUG
		Debug.IncreaseEvaluations();

		/*Each time we enter a recursive call, timeout should trigger 
		* a little bit earlier as we need to rollback all calls on stack when first triggered
		*/
		utility.TRIGGER_TIMEOUT_PERCENTAGE = utility.DEFAULT_TRIGGER_TIMEOUT_PERCENTAGE - (depth * 0.5);

		//Base case, evaluation detected gameover or timeout soon
		if(B.gameState() != MNKGameState.OPEN || utility.isTimeExpiring())// || depth == 0)
		{
			//if(depth == 0)
			//	System.err.println("Depth reached 0");
			return utility.evaluateBoard(B, depth);
		}


		// Relax technique, assume the worst case scenario and relax on each step
		Integer BestValue = Integer.MIN_VALUE;

		// Cycle through all possible moves
		MNKCell FC[] = B.getFreeCells();
		for (MNKCell current : FC) {

			// Mark this cell as player move
			B.markCell(current.i, current.j);

			//Check if already evaluated this game state
			long boardHash = previousHash != null ? ZT.diffHash(previousHash, current, myState) : ZT.computeHash(B);
			Integer boardValue = ZT.EvaluatedStates.getOrDefault(boardHash, null);

			//Already evaluated this game state
			if(boardValue != null){
				Debug.AlreadyEvaluated();
			}
			else{
				//Recursively call minmax on this board scenario
				boardValue = -negaMax(-alpha, -beta,null, depth++);

				//Add current value to HashSet for future use
				ZT.EvaluatedStates.put(boardHash, boardValue);	

				//Add simmetric board states to HashSet as they have the same static evaluation
				ZT.addSimmetryHashes(B, boardValue);
			}

			// Undo the move
			B.unmarkCell();

			//Cuts
			if (boardValue > BestValue)
				BestValue = boardValue;
			if (BestValue > alpha) 
				alpha = BestValue;
			if (BestValue >= beta)
				break;
		}

		// Return the best value obtained
		return alpha;
	}

	public String playerName() {
		return "ZobNega";
	}
}


