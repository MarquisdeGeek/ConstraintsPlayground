package app;

import java.util.Comparator;
import java.util.stream.IntStream;

public class Main {

    
    public static void main(String[] args) {
        CoinChange cc = new CoinChange();

        // Try basic example
        cc.solve(CoinChange.UK, 87);


        // Most UK coins needed for an amount of change under 100?
        record Result(int amount, int coins) {}

        Result best =
            IntStream.range(1, 100)
                .mapToObj(i -> new Result(i, cc.solve(CoinChange.UK, i, true)))
                .max(Comparator.comparingInt(Result::coins))
                .orElseThrow();

        System.out.println(String.format("Getting %d%s in change requires %d coins", best.amount(), CoinChange.UK.unit, best.coins()));

    }

}
