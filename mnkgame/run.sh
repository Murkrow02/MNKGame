./compile.sh
cd out/mnkgame
#java -cp ".." mnkgame.MNKPlayerTester $1 $2 $3 mnkgame.IterativeDeep mnkgame.IterativeDeep
java -cp ".." mnkgame.MNKGame $1 $2 $3 mnkgame.IterativeDeepZob
#MiniMaxAlphaBeta