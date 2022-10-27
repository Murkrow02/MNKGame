package mnkgame;

import java.util.*;

public class WinCounter{

	public String Name;

    public int P1Score = 0;
    public int P2Score = 0;

    public boolean Full = false;
    public LinkedList<MNKCell> CellsToCheck;

    //Updates P1 and P2 wins on this counter by analyzing cells controlled by this counter
	public void updateCounterScore(MNKBoard B){

		//Reset counter values
		P1Score = 0;
		P2Score = 0;

		int P1_FREE = 0, P2_FREE = 0, P1_PLACED = 0, P2_PLACED = 0;

		//Increment multipliers each time multiple symbols are placed next to each other
		int MultiplierP1 = 0;
		int MultiplierP2 = 0;

		// Empty counter
		if (CellsToCheck == null)
			return;

		// Check all the cells controlled by this WinCounter
		for (var cellToCheck : CellsToCheck) {

			MNKCellState targetCellState = B.B[cellToCheck.i][cellToCheck.j];

			//P1 count
			if (targetCellState == MNKCellState.P2){

				//P2 sign on this cell, reset
				P1_FREE = 0;
				P1_PLACED = 0;
			}
			else{

				//Check second condition
				//if(conditionsMetP1 == 1 && P1_PLACED == B.K-2)
				//	conditionsMetP1 = 2;

				//P1 sign on this cell
				if(targetCellState == MNKCellState.P1)
					P1_PLACED++;

				//FREE Cell
				if(targetCellState == MNKCellState.FREE){

					P1_FREE++;

					//First condition for certain win is met
					//if(conditionsMetP1 == 0)
					//	conditionsMetP1 = 1;

					//Check for third condition
					//if (conditionsMetP1 == 2){
					//	P1Score = Integer.MAX_VALUE;
					//	return;
					//}
				}

				//P1 has control of this cell
				if(targetCellState == MNKCellState.P1)
					MultiplierP1++;
			}

			// P2 count
			if (targetCellState == MNKCellState.P1){

				//P2 sign on this cell, reset
				P2_FREE = 0;
				P2_PLACED = 0;
			}
			else{

				//Check second condition
				//if(conditionsMetP2 == 1 && P2_PLACED == B.K-2)
				//	conditionsMetP2 = 2;

				//P2 sign on this cell
				if(targetCellState == MNKCellState.P2)
					P2_PLACED++;

				//FREE Cell
				if(targetCellState == MNKCellState.FREE){

					P2_FREE++;

					//Check for first condition
					//if(conditionsMetP2 == 0)
					//	conditionsMetP2 = 1;

					//Check for third condition
					//if (conditionsMetP2 == 2){
					//	//Third condition is met!!! Add super bonus score1!!!111!1 (and return)
					//	P2Score = Integer.MAX_VALUE;
					//	return;
					//}
				}

				//P2 has control of this cell
				if(targetCellState == MNKCellState.P2)
					MultiplierP2++; //We have multiple

			}

			// Evaluations
			if (P1_PLACED + P1_FREE >= B.K) {
				P1Score += Math.pow(10,MultiplierP1);
				P1_PLACED = 0; P1_FREE = 0; //Reset
			}
			if (P2_PLACED + P2_FREE >= B.K) {
				P2Score += Math.pow(10,MultiplierP2);
				P2_PLACED = 0; P2_FREE = 0; //Reset
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