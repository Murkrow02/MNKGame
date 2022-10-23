./compile.sh
cd out/mnkgame
clear && printf '\e[3J'
#java -cp ".." mnkgame.MNKPlayerTester $1 $2 $3 mnkgame.IterativeDeep mnkgame.IterativeDeep
java -cp ".." mnkgame.MNKGame $1 $2 $3 mnkgame.TestZob mnkgame.TestZob
#MiniMaxAlphaBeta