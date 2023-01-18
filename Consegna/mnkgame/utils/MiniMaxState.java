package mnkgame.utils;

public class MiniMaxState {

    public MiniMaxState(int ev, boolean squeezed){
        BoardValue = ev;
        SqueezedChildren= squeezed;
    }
    public int BoardValue;
    public boolean SqueezedChildren;
}
