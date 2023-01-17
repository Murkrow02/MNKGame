package mnkgame;

import java.util.Hashtable;

/*
 * TODO: Insert all possible win/loss already in hash table
 *  Negamax negascout
 *  Sometime AI is dumb and does not prevent the opponent to place more symbols near each other
 * 	Use try catch to immediately stop minimax when timeout found
 */

public class TestZob implements MNKPlayer {

    private MNKBoard B;
    private mnkgame.Utility utility;

    /* This tri-dimensional array stores M*N random values, is used
       by the game board hashing function to create a unique hash of
       each possible game state
    */
    public mnkgame.ZobristTable ZT;

    public mnkgame.WinCounters Counters;

    /*
     * Count how many times the iteration deepening reached a leaf state
     * used to stop when we reached all leaf states
     * */
    Integer ReachedLeafsCount = 0;

    /**
     * Default empty constructor
     */
    public TestZob() {
    }

    public void initPlayer(int M, int N, int K, boolean first, int timeout_in_secs) {

        utility = new mnkgame.Utility();

        //LOCAL BOARD
        B = new MNKBoard(M, N, K);

        //Possible gamestates
        utility.myWin = first ? MNKGameState.WINP1 : MNKGameState.WINP2;
        utility.yourWin = first ? MNKGameState.WINP2 : MNKGameState.WINP1;

        //Save if we are the first player
        utility.meFirst = first;

        //If the function takes longer than the set timeout an exception is thrown
        utility.TIMEOUT = timeout_in_secs;

        //How the cells are marked in this match
        utility.myMark = utility.meFirst ? MNKCellState.P1 : MNKCellState.P2;
        utility.yourMark = utility.meFirst ? MNKCellState.P2 : MNKCellState.P1;

        //Initialize Zobrist table
        ZT = new mnkgame.ZobristTable(M, N);

        //Initialize hash table
        int MaxTableSize = 1 << 20; // 2^20
        int TableSize = 1 << M * N; // 2^M*N, this size is related to board size

        //Creating a huge hashtable could crash the application
        if (TableSize > MaxTableSize)
            TableSize = MaxTableSize;

        //Init hashtable with desired size
        ZT.EvaluatedStates = new Hashtable<Long, Integer>(TableSize);

        //Initialize wincounters and respective cell references
        Counters = new mnkgame.WinCounters(B);

        //First scan of the whole board, foreach WinCounter check how many wins player can reach
        Counters.UpdateAllCounters(B);
    }

    public MNKCell selectCell(MNKCell[] FC, MNKCell[] MC) {

        //Start timer
        utility.timerStart = System.currentTimeMillis();

        //Update local board with last move from opponent
        if (MC.length > 0) {
            MNKCell c = MC[MC.length - 1]; // Recover move from opponent
            B.markCell(c.i, c.j);         // Save the last move in the local MNKBoard
            utility.updateWinCounters(B, Counters, c); //Update wincounters with opponent move
        }

        // If there is just one possible move, return immediately (match is over)
        if (FC.length == 1) {
            B.markCell(FC[0].i, FC[0].j);
            return FC[0];
        }

        //Use the first available cell if something goes wrong
        MNKCell BestMove = FC[0];

        /*CANNOT IMMEDIATELY WIN, PROCEED WITH MINIMAX*/

        //Iterative deep, increase depth each time until timeout
        int PossibleMovesCount = FC.length;
        for (int i = 0; !utility.isTimeExpiring(); ++i) {
            /*
             * Assume that the most reliable result is the one obtained from the last iteration
             * so every time an iteration completes, the best result found in that iteration is
             * set as the best result to this point. If somehow the iteration breaks (for example
             * timeout is triggered) the best move for iteration will be discarded and the previous
             * iteration result will be taken as best (if an iteration is stopped we cannot say that
             * the best result found is indeed the best of all the others)
             * */
            Integer IterationMax = Integer.MIN_VALUE;
            MNKCell BestIterationMove = FC[0];
            int CellExaminedCount = 0;
            boolean FinishedIteration = true;

            //
            boolean ExploredWholeTree = true;

            //Reset count of leaf states reached
            //ReachedLeafsCount = 0;

            for (MNKCell d : FC) {

                //Mark this cell as player move (temp)
                B.markCell(d.i, d.j);
                utility.updateWinCounters(B, Counters, d);

                //Apply minimax algorithm on the cell
                mnkgame.MiniMaxState MoveVal = miniMax(false, Integer.MIN_VALUE, Integer.MAX_VALUE, null, i, d);
                CellExaminedCount++;

                //
                if (!MoveVal.SqueezedChildren) {
                    ExploredWholeTree = false;
                }

                //Rollback
                utility.updateWinCounters(B, Counters, d);
                B.unmarkCell();

                //Check if found a better move
                if (MoveVal.BoardValue > IterationMax) {
                    IterationMax = MoveVal.BoardValue;
                    //BestMove = d;
                    BestIterationMove = d;
                }

                //Check if time is expiring and we didn't reach last cell to examine -> exit loop and discard this result
                if (utility.isTimeExpiring() && CellExaminedCount < PossibleMovesCount) {
                    FinishedIteration = false;
                    break;
                } else
                    utility.TRIGGER_TIMEOUT_PERCENTAGE = utility.DEFAULT_TRIGGER_TIMEOUT_PERCENTAGE; //Reset default timeout trigger
            }

            //Only set this iteration best move as global best move if iteration has not stopped
            if (FinishedIteration)
                BestMove = BestIterationMove;

            //Stop immediately iteration deepening if we explored the whole game tree
            if (ExploredWholeTree)
                break;
        }

        //Return the result
        B.markCell(BestMove.i, BestMove.j); //Update local board with this move
        utility.updateWinCounters(B, Counters, BestMove);

        //Update game UI
        return BestMove;
    }

    public mnkgame.MiniMaxState miniMax(boolean maximizingPlayer, int alpha, int beta, Long nodeHash, int depth, MNKCell nodeMove) {


        //Base case, evaluation detected gameover or timeout soon
        if (B.gameState() != MNKGameState.OPEN || utility.isTimeExpiring() || depth <= 0) {
            //In return object, save evaluation value and true if reached leaf, false otherwise
            return new mnkgame.MiniMaxState(utility.evaluateBoard2(B, Counters, depth), B.gameState() != MNKGameState.OPEN);
        }

        //Our turn (Maximizing)
        if (maximizingPlayer) {

            //Relax technique, assume the worst case scenario and relax on each step
            int MaxValue = Integer.MIN_VALUE;

            //True: we visited all children of this node (recursively), so we
            //got only leaf states from this
            //initially true, becomes false when a node returns false to the isLeaf property
            //
            //NOTE: the root node is considered as squeezed if an alpha-beta cut or a non-heuristic evaluation is made
            boolean SqueezedNode = true;

            //Cycle through all possible moves
            MNKCell[] FC = B.getFreeCells();
            for (MNKCell current : FC) {

                //Mark this cell as player move
                B.markCell(current.i, current.j);

                //Check if already evaluated this game state
                long boardHash = nodeHash != null ? ZT.diffHash(nodeHash, current, utility.myMark) : ZT.computeHash(B);
                Integer boardValue = ZT.EvaluatedStates.getOrDefault(boardHash, null);

				//If state is not in transposition table, continue searching deeper
                if (boardValue == null) {

                    //Update wincounters affected by this move
                    utility.updateWinCounters(B, Counters, current);

                    //Recursively call minmax on this board scenario
                    int d = depth - 1;
                    mnkgame.MiniMaxState eval = miniMax(false, alpha, beta, boardHash, d, current);
                    boardValue = eval.BoardValue;

                    //Check if we squeezed all children of current node
                    if (!eval.SqueezedChildren) {

                        //We did not explore the whole subtree under the root node
                        //having FC[] as children since the subtree with "current" as root
                        //is not squeezed
                        SqueezedNode = false;
                    }

                    //Restore wincounters before this move
                    utility.updateWinCounters(B, Counters, current);
                }

                //Undo the move
                B.unmarkCell();

                //Check if found better value
                if (boardValue > MaxValue)
                    MaxValue = boardValue;

                //Prune if better result was available before, no need to continue searching
                alpha = Math.max(alpha, MaxValue);
                if (alpha >= beta) {

                    //Consider an alpha-beta cut as a leaf state only if the cut
                    //was not on a heuristic node, but on an end-game node
                    //boolean squeezedByAlphaBeta = Math.abs(MaxValue) > (Integer.MAX_VALUE-50);
                    return new mnkgame.MiniMaxState(MaxValue, true);
                }
            }

			//Save current node evaluation in transposition table if squeezed (visited all children nodes)
			if(SqueezedNode)
				SaveState(nodeHash, B, MaxValue);

            //Return the best value obtained
            return new mnkgame.MiniMaxState(MaxValue, SqueezedNode);
        }
        //Opponent turn (minimizing)
        else {

            //Relax technique, assume the worst case scenario and relax on each step
            int MinValue = Integer.MAX_VALUE;


            boolean SqueezedNode = true;

            //Cycle through all possible moves
            MNKCell[] FC = B.getFreeCells();

            for (MNKCell current : FC) {

                //Mark this cell as player move
                B.markCell(current.i, current.j);

				//Check if already evaluated this game state
				long boardHash = nodeHash != null ? ZT.diffHash(nodeHash, current, utility.yourMark) : ZT.computeHash(B);
				Integer boardValue = ZT.EvaluatedStates.getOrDefault(boardHash, null);

				if(boardValue == null) {

					//Update wincounters affected by this move
					utility.updateWinCounters(B, Counters, current);

					//Recursively call minmax on this board scenario
					int d = depth - 1;
					mnkgame.MiniMaxState eval = miniMax(true, alpha, beta, boardHash, d, current);
					boardValue = eval.BoardValue;

					//Check if we squeezed all children of current node
					if (!eval.SqueezedChildren) {

						//We did not explore the whole subtree under the root node
						//having FC[] as children since the subtree with "current" as root
						//is not squeezed
						SqueezedNode = false;
					}

					//Restore wincounters before this move
					utility.updateWinCounters(B, Counters, current);
				}

                //Undo the move
                B.unmarkCell();

                //Check if found better value
                if (boardValue < MinValue)
                    MinValue = boardValue;

                //Prune if better result was available before, no need to continue searching
                beta = Math.min(beta, MinValue);
                if (beta <= alpha) {

                    //Consider an alpha-beta cut as a leaf state only if the cut
                    //was not on a heuristic node, but on an end-game node
                    //boolean squeezedByAlphaBeta = Math.abs(MinValue) > (Integer.MAX_VALUE-50);
                    return new mnkgame.MiniMaxState(MinValue, true);
                }
            }

			//Save current node evaluation in transposition table if squeezed (visited all children nodes)
			if(SqueezedNode)
				SaveState(nodeHash, B, MinValue);

            return new mnkgame.MiniMaxState(MinValue, SqueezedNode);
        }


    }

	public void SaveState(Long hash, MNKBoard b, int evaluation){

        //Save for future use only if state is certain (not middle evaluations)
		if(hash == null || Math.abs(evaluation) < (Integer.MAX_VALUE-50)) 
			return;

		//Add current value to HashSet for future use
		ZT.EvaluatedStates.put(hash, evaluation);

		//Add symmetric board states to HashSet as they have the same static evaluation
		ZT.addSimmetryHashes(b, evaluation);
	}

    public String playerName() {
        return "Mettaton";
    }
}


