import os
import sys
import numpy as np

#Get command line arguments
player1 = sys.argv[1]
player2 = sys.argv[2]
nrounds = sys.argv[3]
file_path = os.path.dirname(__file__)

#Compile
os.chdir(file_path)
os.system('./compile.sh')

#Array of possible combinations
combs = ("3 3 3", "4 3 3", "4 4 3", "4 4 4", 
"5 4 4", "5 5 4", "6 4 4", "6 5 4", "6 6 4", 
"6 6 5", "6 6 6", "7 4 4", "7 5 4", "7 6 4", 
"7 7 4", "7 5 5", "7 6 5", "7 7 5", "7 7 6", 
"7 7 7", "8 8 4", "10 10 5", "50 50 10","70 70 10")

#Log txt file path
log_filepath = f"{file_path}/Logs/{player1}VS{player2}.txt"
os.system(f"rm -f {log_filepath}") #Remove old one

#Enter out dir
os.chdir(f"{file_path}/out/mnkgame")

for comb in combs:

    #Show output in console to inform beginning of game
    print(comb) 

    #Save game configuration id in log file
    os.system(f"echo $'\n{comb}' >> {log_filepath}")

    #Format command
    command = f'java -cp ".." mnkgame.MNKPlayerTester {comb} {player1} {player2} -r {nrounds} >> {log_filepath}.dirty' # P1 first
    command2 = f'java -cp ".." mnkgame.MNKPlayerTester {comb} {player2} {player1} -r {nrounds} >> {log_filepath}.dirty' # P2 first

    #Execute commands
    os.system(f"echo $'\n'{player1} first $'\n' >> {log_filepath}") #Title
    os.system(command) #Output
    os.system(f"sed -n -e '/Score/{{p;n;}}' {log_filepath}.dirty >> {log_filepath}") #Clean output

    #Remove dirty output
    os.system(f"rm {log_filepath}.dirty")


    #Execute commands
    os.system(f"echo $'\n'{player2} first $'\n' >> {log_filepath}") #Title
    os.system(command2) #Output
    os.system(f"sed -n -e '/Score/{{p;n;}}' {log_filepath}.dirty >> {log_filepath}") #Clean output

    #Remove dirty output
    os.system(f"rm {log_filepath}.dirty")

