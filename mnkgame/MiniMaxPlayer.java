package mnkgame;

import java.util.Random;

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

	private Integer EvalMaxValue;

	/**
   * Default empty constructor
   */
	public MiniMaxPlayer() {
	}

	public void initPlayer(int M, int N, int K, boolean first, int timeout_in_secs) {
		
		B       = new MNKBoard(M,N,K);
		myWin   = first ? MNKGameState.WINP1 : MNKGameState.WINP2; 
		yourWin = first ? MNKGameState.WINP2 : MNKGameState.WINP1;
		TIMEOUT = timeout_in_secs;	

	}

	
	public MNKCell selectCell(MNKCell[] FC, MNKCell[] MC) {
		

		MiniMaxMove result = miniMax(0, null, true);
		
		return result.Cell;
	}

	//IDEA: at the beginning implement a normal minimax algorithm with 1 win 0 lose, next
	// try to use more values such as 0.5 if the move is making 2/3 marks lined up and player is going to win (opposite for adversary)
    public Integer miniMax(int depth, boolean maximizingPlayer){

		//Static evaluation of current game board
		Integer CurrentEval = evaluateBoard();

		//Base case, user won, lost or draw
		if(B.gameState() != MNKGameState.OPEN)
			return CurrentEval;

		//Our turn (Maximizing)
		if(maximizingPlayer){

			//Relax technique, assume the worst case scenario and relax on each step
			Integer MaxValue = Integer.MIN_VALUE;

			//Cycle through all possible moves
			for(int i = 0; i < B.getFreeCells().length; ++i){

				//Mark this cell as player move
				MNKCell CurrentMove = B.getFreeCells()[i];
				B.markCell(CurrentMove.i, CurrentMove.j);

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
			for(int i = 0; i < B.getFreeCells().length; ++i){

				//Mark this cell as player move
				MNKCell CurrentMove = B.getFreeCells()[i];
				B.markCell(CurrentMove.i, CurrentMove.j);

				//Recursively call minmax on this board scenario
				MinValue = Math.min(MinValue, miniMax(depth+1, true));

				//Undo the move
				B.unmarkCell();
			}

			return MinValue;
		}


    }

	public Integer evaluateBoard(){
		return 1;

	} 

	public String playerName() {
		return "Mettaton NEO";
	}
}


