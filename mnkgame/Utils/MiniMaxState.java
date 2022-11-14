package mnkgame;

public class MiniMaxState {

    MiniMaxState(int ev, boolean squeezed){
        BoardValue = ev;
        SqueezedChildren= squeezed;
    }
    int BoardValue;
    boolean SqueezedChildren;
}
