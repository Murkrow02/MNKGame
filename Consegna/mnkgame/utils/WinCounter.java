package mnkgame.utils;
import mnkgame.*;
import java.util.*;


public class WinCounter{

	public WinCounter(){
		CellsIndexes = new Hashtable<>();
	}

	public String Name;
    public int P1Score = 0;
    public int P2Score = 0;

	int P1ScoreHash = 0;
	int P2ScoreHash = 0;

    public boolean Full = false;
	public Long StateHash = null;
    public LinkedList<MNKCell> CellsToCheck;
	public Hashtable<String, Integer> CellsIndexes;

    //Updates P1 and P2 wins on this counter by analyzing cells controlled by this counter
	public void updateCounterScore(MNKBoard B, MNKCell lastMove, ZobristTableCounters ZTCounters){

		if(lastMove != null && StateHash != null){

			//Compute new state hash for this counter
			MNKCellState targetLastMoveState = B.cellState(lastMove.i,lastMove.j);
			StateHash = ZTCounters.diffHash(StateHash, targetLastMoveState, CellsIndexes.get(Utility.CellIdentifier(lastMove)));

			//Check if this state was already computed
			Integer[] CachedScores = ZTCounters.EvaluatedStates.getOrDefault(StateHash, null);
			if(CachedScores != null){
				P1Score = CachedScores[0];
				P2Score = CachedScores[1];
				return;
			}
		}
		else{
			StateHash = ZTCounters.computeHash(this, B);
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
			MNKCellState targetCellState = B.cellState(cellToCheck.i,cellToCheck.j);

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

		//Finished evaluating counter, save in HashTable for future use
		Integer[] EvaluatedScores = new Integer[]{ P1Score, P2Score };
		ZTCounters.EvaluatedStates.put(StateHash, EvaluatedScores);

	}

}