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
	public long[][][] ZT;

    // Initialize the Zobrist table with random values
    public ZobristTable(int M, int N) {

        /*
        * M is the number of rows
        * N is the number of cols
        * 2 is the number of possible pieces
        * 2 is the number of possible turns
        * */
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

		long Hash90 = 0;    // 90 degrees rotation
        long Hash180 = 0;   // 180 degrees rotation
        long Hash270 = 0;   // 270 degrees rotation
        long HashS = 0;     // Specular of original
        long Hash90S = 0;    // 90 degrees rotation specular
        long Hash180S = 0;   // 180 degrees rotation specular
        long Hash270S = 0;   // 270 degrees rotation specular
		int lastColumnIndex = b.N-1;
        int lastRowIndex = b.M-1;

		//DEBUG
		// CustomMNKBoard simmetricXBoard = new CustomMNKBoard(b.M, b.N, b.K);
        // CustomMNKBoard simmetricYBoard = new CustomMNKBoard(b.M, b.N, b.K);
        // CustomMNKBoard simmetricXYBoard = new CustomMNKBoard(b.M, b.N, b.K);


        //Find middle columns and rows
		int middleColumn = b.N/2;
        int middleRow = b.M/2;

		//Cycle through already marked cells
		for(MNKCell c : b.MC) {
			
            //Only square board rotations
            if(b.N == b.M){

                //Hash90
                MNKCell rotate90 = new MNKCell(c.j, b.N - c.i-1);
                Hash90 = diffHash(Hash90, rotate90, c.state);

                //Hash90S
                MNKCell rotate90S = new MNKCell(b.N - c.j - 1, b.M - c.i - 1);
                Hash90S = diffHash(Hash90S, rotate90S, c.state);

                //Hash 270
                MNKCell rotate270 = new MNKCell(b.M - c.j - 1, c.i);
                Hash270 = diffHash(Hash270, rotate270, c.state);

                //Hash 270S
                MNKCell rotate270S = new MNKCell(c.j, c.i);
                Hash270S = diffHash(Hash270S, rotate270S, c.state);
            }

            //Square board and rect board rotations
            
            //Hash180
            MNKCell rotate180 = new MNKCell(b.M - c.i - 1, b.N - c.j - 1);
            Hash180 = diffHash(Hash180, rotate180, c.state);

            //Hash180S
            MNKCell rotate180S = new MNKCell(b.M - c.i - 1, c.j);
            Hash180S = diffHash(Hash180S, rotate180S, c.state);

            //Hash simmetrical
            MNKCell rotateS = new MNKCell(c.i, b.N-c.j-1);
            HashS = diffHash(HashS, rotateS, c.state);

		}

        //DEBUG
        // System.out.println("SIMMETRIES: \n");
        // Utility.printGameState(simmetricXBoard);
        // System.out.println("\n");
        // Utility.printGameState(simmetricYBoard);
        // System.out.println("\n");
        // Utility.printGameState(simmetricXYBoard);

		//Add all calculated hashes to evaluated states
        if(b.N == b.M){
            EvaluatedStates.put(Hash90, value);
            EvaluatedStates.put(Hash90S, value);
            EvaluatedStates.put(Hash270, value);
            EvaluatedStates.put(Hash270S, value);
        }

        EvaluatedStates.put(Hash180, value);
        EvaluatedStates.put(HashS, value);
        EvaluatedStates.put(Hash180S, value);

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

    // Used only by diffHash, if placed cell for P1 then is turn of P2 and vice-versa
    private int turnId(MNKCellState state) {
        if (state == MNKCellState.P1)
            return 1;
        else if (state == MNKCellState.P2)
            return 0;
        else
            return -1;
    }


}
