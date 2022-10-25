package mnkgame;

import java.util.*;

public class WinCounter{

	public String Name;
    public int P1Score = 0;
    public int P2Score = 0;

    public boolean Full = false;
    public LinkedList<MNKCell> CellsToCheck;

    //Updates P1 and P2 wins on this counter by analyzing cells controlled by this counter

	/*
	* NOTES:
	*
	*
	* */
	public void updateCounterWins(MNKBoard B){

		//Reset counter values
		P1Score = 0;
		P2Score = 0;

		int P1_OK = 0;
		int P2_OK = 0;

		int MultiplierP1 = 0;
		int MultiplierP2 = 0;

		// Empty counter
		if (CellsToCheck == null)
			return;

		// Check all the cells controlled by this WinCounter
		for (var cellToCheck : CellsToCheck) {



			MNKCellState targetCellState = B.B[cellToCheck.i][cellToCheck.j];

			// P1 count
			if (targetCellState == MNKCellState.P2)
				P1_OK = 0; // P2 sign on this cell, reset
			else{

				P1_OK++; // Free or P1 sign, can go on searching for possible victory

				//P1 has control of this cell
				if(targetCellState == MNKCellState.P1)
					MultiplierP1++; //We have multiple
			}



			// P2 count
			if (targetCellState == MNKCellState.P1)
				P2_OK = 0; // P1 sign on this cell, reset
			else{

				P2_OK++; // Free or P1 sign, can go on searching for possible victory

				//P1 has control of this cell
				if(targetCellState == MNKCellState.P2)
					MultiplierP2++; //We have multiple

			}

			// Evaluations
			if (P1_OK == B.K) {
				P1Score += Math.pow(10,MultiplierP1);
				P1_OK = 0;
			}
			if (P2_OK == B.K) {
				P2Score += Math.pow(10,MultiplierP2);
				P2_OK = 0;
			}

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