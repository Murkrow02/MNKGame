package mnkgame.utils;
import mnkgame.*;
import java.util.*;

import static mnkgame.utils.Utility.CellIdentifier;

public class WinCounters {
    
    //Save here total wins to prevent cicling each time on array to get exact scores
    public int TotalP1Score = 0;
    public int TotalP2Score = 0;
    public WinCounter[] Counters;
    public ArrayList<ArrayList<LinkedList<Integer>>> WinCountersReferences;

    public ZobristTableCounters ZTCounters;

    public WinCounters(){}

    public WinCounters(MNKBoard b)
    {

        int Max_M_N = Math.max(b.M, b.N);
        ZTCounters = new ZobristTableCounters(Max_M_N);

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
        if(b.M >= b.K)
            CountersCount+= 1+(2*(b.M-b.K));
        if(b.N >= b.K)
            CountersCount+= 1+(2*(b.N-b.K));


        //All set
        Counters = new WinCounter[CountersCount];
        WinCountersReferences = new ArrayList<>(b.M);

        //Initialize lists for board cell references (used to immediately access matrixes to edit when a new piece is added)
        for(int i = 0; i < b.M; i++){   
            
            WinCountersReferences.add(new ArrayList<LinkedList<Integer>>(b.M));

            for(int j = 0; j < b.N; j++){
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
                Counters[CountersIndexPointer].Name = "Row " + i;
                Counters[CountersIndexPointer].CellsToCheck = new LinkedList<MNKCell>();
                
				//Foreach column
				for(int j = 0; j < b.N; j++){

                    //Add reference from this cell to target WinCounter
                    MNKCell ReferencedCell = new MNKCell(i, j);
                    WinCountersReferences.get(i).get(j).add(CountersIndexPointer);
                    Counters[CountersIndexPointer].CellsToCheck.add(ReferencedCell);

                    //Let the counter know at which index this cell is placed
                    Counters[CountersIndexPointer].CellsIndexes.put(CellIdentifier(ReferencedCell), j);
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
                Counters[CountersIndexPointer].Name = "Column " + j;
                Counters[CountersIndexPointer].CellsToCheck = new LinkedList<MNKCell>();
                
				//Foreach row
				for(int i = 0; i < b.M; i++){

                    //Add reference from this cell to target WinCounter
                    MNKCell ReferencedCell = new MNKCell(i, j);
                    WinCountersReferences.get(i).get(j).add(CountersIndexPointer);
                    Counters[CountersIndexPointer].CellsToCheck.add(ReferencedCell);

                    //Let the counter know at which index this cell is placed
                    Counters[CountersIndexPointer].CellsIndexes.put(CellIdentifier(ReferencedCell), i);
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

            //TOP TO DOWN ARROWS

            //Every diagonal starting from first column to last row (including middle one)
            for(int i = b.M-1; i >= 0; i--){

                int old_i = i;
                int j = 0;

                //Check if can place enough symbols to reach victory on this diagonal
                List<MNKCell> visited_cells = new LinkedList<>();
                while(i < b.M && j < b.N){
                    visited_cells.add(new MNKCell(i,j));
                    i++;
                    j++;
                }

                //If we can place at least k symbols on this column then there is enough space to reach victory
                boolean enough_space = visited_cells.size() >= b.K;

                if(enough_space){

                    //Add a new counter
                    Counters[CountersIndexPointer] = new WinCounter();
                    Counters[CountersIndexPointer].Name = "Diagonal from " + visited_cells.get(0).i + " " + visited_cells.get(0).j;
                    Counters[CountersIndexPointer].CellsToCheck = new LinkedList<MNKCell>();

                    int index = 0;
                    for(MNKCell c : visited_cells){

                        //Add reference from this cell to target WinCounter
                        MNKCell ReferencedCell = new MNKCell(c.i, c.j);
                        WinCountersReferences.get(c.i).get(c.j).add(CountersIndexPointer);
                        Counters[CountersIndexPointer].CellsToCheck.add(ReferencedCell);

                        //Let the counter know at which index this cell is placed
                        Counters[CountersIndexPointer].CellsIndexes.put(CellIdentifier(ReferencedCell), index++);
                    }

                    CountersIndexPointer++;
                    totalDiagonals++;
                }


                //Restore i that was modified in the while loop
                i = old_i;
            }

            //Every diagonal starting from first row to last column (excluding middle one)
            for(int j = b.M-1; j >= 1; j--){

                int old_j = j;
                int i = 0;

                //Check if can place enough symbols to reach victory on this diagonal
                List<MNKCell> visited_cells = new LinkedList<>();
                while(i < b.M && j < b.N){
                    visited_cells.add(new MNKCell(i,j));
                    i++;
                    j++;
                }

                //If we can place at least k symbols on this column then there is enough space to reach victory
                boolean enough_space = visited_cells.size() >= b.K;

                if(enough_space){

                    //Add a new counter
                    Counters[CountersIndexPointer] = new WinCounter();
                    Counters[CountersIndexPointer].Name = "Diagonal from " + visited_cells.get(0).i + " " + visited_cells.get(0).j;
                    Counters[CountersIndexPointer].CellsToCheck = new LinkedList<MNKCell>();

                    int index = 0;
                    for(MNKCell c : visited_cells){

                        //Add reference from this cell to target WinCounter
                        MNKCell ReferencedCell = new MNKCell(c.i, c.j);
                        WinCountersReferences.get(c.i).get(c.j).add(CountersIndexPointer);
                        Counters[CountersIndexPointer].CellsToCheck.add(ReferencedCell);

                        //Let the counter know at which index this cell is placed
                        Counters[CountersIndexPointer].CellsIndexes.put(CellIdentifier(ReferencedCell), index++);
                    }

                    CountersIndexPointer++;
                    totalDiagonals++;
                }


                //Restore i that was modified in the while loop
                j = old_j;
            }

            //FROM DOWN TO TOP

            //Every diagonal starting from last row to last column (including middle one)
            for(int j = b.M-1; j >= 0; j--){

                int old_j = j;
                int i = b.M-1;

                //Check if can place enough symbols to reach victory on this diagonal
                List<MNKCell> visited_cells = new LinkedList<>();
                while(i >= 0 && j < b.N){
                    visited_cells.add(new MNKCell(i,j));
                    i--;
                    j++;
                }

                //If we can place at least k symbols on this column then there is enough space to reach victory
                boolean enough_space = visited_cells.size() >= b.K;

                if(enough_space){

                    //Add a new counter
                    Counters[CountersIndexPointer] = new WinCounter();
                    Counters[CountersIndexPointer].Name = "Diagonal from " + visited_cells.get(0).i + " " + visited_cells.get(0).j;
                    Counters[CountersIndexPointer].CellsToCheck = new LinkedList<MNKCell>();

                    int index = 0;
                    for(MNKCell c : visited_cells){

                        //Add reference from this cell to target WinCounter
                        MNKCell ReferencedCell = new MNKCell(c.i, c.j);
                        WinCountersReferences.get(c.i).get(c.j).add(CountersIndexPointer);
                        Counters[CountersIndexPointer].CellsToCheck.add(ReferencedCell);

                        //Let the counter know at which index this cell is placed
                        Counters[CountersIndexPointer].CellsIndexes.put(CellIdentifier(ReferencedCell), index++);
                    }

                    CountersIndexPointer++;
                    totalDiagonals++;
                }


                //Restore i that was modified in the while loop
                j = old_j;
            }

            //Every diagonal starting from first column to first row (excluding middle one)
            for(int i = b.M-2; i >= 0; i--){

                int old_i = i;
                int j = 0;

                //Check if can place enough symbols to reach victory on this diagonal
                List<MNKCell> visited_cells = new LinkedList<>();
                while(i >= 0 && j < b.N){
                    visited_cells.add(new MNKCell(i,j));
                    i--;
                    j++;
                }

                //If we can place at least k symbols on this column then there is enough space to reach victory
                boolean enough_space = visited_cells.size() >= b.K;

                if(enough_space){

                    //Add a new counter
                    Counters[CountersIndexPointer] = new WinCounter();
                    Counters[CountersIndexPointer].Name = "Diagonal from " + visited_cells.get(0).i + " " + visited_cells.get(0).j;
                    Counters[CountersIndexPointer].CellsToCheck = new LinkedList<MNKCell>();

                    int index = 0;
                    for(MNKCell c : visited_cells){

                        //Add reference from this cell to target WinCounter
                        MNKCell ReferencedCell = new MNKCell(c.i, c.j);
                        WinCountersReferences.get(c.i).get(c.j).add(CountersIndexPointer);
                        Counters[CountersIndexPointer].CellsToCheck.add(ReferencedCell);

                        //Let the counter know at which index this cell is placed
                        Counters[CountersIndexPointer].CellsIndexes.put(CellIdentifier(ReferencedCell), index++);
                    }

                    CountersIndexPointer++;
                    totalDiagonals++;
                }


                //Restore i that was modified in the while loop
                i = old_i;
            }
        }
    }

    public void UpdateAllCounters(MNKBoard B){  

        //Reset total win (we are going to start from scratch)
        TotalP1Score = 0;
        TotalP2Score = 0;

        for(var counter : Counters){

            //Keep track of wins on this counter            
            counter.updateCounterScore(B, null, ZTCounters);

            //Update total wins adding those tracked by this counter
			TotalP1Score +=counter.P1Score;
			TotalP2Score +=counter.P2Score;
        }
    }

    public LinkedList<Integer> CountersAffectedByMove(MNKCell move){
        return WinCountersReferences.get(move.i).get(move.j);
    }


}
