package mnkgame;

import java.util.Random;
import mnkgame.MNKBoard;
import mnkgame.MNKCell;
import mnkgame.MNKCellState;

public class ZobristTable {
    
    //Zobrist table of previous game positions
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
    public long addHash(long previousHash, MNKCell newCell, MNKCellState state){
        return previousHash ^= ZT[newCell.i][newCell.j][pieceId(state)];
    }

    public long simmetryHash(MNKBoard b){

		long finalHash = 0;
		int lastColumnIndex = b.N-1;

		//Create custom board to calculate hash
		//CustomMNKBoard simmetricBoard = new CustomMNKBoard(b.M, b.N, b.K);

		//Check if even columns
		boolean evenColumns = b.N%2 == 0;
		int middleColumn = evenColumns ? b.N/2 : (b.N/2)+1;

		//Cycle through already marked cells
		for(MNKCell current : b.MC) {

			//If we are on middle column on non-even column board do nothing
			if(current.j == middleColumn && !evenColumns)
				continue;

			//Move mark from the left side to right and vice-versa
			MNKCell simmetricCell = new MNKCell(current.i, lastColumnIndex-current.j);

			//Add new cell to final hash
			finalHash = addHash(finalHash, simmetricCell, current.state);
		}

		return finalHash;
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
