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

    //private static long opponentTurnHash = 81293; // Random number
    public long computeHash(MNKBoard B) {

        long hash = 0;

        // Hash differently based on whom turn it is (not needed)
        //if (opponentTurn)
        //    hash ^= opponentTurnHash;

        // Hash based on current table situation
        MNKCell MC[] = B.getMarkedCells();
        for (MNKCell current : MC) {
            hash ^= ZT[current.i][current.j][pieceId(current)];// Bitwise XOR
        }

        return hash;
    }

    // If the cell is occupied by P1 return 0, otherwhise (P2) return 1
    private int pieceId(MNKCell cell) {
        if (cell.state == MNKCellState.P1)
            return 0;
        else if (cell.state == MNKCellState.P2)
            return 1;
        else
            return -1;
    }


}
