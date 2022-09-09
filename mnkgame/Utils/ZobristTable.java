package mnkgame;

import java.util.Hashtable;
import java.util.Random;
import mnkgame.MNKBoard;
import mnkgame.Utility;
import mnkgame.CustomMNKBoard;
import mnkgame.MNKCell;
import mnkgame.MNKCellState;

public class ZobristTable {
    
    /* Insert and access quickly (O(1)) to already evaluated game states
	 * uniquely identified by Zobrist Hash function
	 */
	public Hashtable<Long, Integer> EvaluatedStates;

    //Zobrist table
	public long ZT[][][];

    // Initialize the Zobrist table with random values
    public ZobristTable(int M, int N) {

        //Init table
        ZT = new long[M][N][2];

        //Fill with random position
        for (int i = 0; i < M; ++i) {
            for (int j = 0; j < N; ++j) {
                for (int k = 0; k < 2; ++k) {
                    ZT[i][j][k] = new Random().nextLong();
                }
            }
        }
    }

    /*
     * Main Zobrist function, cycles through all the given pieces and 
     * starting from zero, bitwise XOR with the hash with the randomly generated 
     * number from the constructor given to each place on board
     */
    public long computeHash(MNKBoard B) {

        long hash = 0;

        // Hash based on current table situation
        MNKCell MC[] = B.getMarkedCells();
        for (MNKCell current : MC) {
            hash ^= ZT[current.i][current.j][pieceId(current.state)];// Bitwise XOR
        }

        return hash;
    }

    /*
     * Function used to speed up hash calculation, if we already have a hash
     * and only one piece changed then we can immediately calculate new hash
     */
    public long diffHash(long previousHash, MNKCell newCell, MNKCellState state){
        return previousHash ^= ZT[newCell.i][newCell.j][pieceId(state)];
    }

    public void addSimmetryHashes(MNKBoard b, int value){

		long xHash = 0;
        long yHash = 0;
        long xyHash = 0;
		int lastColumnIndex = b.N-1;
        int lastRowIndex = b.M-1;

		//DEBUG
		// CustomMNKBoard simmetricXBoard = new CustomMNKBoard(b.M, b.N, b.K);
        // CustomMNKBoard simmetricYBoard = new CustomMNKBoard(b.M, b.N, b.K);
        // CustomMNKBoard simmetricXYBoard = new CustomMNKBoard(b.M, b.N, b.K);

		//Check if even columns
		boolean evenColumns = b.N%2 == 0;
        boolean evenRows = b.M%2 == 0;
		int middleColumn = b.N/2;
        int middleRow = b.M/2;

		//Cycle through already marked cells
		for(MNKCell current : b.MC) {

			//Reflect on X axys
			if(current.j != middleColumn || evenColumns)
			{
                //Move mark from the left side to right and vice-versa
			    MNKCell simmetricCell = new MNKCell(current.i, lastColumnIndex-current.j);

                //Debug
                //simmetricXBoard.customMarkCell(simmetricCell.i, simmetricCell.j, current.state);

                //Add new cell to final hash
			    xHash = diffHash(xHash, simmetricCell, current.state);
            }
            //No need to transpose
            else{

                //Debug
                //simmetricXBoard.customMarkCell(current.i, current.j, current.state); 

                //Add default cell to final hash
			    xHash = diffHash(xHash, current, current.state);
            }

            //Reflect on Y axis
            if(current.i != middleRow || evenRows){

                //Move mark from the upper side to bottom and vice-versa
                MNKCell simmetricCell = new MNKCell(lastRowIndex-current.i, current.j);

                //Debug
                //simmetricYBoard.customMarkCell(simmetricCell.i, simmetricCell.j, current.state);

                //Add new cell to final hash
			    yHash = diffHash(yHash, simmetricCell, current.state);
            }else{

                //Debug
                //simmetricYBoard.customMarkCell(current.i, current.j, current.state); //Debug

                //Add default cell to final hash
			    yHash = diffHash(yHash, current, current.state);
            }

            //Reflect on XY if square table
            if(b.M == b.N){

                //Reflect on XY
                MNKCell simmetricCell = new MNKCell(lastRowIndex-current.i, lastColumnIndex-current.j);

                //Debug
                //simmetricXYBoard.customMarkCell(simmetricCell.i, simmetricCell.j, current.state); 

                //Add new cell to final hash
                xyHash = diffHash(xyHash, simmetricCell, current.state);
            }

			
		}

        //DEBUG
        // System.out.println("SIMMETRIES: \n");
        // Utility.printGameState(simmetricXBoard);
        // System.out.println("\n");
        // Utility.printGameState(simmetricYBoard);
        // System.out.println("\n");
        // Utility.printGameState(simmetricXYBoard);

		//Add all calculated hashes to evaluated states
        EvaluatedStates.put(xHash, value);
        EvaluatedStates.put(yHash, value);
        EvaluatedStates.put(xyHash, value);
	}

    // If the cell is occupied by P1 return 0, otherwhise (P2) return 1
    private int pieceId(MNKCellState state) {
        if (state == MNKCellState.P1)
            return 0;
        else if (state == MNKCellState.P2)
            return 1;
        else
            return -1;
    }


}
