package mnkgame;

import mnkgame.Debug;
import org.w3c.dom.css.Counter;

import java.util.Hashtable;

/*
 * TODO: Insert all possible win/loss already in hash table
 *  Negamax negascout
 *  Sometime AI is dumb and does not prevent the opponent to place more symbols near each other
 * 	Use try catch to immediately stop minimax when timeout found
 */

public class Dummy implements MNKPlayer {

	private MNKBoard B;
	private mnkgame.Utility utility;

	/* This tri-dimensional array stores M*N random values, is used
	   by the game board hashing function to create a unique hash of
	   each possible game state
	*/
	public mnkgame.ZobristTable ZT;

	public mnkgame.WinCounters Counters;

	/*
	 * Count how many times the iteration deepening reached a leaf state
	 * used to stop when we reached all leaf states
	 * */
	Integer ReachedLeafsCount = 0;

	/**
	 * Default empty constructor
	 */
	public Dummy() {
	}

	public void initPlayer(int M, int N, int K, boolean first, int timeout_in_secs) {

		utility = new mnkgame.Utility();

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
		ZT = new mnkgame.ZobristTable(M,N);

		//Initialize hash table
		int MaxTableSize = 1<<20; // 2^20
		int TableSize = 1<<M*N; // 2^M*N, this size is related to board size

		//Creating a huge hashtable could crash the application
		if(TableSize > MaxTableSize)
			TableSize = MaxTableSize;

		//Init hashtable with desired size
		ZT.EvaluatedStates = new Hashtable<Long, Integer>(TableSize);

		//Initialize wincounters and respective cell references
		Counters = new mnkgame.WinCounters(B);

		//First scan of the whole board, foreach WinCounter check how many wins player can reach
		Counters.UpdateAllCounters(B);
	}

	public MNKCell selectCell(MNKCell[] FC, MNKCell[] MC) {





		return FC[0];
	}



	public String playerName() {
		return "Test";
	}
}


