package mnkgame;

import java.io.Console;
import java.util.Hashtable;
import java.util.concurrent.TimeoutException;

import mnkgame.*;

public class IterativeDeepZob implements MNKPlayer {

	private MNKBoard B;
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
	public IterativeDeepZob() {
	}

	public void initPlayer(int M, int N, int K, boolean first, int timeout_in_secs) {
		
		utility = new Utility();

		//LOCAL BOARD
		B       = new MNKBoard(M,N,K); 

		//Possible gamestates
		utility.myWin   = first ? MNKGameState.WINP1 : MNKGameState.WINP2; 
		utility.yourWin = first ? MNKGameState.WINP2 : MNKGameState.WINP1;

		//How the cells are marked in this match
		utility.myMark = first ? MNKCellState.P1 : MNKCellState.P2;
		utility.yourMark = first ? MNKCellState.P2 : MNKCellState.P1;

		//Save if we are the first player
		meFirst = first;

		//If the function takes longer than the set timeout an exception is thrown
		utility.TIMEOUT = timeout_in_secs;	

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
		
		/*
		 * Note: is not needed anymore to search for immediate win/loss becaouse with 
		 * iterative deepening we always find immediately a winning/losing position,
		 */

		/*CANNOT IMMEDIATELY WIN, PROCEED WITH ITERATIVEDEEPENING*/

		//Cycle through all possible cells
		//for(int i = 1; !utility.isTimeExpiring(); ++i){

			for(MNKCell d : FC) {

				//Mark this cell as player move (temp)
				B.markCell(d.i, d.j);

				//Apply negamax algorithm on the cell
				Integer MoveVal = 0;
				try{
					MoveVal = miniMax(false, Integer.MIN_VALUE, Integer.MAX_VALUE, null, 1);
				}catch(TimeoutException ex){}

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
			}
		//}

		//Select center cell as first move (placed here to do computation and fill hashtable anyway before first move)
		if(MC.length == 0){

			//Calculate and mark middle cell
			MNKCell MiddleCell = new MNKCell(B.M/2, B.N/2);
			B.markCell(MiddleCell.i, MiddleCell.j);

			//DEBUG
			Debug.PrintSummary();

			return MiddleCell;
		}


		//Return the result
		B.markCell(BestMove.i, BestMove.j); //Update local board with this move
		
		//DEBUG
		Debug.PrintSummary();

		return BestMove; //Update game board
	}

	public Integer miniMax(boolean maximizingPlayer, int alpha, int beta, Long previousHash, int depth)throws TimeoutException{

		//DEBUG
		Debug.IncreaseEvaluations();

		//Immediately stop if we are running out of time
		if(utility.isTimeExpiring()){
			throw new TimeoutException();
		}

		//Base case, evaluation detected gameover or timeout soon
		if(B.gameState() != MNKGameState.OPEN || depth <= 0)
		{
			//if(depth == 0)
			//	System.err.println("Depth reached 0");
			return utility.evaluateBoard(B, depth);
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

				//Check if already evaluated this game state
				long boardHash = previousHash != null ? ZT.diffHash(previousHash, current, utility.myMark) : ZT.computeHash(B);
				Integer boardValue = ZT.EvaluatedStates.getOrDefault(boardHash, null);

				//Already evaluated this game state
				if(boardValue != null){
					Debug.AlreadyEvaluated();
				}
				else{
					//Recursively call minmax on this board scenario
					boardValue = miniMax(false, alpha, beta, boardHash, depth--);

					//Add current value to HashSet for future use
					ZT.EvaluatedStates.put(boardHash, boardValue);	

					//Add simmetric board states to HashSet as they have the same static evaluation
					ZT.addSimmetryHashes(B, boardValue);
				}

				//Undo the move
				B.unmarkCell();

				//Check if found better value
				MaxValue = Math.max(MaxValue, boardValue);	

				//Prune if better result was available before, no need to continue searching
				alpha = Math.max(alpha, MaxValue);
				if (alpha >= beta) {
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

				//Check if already evaluated this game state
				long boardHash = previousHash != null ? ZT.diffHash(previousHash, current, utility.yourMark) : ZT.computeHash(B);
				Integer boardValue = ZT.EvaluatedStates.getOrDefault(boardHash, null);
				
				//Already evaluated this state
				if (boardValue != null) {
					Debug.AlreadyEvaluated();
				} else{

					//Recursively call minmax on this board scenario
					boardValue = miniMax(true, alpha, beta, boardHash, depth--);

					//Add current value to HashSet for future use
					ZT.EvaluatedStates.put(boardHash, boardValue);	

					//Add simmetric board states to HashSet as they have the same static evaluation
					ZT.addSimmetryHashes(B, boardValue);
				}

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
		return "IterativeZob";
	}
}


