package mnkgame;

import java.math.BigInteger;

public class Utility {
    public static BigInteger bigFactorial(int n) {

        BigInteger result = BigInteger.ONE;

        try {
            for (int i = 2; i <= n; i++)
                result = result.multiply(BigInteger.valueOf(i));

        } catch (Exception ex) {
            return result; // maximum value reached
        }

        return result;
    }
}
