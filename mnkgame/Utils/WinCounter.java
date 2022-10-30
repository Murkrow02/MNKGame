package mnkgame;

import mnkgame.Utility;
import mnkgame.Utils.ZobristTableCounters;

import javax.swing.plaf.nimbus.State;
import java.util.*;


//TODO: IMPLEMENT REVERSE HASH TO USE SIMMETRY

public class WinCounter{

	public WinCounter(){
		CellsIndexes = new Hashtable<>();
	}

	public String Name;
    public int P1Score = 0;
    public int P2Score = 0;

    public boolean Full = false;
	public long StateHash = 0;
    public LinkedList<MNKCell> CellsToCheck;
	public Hashtable<String, Integer> CellsIndexes;

    //Updates P1 and P2 wins on this counter by analyzing cells controlled by this counter
	public void updateCounterScore(MNKBoard B, MNKCell lastMove, ZobristTableCounters ZTCounters){

		if(lastMove != null){

			//Compute new state hash for this counter
			StateHash = ZTCounters.diffHash(StateHash, lastMove, CellsIndexes.get(Utility.CellIdentifier(lastMove)));

			//Check if this state was already computed
			Integer[] CachedScores = ZTCounters.EvaluatedStates.getOrDefault(StateHash, null);
			if(CachedScores != null){
				P1Score = CachedScores[0];
				P2Score = CachedScores[1];
				return;
			}
		}

		//Reset counter values
		P1Score = 0;
		P2Score = 0;

		//Check how many wins in one move, players can obtain by this state
		int P1_ImmediateWins = 0, P2_ImmediateWins = 0;

		int Free_Consecutive = 0,
			FreeOrP1_Consecutive = 0,
			FreeOrP2_Consecutive = 0,
			P1_Consecutive = 0,
			P2_Consecutive = 0;

		// Empty counter
		if (CellsToCheck == null)
			return;

		// Check all the cells controlled by this WinCounter
		for (var cellToCheck : CellsToCheck) {

			//Check current cell marked state
			MNKCellState targetCellState = B.B[cellToCheck.i][cellToCheck.j];

			//P1 controls this cell
			if (targetCellState == MNKCellState.P1){

				//Reset free streak and P2 streak
				FreeOrP2_Consecutive = 0;
				P2_Consecutive = 0;

				//Increment P1 streak
				P1_Consecutive++;
				FreeOrP1_Consecutive++;
			}

			//P2 controls this cell
			else if (targetCellState == MNKCellState.P2){

				//Reset free streak and P1 streak
				FreeOrP1_Consecutive = 0;
				P1_Consecutive = 0;

				//Increment P2 streak
				P2_Consecutive++;
				FreeOrP2_Consecutive++;
			}

			//Free cell
			else{
				FreeOrP1_Consecutive++;
				FreeOrP2_Consecutive++;
				Free_Consecutive++;
				P1_Consecutive = 0;
				P2_Consecutive = 0;


//				//Fakely mark this cell to determine if one time victory is possible from this state
//				if(B.gameState == MNKGameState.OPEN){
//					B.markCell(cellToCheck.i, cellToCheck.j);
//					if(B.gameState == MNKGameState.WINP1)
//						P1_ImmediateWins++;
//					else if (B.gameState() == MNKGameState.WINP2)
//						P2_ImmediateWins++;
//
//					//Rollback
//					B.unmarkCell();
//
//					//If any of the two players has more than one win with this move, win is certain so return max value
//					if(P1_ImmediateWins > 1){
//						mnkgame.Debug.PrintGameState(B);
//						P1Score = Integer.MAX_VALUE;
//						P2Score = 0;
//						return;
//					}
//					if(P2_ImmediateWins > 1){
//						mnkgame.Debug.PrintGameState(B);
//						P2Score = Integer.MAX_VALUE;
//						P1Score = 0;
//						return;
//					}
//				}
			}


			//Evaluations
			if (FreeOrP1_Consecutive >= B.K) {

				//Win probability is notably higher for multiple signs consequently placed
				P1Score += Math.pow(10,P1_Consecutive);

				//Reset streaks
				P1_Consecutive = 0; FreeOrP1_Consecutive = 0;
			}
			if (FreeOrP2_Consecutive >= B.K) {

				//Win probability is notably higher for multiple signs consequently placed
				P2Score += Math.pow(10,P2_Consecutive);

				//Reset streaks
				P2_Consecutive = 0; FreeOrP2_Consecutive = 0;
			}

			if(targetCellState != MNKCellState.FREE)
				Free_Consecutive = 0;


			// Check if no need to go further (implement later by saving cell controlled
			// count and incrementing local variable)
			// int left_until_end = B.M-j; //How many spaces until end of row
			// if(left_until_end+WE_OK<B.K && left_until_end+HIM_OK<B.K){

			// //Neither us nor him can win on this row, no need to go further
			// continue;
			// }
		}

	}

}