package mnkgame;

import java.util.Hashtable;
import mnkgame.*;

/*
 * TODO: Insert all possible win/loss already in hash table
 *  Negamax negascout
 *  Sometime AI is dumb and does not prevent the opponent to place more symbols near each other
 * 	Use try catch to immediately stop minimax when timeout found
 */

public class TestZob implements MNKPlayer {

	private MNKBoard B;
	private Utility utility;

	/* This tri-dimensional array stores M*N random values, is used 
	   by the game board hashing function to create a unique hash of 
	   each possible game state
	*/
	public ZobristTable ZT;

	public WinCounters Counters;

	/**
   * Default empty constructor
   */
	public TestZob() {
	}

	public void initPlayer(int M, int N, int K, boolean first, int timeout_in_secs) {
		
		utility = new Utility();

		//LOCAL BOARD
		B       = new MNKBoard(M,N,K); 

		//Possible gamestates
		utility.myWin   = first ? MNKGameState.WINP1 : MNKGameState.WINP2; 
		utility.yourWin = first ? MNKGameState.WINP2 : MNKGameState.WINP1;

		//Save if we are the first player
		utility.meFirst = first;

		//If the function takes longer than the set timeout an exception is thrown
		utility.TIMEOUT = timeout_in_secs;	

		//How the cells are marked in this match
		utility.myMark = utility.meFirst ? MNKCellState.P1 : MNKCellState.P2;
		utility.yourMark = utility.meFirst ? MNKCellState.P2 : MNKCellState.P1;

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

		Counters = new WinCounters(B);
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

		/*CANNOT IMMEDIATELY WIN, PROCEED WITH MINIMAX*/

		//Cycle through all possible cells
		//for(int i = 0; !utility.isTimeExpiring(); ++i){

			for(MNKCell d : FC) {

				//Mark this cell as player move (temp)
				B.markCell(d.i, d.j);

				Counters.UpdateAllCounters(B);
				
				//Apply minimax algorithm on the cell
				Integer MoveVal = miniMax(false, Integer.MIN_VALUE, Integer.MAX_VALUE, null, 10, d);

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
			
		//}
		

		//Return the result
		B.markCell(BestMove.i, BestMove.j); //Update local board with this move
		
		//DEBUG
		Debug.PrintSummary();

		return BestMove; //Update game board
	}

    public Integer miniMax(boolean maximizingPlayer, int alpha, int beta, Long previousHash, int depth, MNKCell lastMove){

		//DEBUG
		Debug.IncreaseEvaluations();

		//Base case, evaluation detected gameover or timeout soon
		if(B.gameState() != MNKGameState.OPEN || utility.isTimeExpiring() || depth <= 0)
		{
			//if(depth == 0)
			//	System.err.println("Depth reached 0");
			return utility.evaluateBoard2(B, Counters);
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

				//Update wincounters affected by this move 
				utility.updateWinCounters(B, Counters, current);

				//Check if already evaluated this game state
				long boardHash = previousHash != null ? ZT.diffHash(previousHash, current, utility.myMark) : ZT.computeHash(B);
				Integer boardValue = ZT.EvaluatedStates.getOrDefault(boardHash, null);

				//Already evaluated this game state
				if(boardValue != null){
					Debug.AlreadyEvaluated();
				}
				else{
					//Recursively call minmax on this board scenario
					boardValue = miniMax(false, alpha, beta, boardHash, --depth, current);

					//Add current value to HashSet for future use
					ZT.EvaluatedStates.put(boardHash, boardValue);	

					//Add simmetric board states to HashSet as they have the same static evaluation
					ZT.addSimmetryHashes(B, boardValue);
				}

				//Undo the move
				B.unmarkCell();
				
				//Restore wincounters before this move
				utility.updateWinCounters(B, Counters, current);

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

				//Update wincounters affected by this move 
				utility.updateWinCounters(B, Counters, current);

				//Check if already evaluated this game state
				long boardHash = previousHash != null ? ZT.diffHash(previousHash, current, utility.yourMark) : ZT.computeHash(B);
				Integer boardValue = ZT.EvaluatedStates.getOrDefault(boardHash, null);
				
				//Already evaluated this state
				if (boardValue != null) {
					Debug.AlreadyEvaluated();
				} else{

					//Recursively call minmax on this board scenario
					boardValue = miniMax(true, alpha, beta, boardHash, --depth, current);

					//Add current value to HashSet for future use
					ZT.EvaluatedStates.put(boardHash, boardValue);	

					//Add simmetric board states to HashSet as they have the same static evaluation
					ZT.addSimmetryHashes(B, boardValue);
				}

				//Undo the move
				B.unmarkCell();

				//Restore wincounters before this move
				utility.updateWinCounters(B, Counters, current);

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
		return "TestZob";
	}
}


