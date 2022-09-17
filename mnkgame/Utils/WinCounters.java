package mnkgame;

import mnkgame.*;
import java.util.*;

public class WinCounters {
    
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

            
        }
    }

    public void Reset(){

        //Reset all counters to 0
        for (WinCounter counter : Counters) {
            counter.P1Wins = 0;
            counter.P2Wins = 0;
            counter.Full = false;
        }
    }

    public int Score(boolean weAreP1){

        int P1Score = 0;
        int P2Score = 0;
        for (WinCounter winCounter : Counters) {
            P1Score+=winCounter.P1Wins;
            P2Score+=winCounter.P2Wins;
        }

        if(weAreP1)
            return P1Score-P2Score;
        else
            return P2Score-P1Score;
    }

    public int ScoreP1(){

        int P1Score = 0;
        for (WinCounter winCounter : Counters) {
            P1Score+=winCounter.P1Wins;
        }

        return P1Score;
    }

    public int ScoreP2(){

        int P2Score = 0;
        for (WinCounter winCounter : Counters) {
            P2Score+=winCounter.P2Wins;
        }

        return P2Score;
    }

    public WinCounters Copy(){
        
        WinCounters newCopy = new WinCounters();
        newCopy.Counters = Arrays.copyOf(Counters, Counters.length);;
        //newCopy.WinCountersReferences = WinCountersReferences;
        return newCopy;
    }

}
