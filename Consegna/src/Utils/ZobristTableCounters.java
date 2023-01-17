package mnkgame.Utils;

import mnkgame.MNKBoard;
import mnkgame.MNKCell;
import mnkgame.MNKCellState;
import mnkgame.WinCounter;

import java.util.Hashtable;
import java.util.List;
import java.util.Random;

public class ZobristTableCounters {

    /* Insert and access quickly (O(1)) to already evaluated game states
	 * uniquely identified by Zobrist Hash function
	 */
	public Hashtable<Long, Integer[]> EvaluatedStates;

    //Zobrist table
	public long[][] ZT;

    // Initialize the Zobrist table with random values
    public ZobristTableCounters(int M) {

        EvaluatedStates = new Hashtable<>();

        /*
        * M is the number of rows
        * N is the number of cols
        * 2 is the number of possible pieces
        * 2 is the number of possible turns
        * */
        ZT = new long[M][3];

        //Fill with random position
        for (int i = 0; i < M; ++i) {
            for (int j = 0; j < 3; ++j) {
                ZT[i][j] = new Random().nextLong();
            }
        }
    }

    public long computeHash(WinCounter counter, MNKBoard b) {

        long hash = 0;

        // Hash based on current table situation
        List<MNKCell> CellsToCheck = counter.CellsToCheck;
        int index = 0;
        for (MNKCell current : CellsToCheck) {
            hash ^= ZT[index++][stateId(b.B[current.i][current.j])];// Bitwise XOR
        }

        return hash;
    }

    public long diffHash(long previousHash, MNKCellState newCellState, int index){
        return previousHash ^= ZT[index][stateId(newCellState)];
    }


    // If the cell is occupied by P1 return 0, otherwhise (P2) return 1
    private int stateId(MNKCellState state) {
        if (state == MNKCellState.P1)
            return 1;
        else if (state == MNKCellState.P2)
            return 2;
        else
            return 0;
    }
}
