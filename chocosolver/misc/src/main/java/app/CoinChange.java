package app;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;

public class CoinChange {
    public static class Currency {
        public String unit;
        public int[]  coinage;

        public Currency(String unit, int[] coinage) {
            this.unit = unit;
            this.coinage = coinage;
        }
    }

    public static Currency UK = new Currency("p", new int[] {1, 2, 5, 10, 20, 50, 100, 200});
    public static Currency US = new Currency("¢", new int[] {1, 5, 10, 25, 50, 100});
    public static Currency AUS = new Currency("c", new int[] {5, 10, 20, 50, 100, 200});


    public int solve(Currency currency, int total) {
        return solve(currency, total, false);
    }

    public int solve(Currency currency, int total, Boolean quiet) {
        if (quiet == null) {
            quiet = false;
        }
        //
        Model model = new Model();

        // one variable per coin type
        IntVar[] coinCount = new IntVar[currency.coinage.length];
        for (int i = 0; i < currency.coinage.length; i++) {
            coinCount[i] = model.intVar("coin_" + currency.coinage[i], 0, total); // max total coins
        }

        // weighted sum of all coins = total
        model.scalar(coinCount, currency.coinage, "=", total).post();

        // total number of coins...
        IntVar numCoins = model.intVar("numCoins", 0, total);
        model.sum(coinCount, "=", numCoins).post();

        // ...should be minimized
        model.setObjective(Model.MINIMIZE, numCoins);


        int minCoins = Integer.MAX_VALUE;
        int solutionCount = 0;
        String solution = null;

        while (model.getSolver().solve()) {
            if (numCoins.getValue() < minCoins) {
                minCoins = numCoins.getValue();

                solution = IntStream.range(0, currency.coinage.length)
                    .filter(i -> coinCount[i].getValue() != 0)
                    .mapToObj(i -> String.format("%d of %s%s (=%d)", coinCount[i].getValue(), currency.coinage[i], currency.unit, currency.coinage[i] * coinCount[i].getValue()))
                    .collect(Collectors.joining(", "));
                //
                solution += " [results in ";
                solution += IntStream.range(0, currency.coinage.length)
                    .filter(i -> coinCount[i].getValue() != 0)
                    .mapToObj(i -> String.format("%d", currency.coinage[i] * coinCount[i].getValue()))
                    .collect(Collectors.joining(" + "));
                solution += String.format(" = %d]", total);
            }
            //
            ++solutionCount;
        }


        if (!quiet) {
            if (solution != null) {
                System.out.println(String.format("Making %d has %d solutions.", total, solutionCount));
                System.out.println(String.format("Best requires %d coins: %s", minCoins, solution));
                
            } else {
                System.out.println("No solution available to find " + total + " with the coins " + Arrays.toString(currency.coinage));
            }
        }

        
        return minCoins;
    }
}
