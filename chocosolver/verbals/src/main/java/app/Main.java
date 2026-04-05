package app;


public class Main {

    
    public static void main(String[] args) {

        VerbalArithmetic va = new VerbalArithmetic(new String[]{"SEND","MORE"}, VerbalArithmetic.Op.ADD, "MONEY");
        VerbalArithmetic.VerbalResult result = va.solveAll();

        System.out.println("Problem: " + result.problemName);

        int solutionCount = 0;

        for(VerbalArithmetic.VSolution s: result.solutions) {
            ++solutionCount;
            String solutionString = "Solution: " + solutionCount;

            System.out.println(solutionString);
            System.out.println("-".repeat(solutionString.length()));
            
            va.renderSolution(s);
            System.out.println("");
        }

        if(solutionCount == 0){
            System.out.println("We got nada!");
        }

    }
}
