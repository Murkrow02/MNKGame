package mnkgame;

import mnkgame.MNKBoard;
import mnkgame.MNKCellState;

/*
 * Custom class that extends canonical MNKBoard, used to arbitrarily
 * mark cell as P1 or P2, without depending on whom turn it is
 */
public class CustomMNKBoard extends MNKBoard {

    public CustomMNKBoard(int M, int N, int K) throws IllegalArgumentException {
        super(M, N, K);
    }

    public void customMarkCell(int i, int j, MNKCellState state) throws IndexOutOfBoundsException, IllegalStateException {
		  B[i][j] = state;
    }
    
}
