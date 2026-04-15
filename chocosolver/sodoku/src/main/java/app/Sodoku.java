package app;

import java.util.ArrayList;
import java.util.List;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.variables.IntVar;


public class Sodoku {

    // So much of the code (currently) relies on a 3x3 set of grids, so these
    // params can not be changed freely
    final static int GRID_WIDTH = 9;
    final static int GRID_HEIGHT = 9;

    IntVar[][] grid = new IntVar[GRID_WIDTH][GRID_HEIGHT];
    Model model;
    boolean checkDiagonals;

    // Note: These could be static inner classes
    public static class SPuzzle extends SGrid {
        SPuzzle(int[][] rows) {
            grid = rows;
        }
    }

    public static class SSolution extends SGrid {
    }

    public static class SGrid {
        public int[][] grid = new int[GRID_WIDTH][GRID_HEIGHT];

        public void render() {

            for(int col=0;col<GRID_WIDTH;++col) {
                System.out.print("+---");
            }
            System.out.println("+");

            for(int row=0;row<GRID_HEIGHT;++row) {
                //
                for(int col=0;col<GRID_WIDTH;++col) {
                    String pad = ((((col/3)*3) + ((row/3)*3)) & 1) == 0 ? " " : "|";
                    System.out.print("|" + pad + (grid[row][col] == 0 ? " " : grid[row][col]) + pad);
                }
                System.out.println("|");
                //
                for(int col=0;col<GRID_WIDTH;++col) {
                    System.out.print("+---");
                }
                System.out.println("+");
            }
        }


        public void renderAsArray(String varname) {
            System.out.println(String.format("int[][] %s = {", varname));

            for(int row=0;row<GRID_HEIGHT;++row) {
                String rowString = "{";

                for(int col=0;col<GRID_WIDTH;++col) {
                    rowString += grid[row][col] + ", ";
                }
                rowString += "},";
                System.out.println(rowString);
            }

            System.out.println(String.format("} // end of %s", varname));
        }

    }


    public Sodoku() {
        this(false);
    }


    public Sodoku(boolean diagonals) {
        this(new int[GRID_WIDTH][GRID_HEIGHT], diagonals);
    }


    public Sodoku(SPuzzle puzzle) {
        this(puzzle.grid, false);
    }

    public Sodoku(int[][] predefinedRows) {
        this(predefinedRows, false);
    }


    public Sodoku(int[][] predefinedRows, boolean diagonals) {

        model = new Model("soduku");
        checkDiagonals = diagonals;

        // Build a grid
        for(int row=0;row<GRID_HEIGHT;++row) {
            for(int col=0;col<GRID_WIDTH;++col) {
                
                if (predefinedRows[row][col] != 0) {
                    grid[row][col] = model.intVar(predefinedRows[row][col]);
                    // Alt: We could create the intVar as normal, and fix a constraint with:
                    // model.arithm(grid[row][col], "=", predefinedRows[row][col]).post();
                } else {
                    grid[row][col] = model.intVar(String.format("g_%d_%d", row, col), 1, 9, false);
                }
                
            }
        }


        // Are the areas unique?
        for(int row=0;row<GRID_HEIGHT;++row) {
            // In each row?
            model.allDifferent(grid[row]).post();

            // In each column?
            IntVar[] thisColumn = new IntVar[GRID_HEIGHT];
            for(int col=0;col<GRID_WIDTH;++col) {
                thisColumn[col] = grid[col][row];
            }
            model.allDifferent(thisColumn).post();
        }

        // In each of the nine mini 3x3 grids?
        for(int area=0;area<9;++area) {
            int firstRow = (area / 3) * 3;
            int firstColumn = (area % 3) * 3;

            IntVar[] thisQuad = new IntVar[GRID_HEIGHT];
            for(int sq=0;sq<9;++sq) {
                int row = firstRow + sq/3;
                int col = firstColumn + sq%3;

                thisQuad[sq] = grid[row][col];
            }
            
            model.allDifferent(thisQuad).post();
        }

        // Two major diagonals (assumes square Soduko)
        if (checkDiagonals) {
            IntVar[] thisDiagonalMajor = new IntVar[GRID_HEIGHT];
            IntVar[] thisDiagonalMinor = new IntVar[GRID_HEIGHT];
            for(int i=0;i<9;++i) {
                thisDiagonalMajor[i] = grid[i][i];
                thisDiagonalMinor[i] = grid[i][8-i];
            }
            //
            model.allDifferent(thisDiagonalMajor).post();
            model.allDifferent(thisDiagonalMinor).post();
        }

    }


    public SSolution solveFirst() {
        Solution solution = model.getSolver().findSolution();
        return captureSolution(solution);
    }


    public SSolution[] solveAll() {
        List<SSolution> list = new ArrayList<>();

        while (model.getSolver().solve()) {
            Solution solution = new Solution(model);
            solution.record();
            //
            list.add(captureSolution(solution));
        }
        return list.toArray(new SSolution[0]);
    }


    protected SSolution captureSolution(Solution solution) {
        if (solution == null) {
            return null;
        }
        //
        SSolution vs = new SSolution();

        for(int row=0;row<GRID_HEIGHT;++row) {
            for(int col=0;col<GRID_WIDTH;++col) {
                vs.grid[row][col] = grid[row][col].getValue();
            }
        }

        return vs;
    }


    public void renderSolution(SSolution solution) {
        if (solution == null) {
            System.out.println("No solution exists.");
        } else {
            solution.render();
        }
    }


    public void renderSolutionAsArray(SSolution solution, String varname) {
        if (solution == null) {
            System.out.println(String.format("int[][] %s = {", varname));
            System.out.println(String.format("} // end of %s", varname));
        } else {
            solution.renderAsArray(varname);
        }
    }

}
