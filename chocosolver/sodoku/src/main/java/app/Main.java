package app;

public class Main {

    
    public static void main(String[] args) {

        // Basic solver
        Sodoku.SPuzzle puzzle = new Sodoku.SPuzzle(new int[][]{
            {8, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 3, 6, 0, 0, 0, 0, 0},
            {0, 7, 0, 0, 9, 0, 2, 0, 0},

            {0, 5, 0, 0, 0, 7, 0, 0, 0},
            {0, 0, 0, 0, 4, 5, 7, 0, 0},
            {0, 0, 0, 1, 0, 0, 0, 3, 0},

            {0, 0, 1, 0, 0, 0, 0, 6, 8},
            {0, 0, 8, 5, 0, 0, 0, 1, 0},
            {0, 9, 0, 0, 0, 0, 4, 0, 0},
            });
        puzzle.render();

        Sodoku s = new Sodoku(puzzle);
        Sodoku.SSolution solution = s.solveFirst();

        System.out.println("Solution:");
        s.renderSolution(solution);
        s.renderSolutionAsArray(solution, "puzzle");


        // One with diagonals
        int[][] xpuzzle = {
            {6, 0, 3, 2, 1, 9, 8, 4, 5, },
            {5, 8, 9, 4, 3, 7, 2, 1, 6, },
            {4, 2, 1, 6, 8, 5, 7, 9, 3, },
            {0, 4, 7, 9, 6, 8, 3, 5, 1, },
            {0, 9, 6, 5, 2, 1, 4, 7, 8, },
            {0, 1, 5, 0, 7, 4, 6, 2, 9, },
            {7, 3, 4, 1, 9, 6, 5, 8, 2, },
            {1, 6, 8, 7, 5, 2, 9, 3, 4, },
            {9, 5, 2, 8, 4, 3, 1, 6, 7, },
        };
        Sodoku xs = new Sodoku(xpuzzle, true);
        Sodoku.SSolution xsolution = xs.solveFirst();

        System.out.println("Sodoku-X solution:");
        xs.renderSolution(xsolution);


        // Apparently, the most difficult Sodoku!?!?
        Sodoku difficultProblem = new Sodoku(new int[][]{
            {0, 0, 0, 0, 0, 0, 0, 1, 0, },
            {6, 0, 0, 0, 0, 0, 0, 0, 0, },
            {0, 8, 0, 0, 0, 0, 0, 0, 0, },
            {0, 0, 0, 0, 5, 0, 6, 0, 3, },
            {0, 0, 2, 0, 0, 0, 7, 0, 0, },
            {0, 0, 1, 0, 0, 0, 0, 0, 0, },
            {7, 0, 0, 6, 0, 0, 8, 0, 0, },
            {0, 5, 0, 1, 0, 0, 0, 0, 0, },
            {0, 0, 0, 2, 0, 4, 0, 0, 0, }, 
        });
        System.out.println("Difficult Sodoku solution:");
        difficultProblem.solveFirst().render();


        // Generate a basic puzzle, but providing no data
        Sodoku newPuzzle = new Sodoku(true);
        Sodoku.SSolution newSolution = newPuzzle.solveFirst();

        System.out.println("Solution to a new puzzle:");
        newPuzzle.renderSolution(newSolution);

        // Now, removing a number of entries in such a way that it's solveable is...
        // ... an exercise left for the reader :)
    }

}
