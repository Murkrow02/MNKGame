package mnkgame;

public class Utility {
    
    public static int Factorial(int n) {

        int result = 1;

        try {
            for (int i = 2; i <= n; i++)
                result = result * i;

        } catch (Exception ex) {
            return result; // maximum value reached
        }

        return result;
    }
}
