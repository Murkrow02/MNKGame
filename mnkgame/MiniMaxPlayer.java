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
	private MNKBoard B;
	private MNKGameState myWin;
	private MNKGameState yourWin;

	/**
   * Default empty constructor
   */
	public MiniMaxPlayer() {
	}

	public void initPlayer(int M, int N, int K, boolean first, int timeout_in_secs) {
		
		B       = new MNKBoard(M,N,K); //LOCAL BOARD
		myWin   = first ? MNKGameState.WINP1 : MNKGameState.WINP2; 
		yourWin = first ? MNKGameState.WINP2 : MNKGameState.WINP1;
		TIMEOUT = timeout_in_secs;	

	}

	
	public MNKCell selectCell(MNKCell[] FC, MNKCell[] MC) {
		
		//Update local board
		if(MC.length > 0) {
			MNKCell c = MC[MC.length-1]; // Recover move from opponent
			B.markCell(c.i,c.j);         // Save the last move in the local MNKBoard
		}
		
		//Use the first available cell if something goes wrong
		MNKCell BestMove = FC[0]; 

		//Initialize with relaxing technique
		Integer MaxMoveValue = Integer.MIN_VALUE;

		//Cycle through all possible cells
		System.out.println("\n ####################################### \n");

		//Print gameboard
		for(int i = 0; i < B.M; i++){
			for(int j = 0; j<B.N; j++){
				System.err.println(B.B[i][j].toString());
			}
		}
		
		for(MNKCell d : FC) {
			
			//Mark this cell as player move (temp)
			B.markCell(d.i, d.j);

			//Apply minimax algorithm on the cell
            Integer MoveVal = miniMax(0, false);


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

	//IDEA: at the beginning implement a normal minimax algorithm with 1 win 0 lose, next
	// try to use more values such as 0.5 if the move is making 2/3 marks lined up and player is going to win (opposite for adversary)
    public Integer miniMax(int depth, boolean maximizingPlayer){

		//Static evaluation of current game board
		
		//in previous call it was our turn and now we lost
		if(B.gameState() == myWin)
			return 1;
		else if (B.gameState() == yourWin)
			return -1;
		else if(B.gameState() == MNKGameState.DRAW)
			return 0;

		//Base case, user won, lost or draw
		// if(B.gameState() != MNKGameState.OPEN)
		// 	return CurrentEval;

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
				MaxValue = Math.max(MaxValue, miniMax(depth+1, false));			

				//Undo the move
				B.unmarkCell();
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
				MinValue = Math.min(MinValue, miniMax(depth+1, true));

				//Undo the move
				B.unmarkCell();
			}

			return MinValue;
		}


    }

	public int evaluateBoard() {

        return 0;
    }


	public String playerName() {
		return "Mettaton NEO";
	}
}


