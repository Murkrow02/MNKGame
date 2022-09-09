./compile.sh
cd out/mnkgame
#java -cp ".." mnkgame.MNKPlayerTester $1 $2 $3 mnkgame.ZobristPlayer mnkgame.ZobristPlayer
java -cp ".." mnkgame.MNKGame $1 $2 $3 mnkgame.MiniMaxAlphaBeta
#MiniMaxAlphaBeta