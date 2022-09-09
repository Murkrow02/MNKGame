package mnkgame;

import mnkgame.*;

public class Debug {

    public static int Evaluations = 0;

    public static void printGameState(MNKBoard b){
        System.out.print("\n");
		for (int i = 0; i < b.M; i++) { // Print gameboard
			for (int j = 0; j < b.N; j++) {
                String mark = "-";
                if(b.B[i][j] == MNKCellState.P1)
                    mark = "X";
                else if (b.B[i][j] == MNKCellState.P2)
                    mark = "O";
				System.out.print(mark + "\t");
			}

            System.out.print("\n");
		}
	}

    public static void Divider(){
        System.out.println("\n ####################################### \n");
    }

    public static void FoundEnd(boolean win){

        if(win)
            System.out.println("Found immediate win");
        else
            System.out.println("Found immediate loss");
    }

    public static void PrintMiddleCicle(MNKBoard b){
        Evaluations = 0;
    }
}
