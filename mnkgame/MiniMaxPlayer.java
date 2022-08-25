/*
 * TODO: MAX_DEPTH in base a dimensioni area di gioco
 * 
 */

package mnkgame;

import java.util.HashSet;
import java.util.Random;

import javax.crypto.spec.DHPrivateKeySpec;
import javax.swing.text.html.MinimalHTMLWriter;

import mnkgame.MNKBoard;
import mnkgame.MNKCell;
import mnkgame.MNKGameState;
import mnkgame.MiniMaxMove;

/**
 * Totally random software player.
 */
public class MiniMaxPlayer  implements MNKPlayer {

	private int TIMEOUT;
	public int MAX_DEPTH = 5;
	private static int MAX_VALUE = 10;
	private MNKBoard B;
	private MNKGameState myWin;
	private MNKGameState yourWin;
	private boolean meFirst;

	//Zobrist table of previous game positions
	private long ZobristTable[][][];

	/**
   * Default empty constructor
   */
	public MiniMaxPlayer() {
	}

	public void initPlayer(int M, int N, int K, boolean first, int timeout_in_secs) {
		
		B       = new MNKBoard(M,N,K); //LOCAL BOARD
		myWin   = first ? MNKGameState.WINP1 : MNKGameState.WINP2; 
		yourWin = first ? MNKGameState.WINP2 : MNKGameState.WINP1;
		meFirst = first;
		TIMEOUT = timeout_in_secs;	

		//Initialize Zobrist
		ZobristTable = new long[M][N][2];
		initZobristTable();
	}

	long start = 0;
	public MNKCell selectCell(MNKCell[] FC, MNKCell[] MC) {

		//Start timer
		start = System.currentTimeMillis();

		//Set max depth based on how many free cell we have
		MAX_DEPTH = 25-FC.length;

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

		//Select center cell as first move
		if(MC.length == 0){
			MNKCell MiddleCell = new MNKCell(B.M/2, B.N/2);
			B.markCell(MiddleCell.i, MiddleCell.j);
			return MiddleCell;
		}

		//Use the first available cell if something goes wrong
		MNKCell BestMove = FC[0]; 

		//Initialize with relaxing technique
		Integer MaxMoveValue = Integer.MIN_VALUE;

		//DEBUG
		System.out.println("\n ####################################### \n");

		//If found the best move possible (immediate win) return immediately
		if((meFirst && MC.length >= (B.K*2-2)) || (!meFirst && MC.length >= (B.K*2-1))){

			System.err.println("START SEARCH FOR WIN");

			//Check if immediate win is possible
			MNKCell ImmediateWinCell = checkImmediateWin();

			//Exit the loop if found way not to lose
			if(ImmediateWinCell != null){
				System.err.println("FOUND IMMEDIATE WIN");
				B.markCell(ImmediateWinCell.i, ImmediateWinCell.j); //Update local board with this move
				return ImmediateWinCell;
			}
		}

		//Return immediately by intercepting player2 winning move before he does
		//Check if there are already enough symbols placed for player2 to reach victory
		if((meFirst && MC.length >= (B.K*2-2)) || (!meFirst && MC.length >= (B.K*2-3))){

			System.err.println("START SEARCH FOR LOSS");

			//Check if immediate loss is possible
			MNKCell PreventLossCell = checkImmediateLoss();

			//Exit the loop if found way not to lose
			if(PreventLossCell != null){
				System.err.println("FOUND IMMEDIATE LOSS");
				B.markCell(PreventLossCell.i, PreventLossCell.j); //Update local board with this move
				return PreventLossCell;
			}
		}
		

		//CANNOT IMMEDIATELY LOSE OR WIN, PROCEED WITH MINIMAX

		//Cycle through all possible cells
		for(MNKCell d : FC) {

			//Mark this cell as player move (temp)
			B.markCell(d.i, d.j);

			//Apply minimax algorithm on the cell
            Integer MoveVal = miniMax(MAX_DEPTH, false, Integer.MIN_VALUE, Integer.MAX_VALUE);

			//DEBUG
			System.err.println(d.i + " " + d.j + " = " + MoveVal);

			//Rollback
			B.unmarkCell();

			//Check if found a better move
			if(MoveVal > MaxMoveValue){
				MaxMoveValue = MoveVal;
				BestMove = d;
			}
		}

		//Return the result
		B.markCell(BestMove.i, BestMove.j); //Update local board with this move
		return BestMove; //Update game board
	}

	
    public Integer miniMax(int depth, boolean maximizingPlayer, int alpha, int beta){

		//Static evaluation of current game board
		int StaticEvaluation = evaluateBoard(depth);

		//Base case, evaluation detected gameover or depth limit reached
		if(B.gameState() != MNKGameState.OPEN || depth == 0){
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
				MaxValue = Math.max(MaxValue, miniMax(depth-1, false, alpha, beta));			

				//Undo the move
				B.unmarkCell();

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

				//Recursively call minmax on this board scenario
				MinValue = Math.min(MinValue, miniMax(depth-1, true, alpha, beta));

				//Undo the move
				B.unmarkCell();

				//Prune if better result was available before, no need to continue searching
				beta = Math.max(beta, MinValue);
				if (beta <= alpha || isTimeExpiring()) {
					return MinValue;
				}
			}

			return MinValue;
		}


    }

	//IDEA: at the beginning implement a normal minimax algorithm with 1 win 0 lose, next
	// try to use more values such as 0.5 if the move is making 2/3 marks lined up and player is going to win (opposite for adversary)
	public int evaluateBoard(int depth) {

		//Game over
        if(B.gameState() == myWin)
			return MAX_VALUE + depth; //Sum depth to prefer faster win 
		else if (B.gameState() == yourWin)
			return -MAX_VALUE - depth;

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
					System.err.println("P2 wins with " + current.i + " " + current.j);
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
		boolean Expiring = (System.currentTimeMillis()-start)/1000.0 > TIMEOUT*(99.0/100.0);
		//if(Expiring)
		// 	System.err.println("Timeout");
		return Expiring;
	}

	public void printGameState(){
		for (int i = 0; i < B.M; i++) { // Print gameboard
			for (int j = 0; j < B.N; j++) {
				System.out.println(i + " " + j + ": " + B.B[i][j].toString());
			}
		}
	}

	//Initialize the Zobrist table with random values
	public void initZobristTable() {
		for (int i = 0; i < B.M; ++i) {
			for (int j = 0; j < B.N; ++j) {
				for (int k = 0; k < B.K; ++k) {
					ZobristTable[i][j][k] = new Random().nextLong();
					System.out.println(ZobristTable[i][j][k] + "\n");
				}
			}
		}
	}

	private long opponentTurnHash = 81293; //Random number
	public long computeHash(boolean opponentTurn)
	{
		long hash = 0;

		//Hash differently based on whom turn it is
		if(opponentTurn)
			hash ^= opponentTurnHash;

		//Hash based on current table situation
		MNKCell MC[] = B.getMarkedCells();
		for(MNKCell current : MC) {
			hash ^= ZobristTable[current.i][current.j][current.hashCode()];//Bitwise XOR
		}
		

	public String playerName() {
		return "Mettaton NEO";
	}
}


