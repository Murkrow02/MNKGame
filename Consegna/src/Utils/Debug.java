package mnkgame;

import mnkgame.*;

public class Debug {

    //Enable-disable debug logs
    public static boolean active = false;
    public static long TotalEvaluations = 0;
    public static long Cuts = 0;
    public static long TableRead = 0;
    public static long MaxDepthReached = 0;
    public static boolean SolvedGame = true;
    public static int AlgorithmStarts = 0;

    public static void PrintGameState(MNKBoard b){

        if(!active)
            return;

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

    public static void Reset(){

        if(!active)
            return;

        System.out.println("\n#######################################\n");
        TotalEvaluations = 0;
        MaxDepthReached = 0;
        Cuts = 0;
        TableRead = 0;
        if(!SolvedGame)
            AlgorithmStarts++;
        SolvedGame = true;
    }

    public static void FoundEnd(boolean win)
    {

        if(!active)
            return;

        if(win)
            System.out.println("Found immediate win");
        else
            System.out.println("Found immediate loss");
    }

    public static void PrintSummary(){
        if(!active)
            return;

        System.out.println("\n---------RESULT---------");
        System.out.println("Evaluations: " + TotalEvaluations);
        System.out.println("Cuts: " + Cuts);
        System.out.println("TableRead: " + TableRead);
        System.out.println("Max depth reached: " + MaxDepthReached);



//        if(SolvedGame){
//            System.out.println("Game solved in: " + AlgorithmStarts);
//        }
    }

    public static void PrintCounters(mnkgame.WinCounters counters){

        if(!active)
            return;
        System.out.println("\n---------WINCOUNTERS---------");
        for (mnkgame.WinCounter counter : counters.Counters){
            System.out.println(counter.Name + " P1: " + counter.P1Score + " P2: " + counter.P2Score);
//            for(MNKCell controlled : counter.CellsToCheck){
//                System.out.println(controlled.toString());
//            }
        }

    }

    public static void IncreaseEvaluations(){

        if(!active)
            return;

        TotalEvaluations++;
    }

    public static void NewDepthReached(){
        MaxDepthReached++;
    }

    public static void PrintMiddleCicle(MNKBoard b, MNKCell cell, Integer val){

        if(!active)
            return;

        mnkgame.Debug.IncreaseEvaluations();

        System.out.println(cell.i + "," + cell.j + ": " + val);
    }

    public static void PrintDiagonalsCount(int count){

        if(!active)
            return;

        System.out.println("Totale diagonali: " + count);
    }

    public static void PrintCountersCount(int count){

        if(!active)
            return;

        System.out.println("Totale wincounters: " + count);
    }

    public static void AlreadyEvaluated(){

        if(!active)
            return;

        TableRead++;
    }
}
