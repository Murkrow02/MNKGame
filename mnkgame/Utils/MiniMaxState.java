package mnkgame;

import java.util.Hashtable;
import java.util.Random;
import mnkgame.MNKBoard;
import mnkgame.Utility;
import mnkgame.CustomMNKBoard;
import mnkgame.MNKCell;
import mnkgame.MNKCellState;

public class MiniMaxState {

    boolean MaximizingPlayer;
    MNKBoard Board;
    int MaxValue;
    int MinValue;
}
