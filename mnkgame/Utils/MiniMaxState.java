package mnkgame;

public class MiniMaxState {

    MiniMaxState(int ev, boolean leaf){
        BoardValue = ev;
        ReachedLeaf= leaf;
    }
    int BoardValue;
    boolean ReachedLeaf;
}
