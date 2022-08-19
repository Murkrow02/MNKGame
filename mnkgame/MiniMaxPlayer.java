package mnkgame;

import java.util.Random;

import javax.swing.text.html.MinimalHTMLWriter;

import mnkgame.MNKBoard;
import mnkgame.MNKCell;
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
		
		B       = new MNKBoard(M,N,K);
		myWin   = first ? MNKGameState.WINP1 : MNKGameState.WINP2; 
		yourWin = first ? MNKGameState.WINP2 : MNKGameState.WINP1;
		TIMEOUT = timeout_in_secs;	

	}

	
	public MNKCell selectCell(MNKCell[] FC, MNKCell[] MC) {
		

		MiniMaxMove result = miniMax(null, true);
		
		return result.Cell;
	}

	//IDEA: at the beginning implement a normal minimax algorithm with 1 win 0 lose, next
	// try to use more values such as 0.5 if the move is making 2/3 marks lined up and player is going to win (opposite for adversary)
    public MiniMaxMove miniMax(MNKCell lastCellSelected, boolean maximizingPlayer){

		//Detect gameover
		if(B.gameState() == myWin)
		{
			B.unmarkCell();
			return new MiniMaxMove(1, lastCellSelected);
		}
		else if(B.gameState() == yourWin)
		{
			B.unmarkCell();
			return new MiniMaxMove(-1, lastCellSelected);
		}

		if(maximizingPlayer){

			//Relax technique
			MiniMaxMove MaxEval = new MiniMaxMove(Integer.MIN_VALUE, null);

			int rollback = 0;
			for(int i = 0; i < B.getFreeCells().length; i++){

				rollback++;
				
				//Mark free cell
				MNKCell Marked = B.getFreeCells()[i];
				B.markCell(Marked.i, Marked.j);

				//Eval free cell value
				MiniMaxMove CurrentEval = miniMax(Marked, false);

				//Choose between now evaluated value and maximum evaluated value
				MaxEval = MaxEval.MoveValue > CurrentEval.MoveValue ? MaxEval : CurrentEval;
			}

			//Rollback
			for(int i = 0; i< rollback;i++){
				B.unmarkCell();
			}

			return MaxEval;
		}else{

			//Relax technique
			MiniMaxMove MinEval = new MiniMaxMove(Integer.MAX_VALUE, null);

			int rollback = 0;
			for(int i = 0; i < B.getFreeCells().length; i++){
				
				rollback++;
				
				//Mark free cell
				MNKCell Marked = B.getFreeCells()[i];
				B.markCell(Marked.i, Marked.j);

				//Eval free cell value
				MiniMaxMove CurrentEval = miniMax(Marked, true);

				//Choose between now evaluated value and maximum evaluated value
				MinEval = MinEval.MoveValue < CurrentEval.MoveValue ? MinEval : CurrentEval;
			}

			//Rollback
			for(int i = 0; i< rollback;i++){
				B.unmarkCell();
			}

			return MinEval;

		}

    }

	public Integer evaluateBoard(){
		return 1;

	} 

	public String playerName() {
		return "Mettaton NEO";
	}
}


