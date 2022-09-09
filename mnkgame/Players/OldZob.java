package mnkgame;

import java.util.Hashtable;
import mnkgame.*;

/*
 * TODO: Insert all possible win/loss already in hash table
 *  Negamax negascout
 */

public class OldZob  implements MNKPlayer {

	private int TIMEOUT;
	private double DEFAULT_TRIGGER_TIMEOUT_PERCENTAGE=98;
	private double TRIGGER_TIMEOUT_PERCENTAGE=98;
	//public int MAX_DEPTH = 5;
	private static final int MAX_VALUE = 10;
	private MNKBoard B;
	private MNKGameState myWin;
	private MNKGameState yourWin;
	private MNKCellState myState;
	private MNKCellState yourState;
	private boolean meFirst;

	/* This tri-dimensional array stores M*N random values, is used 
	   by the game board hashing function to create a unique hash of 
	   each possible game state
	*/
	public ZobristTable ZT;

	/**
   * Default empty constructor
   */
	public OldZob() {
	}

	public void initPlayer(int M, int N, int K, boolean first, int timeout_in_secs) {
		
		B       = new MNKBoard(M,N,K); //LOCAL BOARD
		myWin   = first ? MNKGameState.WINP1 : MNKGameState.WINP2; 
		yourWin = first ? MNKGameState.WINP2 : MNKGameState.WINP1;
		meFirst = first;
		TIMEOUT = timeout_in_secs;	
		myState = meFirst ? MNKCellState.P1 : MNKCellState.P2;
		yourState = meFirst ? MNKCellState.P2 : MNKCellState.P1;

		//Initialize Zobrist table
		ZT = new ZobristTable(M,N);

		//Initialize hash table 
		int MaxTableSize = 1<<20; // 2^20
		int TableSize = 1<<M*N; // 2^M*N
		if(TableSize > MaxTableSize)
			TableSize = MaxTableSize;
		ZT.EvaluatedStates = new Hashtable<Long, Integer>(TableSize);

		//Calculate after how much time trigger timeout warning
		//DEFAULT_TRIGGER_TIMEOUT_PERCENTAGE = 99-(M*N/5);
	}

	long timerStart = 0;
	public MNKCell selectCell(MNKCell[] FC, MNKCell[] MC) {

		//Start timer
		timerStart = System.currentTimeMillis();

		//Update local board
		if(MC.length > 0) {
			MNKCell c = MC[MC.length-1]; // Recover move from opponent
			B.markCell(c.i,c.j);         // Save the last move in the local MNKBoard
		}
		
		// If there is just one possible move, return immediately
		if(FC.length == 1)
		{
			B.markCell(FC[0].i, FC[0].j);
			return FC[0];
		}

		//Use the first available cell if something goes wrong
		MNKCell BestMove = FC[0]; 

		//Initialize with relaxing technique
		Integer MaxMoveValue = Integer.MIN_VALUE;

		//DEBUG
		Debug.Reset();

		//If found the best move possible (immediate win) return immediately
		if((meFirst && MC.length >= (B.K*2-2)) || (!meFirst && MC.length >= (B.K*2-1))){

			//System.err.println("START SEARCH FOR WIN");

			//Check if immediate win is possible
			MNKCell ImmediateWinCell = checkImmediateWin();

			//Exit the loop if found way not to lose
			if(ImmediateWinCell != null){
				//System.err.println("FOUND IMMEDIATE WIN");
				B.markCell(ImmediateWinCell.i, ImmediateWinCell.j); //Update local board with this move
				return ImmediateWinCell;
			}
		}

		//Return immediately by intercepting player2 winning move before he does
		MNKCell PreventLossCell = null;
		
		//Check if there are already enough symbols placed for player2 to reach victory
		if((meFirst && MC.length >= (B.K*2-2)) || (!meFirst && MC.length >= (B.K*2-3))){

			//System.err.println("START SEARCH FOR LOSS");

			//Check if immediate loss is possible
			PreventLossCell = checkImmediateLoss();
		}
		

		//CANNOT IMMEDIATELY WIN, PROCEED WITH MINIMAX

		//Cycle through all possible cells
		for(MNKCell d : FC) {

			//Mark this cell as player move (temp)
			B.markCell(d.i, d.j);

			//Apply minimax algorithm on the cell
            Integer MoveVal = miniMax(false, Integer.MIN_VALUE, Integer.MAX_VALUE, null, 0);

			//DEBUG
			Debug.PrintMiddleCicle(B, d, MoveVal);
			evaluations = 0;

			//Rollback
			B.unmarkCell();

			//Check if found a better move
			if(MoveVal > MaxMoveValue){
				MaxMoveValue = MoveVal;
				BestMove = d;
			}

			//Check timeout
			if(isTimeExpiring())
				break;
			else
				TRIGGER_TIMEOUT_PERCENTAGE = DEFAULT_TRIGGER_TIMEOUT_PERCENTAGE; //Reset default timeout trigger
		}

		//Select center cell as first move (placed here to do computation and fill hashtable anyway before first move)
		if(MC.length == 0){
			MNKCell MiddleCell = new MNKCell(B.M/2, B.N/2);
			B.markCell(MiddleCell.i, MiddleCell.j);
			return MiddleCell;
		}

		//Select cell that would make the opponent win as is for sure the best move (placed here to do computation and fill hashtable anyway)
		if(PreventLossCell != null){
			//System.err.println("FOUND IMMEDIATE LOSS");
			B.markCell(PreventLossCell.i, PreventLossCell.j); //Update local board with this move
			return PreventLossCell;
		}

		//Return the result
		B.markCell(BestMove.i, BestMove.j); //Update local board with this move
		return BestMove; //Update game board
	}

	int evaluations = 0; //debug
    public Integer miniMax(boolean maximizingPlayer, int alpha, int beta, Long previousHash, int depth){

		//DEBUG
		evaluations++;

		/*Each time we enter a recursive call, timeout should trigger 
		* a little bit earlier as we need to rollback all calls on stack when first triggered
		*/
		TRIGGER_TIMEOUT_PERCENTAGE = DEFAULT_TRIGGER_TIMEOUT_PERCENTAGE - (depth * 0.5);

		//Static evaluation of current game board
		int StaticEvaluation = evaluateBoard();

		//Base case, evaluation detected gameover or depth limit reached
		if(B.gameState() != MNKGameState.OPEN)// || depth == 0)
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

				//Check if already evaluated this game state
				long boardHash = previousHash != null ? ZT.diffHash(previousHash, current, myState) : ZT.computeHash(B);
				Integer boardValue = ZT.EvaluatedStates.getOrDefault(boardHash, null);
				if(boardValue != null){
					//System.out.println("Already evaluated"); //Already evaluated this state, no need to proceed with minimax
				}
				else{
					//Recursively call minmax on this board scenario
					boardValue = miniMax(false, alpha, beta, boardHash, depth++);

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
				if (alpha >= beta || isTimeExpiring()) {
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
				long boardHash = previousHash != null ? ZT.diffHash(previousHash, current, yourState) : ZT.computeHash(B);
				Integer boardValue = ZT.EvaluatedStates.getOrDefault(boardHash, null);
				if (boardValue != null) {
					//System.out.println("Already evaluated"); //Already evaluated this state, no need to proceed with minimax
				} else{

					//Recursively call minmax on this board scenario
					boardValue = miniMax(true, alpha, beta, boardHash, depth++);

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
				if (beta <= alpha || isTimeExpiring()) {
					return MinValue;
				}
			}

			return MinValue;
		}


    }

	//IDEA: at the beginning implement a normal minimax algorithm with 1 win 0 lose, next
	// try to use more values such as 0.5 if the move is making 2/3 marks lined up and player is going to win (opposite for adversary)
	public int evaluateBoard() {

		//Game over
        if(B.gameState() == myWin)
			return MAX_VALUE; // + depth; //Sum depth to prefer faster win (ignored as method to check immediate win is present)
		else if (B.gameState() == yourWin)
			return -MAX_VALUE; // - depth;

		//Draw or not defined
		return 0;

    }

	//Return the only cell to select to prevent immediate loss if possible
	public MNKCell checkImmediateLoss(){

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
	public MNKCell checkImmediateWin(){

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
		//if(Expiring)
		// 	System.err.println("Timeout");
		return Expiring;
	}

	

	

	public String playerName() {
		return "Mettaton NEO";
	}
}


