#Compile
./compile.sh
cd out/mnkgame

#Test all combinations
#echo -e "\n 3 3 3"
#java -cp ".." mnkgame.MNKPlayerTester 3 3 3 mnkgame.$1 mnkgame.$1
#echo -e "\n 4 3 3"
#java -cp ".." mnkgame.MNKPlayerTester 4 3 3 mnkgame.$1 mnkgame.$1
#echo -e "\n 4 4 3"
#java -cp ".." mnkgame.MNKPlayerTester 4 4 3 mnkgame.$1 mnkgame.$1
#echo -e "\n 4 4 4"
#java -cp ".." mnkgame.MNKPlayerTester 4 4 4 mnkgame.$1 mnkgame.$1
#echo -e "\n 5 4 4"
#java -cp ".." mnkgame.MNKPlayerTester 5 4 4 mnkgame.$1 mnkgame.$1
#echo -e "\n 5 5 4"
#java -cp ".." mnkgame.MNKPlayerTester 5 5 4 mnkgame.$1 mnkgame.$1
echo -e "\n 5 5 5"
java -cp ".." mnkgame.MNKPlayerTester 5 5 5 mnkgame.$1 mnkgame.$2 $4 -r $3
echo -e "\n 6 4 4"
java -cp ".." mnkgame.MNKPlayerTester 6 4 4 mnkgame.$1 mnkgame.$2 $4 -r $3
echo -e "\n 6 5 4"
java -cp ".." mnkgame.MNKPlayerTester 6 5 4 mnkgame.$1 mnkgame.$2 $4 -r $3
echo -e "\n 6 6 4"
java -cp ".." mnkgame.MNKPlayerTester 6 6 4 mnkgame.$1 mnkgame.$2 $4 -r $3
echo -e "\n 6 6 5"
java -cp ".." mnkgame.MNKPlayerTester 6 6 5 mnkgame.$1 mnkgame.$2 $4 -r $3
echo -e "\n 6 6 6"
java -cp ".." mnkgame.MNKPlayerTester 6 6 6 mnkgame.$1 mnkgame.$2 $4 -r $3
echo -e "\n 7 4 4"
java -cp ".." mnkgame.MNKPlayerTester 7 4 4 mnkgame.$1 mnkgame.$2 $4 -r $3
echo -e "\n 7 5 4"
java -cp ".." mnkgame.MNKPlayerTester 7 5 4 mnkgame.$1 mnkgame.$2 $4 -r $3
echo -e "\n 7 6 4"
java -cp ".." mnkgame.MNKPlayerTester 7 6 4 mnkgame.$1 mnkgame.$2 $4 -r $3
echo -e "\n 7 7 4"
java -cp ".." mnkgame.MNKPlayerTester 7 7 4 mnkgame.$1 mnkgame.$2 $4 -r $3
echo -e "\n 7 5 5"
java -cp ".." mnkgame.MNKPlayerTester 7 5 5 mnkgame.$1 mnkgame.$2 $4 -r $3
echo -e "\n 7 6 5"
java -cp ".." mnkgame.MNKPlayerTester 7 6 5 mnkgame.$1 mnkgame.$2 $4 -r $3
echo -e "\n 7 7 5"
java -cp ".." mnkgame.MNKPlayerTester 7 7 5 mnkgame.$1 mnkgame.$2 $4 -r $3
echo -e "\n 7 7 6"
java -cp ".." mnkgame.MNKPlayerTester 7 7 6 mnkgame.$1 mnkgame.$2 $4 -r $3
echo -e "\n 7 7 7"
java -cp ".." mnkgame.MNKPlayerTester 7 7 7 mnkgame.$1 mnkgame.$2 $4 -r $3
echo -e "\n 8 8 4"
java -cp ".." mnkgame.MNKPlayerTester 8 8 4 mnkgame.$1 mnkgame.$2 $4 -r $3
echo -e "\n 10 10 5"
java -cp ".." mnkgame.MNKPlayerTester 10 10 5 mnkgame.$1 mnkgame.$2 $4 -r $3
echo -e "\n 50 50 10"
java -cp ".." mnkgame.MNKPlayerTester 50 50 10 mnkgame.$1 mnkgame.$2 $4 -r $3
echo -e "\n 70 70 10"
java -cp ".." mnkgame.MNKPlayerTester 70 70 10 mnkgame.$1 mnkgame.$2 $4 -r $3
