package mnkgame;

import mnkgame.*;
import java.util.*;

public class WinCounters {
    
    //Save here total wins to prevent cicling each time on array to get exact scores
    public int TotalP1Wins = 0;
    public int TotalP2Wins = 0;

    public WinCounter[] Counters; 
    public ArrayList<ArrayList<LinkedList<Integer>>> WinCountersReferences;

    public WinCounters(){}

    public WinCounters(MNKBoard b)
    {

        //Calculate how many win counters we need
        int CountersCount = 0;

        //Count horizontal matrixes
        if(b.N >= b.K){
            CountersCount+=b.M;
        }

        //Count vertical matrixes
        if(b.M >= b.K){
            CountersCount+=b.N;
        }

        //Count diagonal matrixes
        int Min_M_N = Math.min(b.M, b.N);
        // if(Min_M_N >= b.K){

        //     if(b.M >= b.K)
        //         CountersCount+= 1+(2*(b.M-b.K));
            
        //     if(b.N >= b.K)
        //         CountersCount+= 1+(2*(b.N-b.K));
        // }

        //All set
        Counters = new WinCounter[CountersCount];
        WinCountersReferences = new ArrayList<>(b.M);

        //Initialize lists for board cell references (used to immediately access matrixes to edit when a new piece is added)
        for(int i = 0; i < b.N; i++){   
            
            WinCountersReferences.add(new ArrayList<LinkedList<Integer>>(b.N));

            for(int j = 0; j < b.M; j++){
                WinCountersReferences.get(i).add(new LinkedList<Integer>());
            }
        }

        //Used instad of calling .size() on array each time
        int CountersIndexPointer = 0;

        //Horizontal matrixes
        if(b.N >= b.K) //If can't win on horizontal there's no need to save possible wins
        {

            //Foreach row
            for(int i = 0; i < b.M; i++){

                //Add a new counter
                Counters[CountersIndexPointer] = new WinCounter();
                Counters[CountersIndexPointer].CellsToCheck = new LinkedList<MNKCell>();
                
				//Foreach column
				for(int j = 0; j < b.N; j++){

                    //Add reference from this cell to target WinCounter
                    WinCountersReferences.get(i).get(j).add(CountersIndexPointer);
                    Counters[CountersIndexPointer].CellsToCheck.add(new MNKCell(i, j));
                }

                CountersIndexPointer++;
            }
        }

        //Vertical matrixes
        if(b.M >= b.K) //If can't win on vertical there's no need to save possible wins
        {

            //Foreach column
            for(int j = 0; j < b.N; j++){

                //Add a new counter
                Counters[CountersIndexPointer] = new WinCounter();
                Counters[CountersIndexPointer].CellsToCheck = new LinkedList<MNKCell>();
                
				//Foreach row
				for(int i = 0; i < b.M; i++){

                    //Add reference from this cell to target WinCounter
                    WinCountersReferences.get(i).get(j).add(CountersIndexPointer);
                    Counters[CountersIndexPointer].CellsToCheck.add(new MNKCell(i, j));
                }

                CountersIndexPointer++;
            }
        }

        //Diagonal matrixes
        if(Min_M_N >= b.K) //If can't win on diagonal there's no need to save possible wins
        {
            int totalDiagonals = 0;
            /*
            Bruteforce every possible diagonal to find which are suitable for win and consequently
            worth to save in a counter (there could have been a more elegant way but since this function
            is only called once I'm lazy to find one)
            */ 

            //Top to down arrows

            //Every diagonal starting from the first column, targeting the last row of the board
            for(int i = b.M-1; i >= 0; i--){

                //Save i index as it will be modified in the while loop
                int old_i = i;

                //Start each time from the first column
                int j = 0;
                
                //Check if can place enough symbols to reach victory on this diagonal
                int spaces_count = 0;
                while(i < b.N && j < b.M){
                    i++;
                    j++;
                    spaces_count++;
                    System.out.println(i);
                    System.out.println(j);
                }

                //If we can place at least k symbols on this column then there is enough space to reach victory
                boolean enough_space = spaces_count >= b.K;

                if(enough_space)
                    totalDiagonals++;

                //Restore i that was modified in the while loop
                i = old_i;
            }

            System.out.println("Totale diagonali: " + totalDiagonals);
            
        }
    }

    public void UpdateAllCounters(MNKBoard B){  

        //Reset total win (we are going to start from scratch)
        TotalP1Wins = 0;
        TotalP2Wins = 0;

        for(var counter : Counters){

            //Keep track of wins on this counter            
            counter.updateCounterWins(B);

            //Update total wins adding those tracked by this counter
			TotalP1Wins+=counter.P1Wins;
			TotalP2Wins+=counter.P2Wins;
        }
    }

    public LinkedList<Integer> CountersAffectedByMove(MNKCell move){
        return WinCountersReferences.get(move.i).get(move.j);
    }

    // public void Reset(){

    //     //Reset all counters to 0
    //     for (WinCounter counter : Counters) {
    //         TotalP1Wins = 0;
    //         TotalP2Wins = 0;
    //         counter.P1Wins = 0;
    //         counter.P2Wins = 0;
    //         counter.Full = false;
    //     }
    // }

}
