package mnkgame;

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
	private static int MAX_DEPTH = 6;
	private static int MAX_VALUE = 10;
	private MNKBoard B;
	private MNKGameState myWin;
	private MNKGameState yourWin;
	private bool meFirst;

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

	}

	
	public MNKCell selectCell(MNKCell[] FC, MNKCell[] MC) {
		
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
		System.out.println("\n ####################################### \n");
		for(int i = 0; i < B.M; i++){ 		//Print gameboard
			for(int j = 0; j<B.N; j++){
				System.err.println(B.B[i][j].toString());
			}
		}

		//Cycle through all possible cells
		bool alreadyCheckedImmediateLoss = false;
		for(MNKCell d : FC) {
			
			//Mark this cell as player move (temp)
			B.markCell(d.i, d.j);

			//Apply minimax algorithm on the cell
            Integer MoveVal = miniMax(MAX_DEPTH, false, Integer.MIN_VALUE, Integer.MAX_VALUE);

			//DEBUG
			System.err.println(d.i + " " + d.j + " = " + MoveVal);

			//Rollback
			B.unmarkCell();

			//If found the best move possible (immediate win) return immediately
			if(MoveVal == MAX_VALUE + MAX_DEPTH)
			{
				BestMove = d;
				break; //Exit the loop
			}else if(!alreadyCheckedImmediateLoss){

				//If player2 is able to win (there are already enough symbols placed for him to reach vicory) intercept his winning move before he does
				if((meFirst && MC.length >= (B.K*2-1)) || (!meFirst && MC.length >= (B.k*2-2))){

					//Check if 
					MNKCell PreventLossCell = checkImmediateLoss();
					alreadyCheckedImmediateLoss = true;

					//Exit the loop if found way not to lose
					if(PreventLossCell != null){
						BestMove = PreventLossCell;
						break;
					}
				}
			}

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
				if (alpha >= beta) {
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
				if (beta <= alpha) {
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
		for(MNKCell current : FC) {
		
		}

	}


	public String playerName() {
		return "Mettaton NEO";
	}
}


