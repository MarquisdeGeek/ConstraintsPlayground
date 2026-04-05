package app;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.variables.IntVar;


public class Main {

    
    public static void main(String[] args) {
        Main m = new Main();

        // When I was 22, my brother was half my age. Now I’m 44. How old is he?
        m.classic(22, 2, 44);

        // From https://www.arvindguptatoys.com/arvindgupta/challengingpuzzles.pdf
        // Page 13
        // ‘My husband’s age,’ remarked a lady the other day, ‘is represented by 
        // the figures of my own age reversed. He is my senior, and the difference
        // between our ages is one-eleventh of their sum.’
        m.positional(11);

        // Ibid. Page 13
        m.roversAge();
    }



    void classic(int when, int fraction, int iam) {
        
        Model model = new Model("age puzzle");

        // Create the variables - we don't know the values, yet
        // (since we can't have unbounded IntVars, give us some headroom!)
        IntVar me = model.intVar("me", 1, 199, false);
        IntVar brother = model.intVar("brother", 1, 199, false);


        // Rules of the puzzle
        int whenIWas = when; // e.g. 22
        int myBrotherWas = fraction; // e.g. 2, fraction of age, meaning brother was half as old
        int iAmNow = iam; // e.g. 44, current age


        // Relationships known from the puzzle
        model.arithm(me, "=", whenIWas).post(); // my age
        model.arithm(brother, "=", me.div(myBrotherWas).intVar()).post(); // my brother was 1/2 my age


        // Resolve and output
        Solution solution = model.getSolver().findSolution();
        if (solution != null && solution.exists()) {
            int thisWasYearsAgo = iAmNow - whenIWas;

            int myAge = thisWasYearsAgo + me.getValue();

            if (myAge == iAmNow) {
                int hisAge = thisWasYearsAgo + brother.getValue();
                System.out.println("He is " + hisAge + " years old.");
            } else {
                System.out.println("Sorry - we goofed!");
            }
        } else {
            System.out.println("Sorry - someone goofed, puzzle couldn't be solved!");
        }

    }



    void positional(int fraction) {
        
        Model model = new Model("age puzzle (2)");

        // Create the variables - we don't know the values, yet
        // (since we can't have unbounded IntVars, give us some headroom!)
        IntVar me = model.intVar("me", 1, 199, false);
        IntVar husband = model.intVar("husband", 1, 199, false);


        // Relationships known from the puzzle
        // age 1 (e.g. AB) == age 2 (as BA)
        model.arithm(me, "=", husband.mod(10).mul(10).add(husband.div(10)).intVar()).post();

        // He is my senior...
        model.arithm(husband, ">", me).post();

        // ...and the difference between our ages ...
        IntVar difference = model.intVar("diff", -100, 100);
        model.arithm(husband, "-", me, "=", difference).post();

        // ...is one-eleventh of their sum.’
        model.arithm(difference, "=", me.add(husband).div(fraction).intVar()).post();


        // Resolve and output
        Solution solution = model.getSolver().findSolution();
        if (solution != null && solution.exists()) {
            System.out.println("I am " + me.getValue() + " and my husband is " + husband.getValue());
        } else {
            System.out.println("Sorry - someone goofed, puzzle couldn't be solved!");
        }
    }



    void roversAge() {
        
        Model model = new Model("age puzzle (3)");

        // ‘Well, five years ago,’ was the youngster’s reply, ‘sister was four times older than the dog, but now she is only three times as old.’ Can you tell Rover’s age

        // Create the variables - we don't know the values, yet
        // (since we can't have unbounded IntVars, give us some headroom!)
        int yearsAgo = 5;
        int fractionThen = 4;
        int fractionNow = 3;

        IntVar sisterNow = model.intVar("sisterNow", 1, 100, false);
        IntVar roverNow = model.intVar("roverNow", 1, 100, false);

        IntVar sisterThen = model.intVar("sisterThen", 1, 100, false);
        model.arithm(sisterThen, "=", sisterNow.sub(yearsAgo).intVar()).post();
        
        IntVar roverThen = model.intVar("roverThen", 1, 100, false);
        model.arithm(roverThen, "=", roverNow.sub(yearsAgo).intVar()).post();


        // Well, five years ago, sister was four times older than the dog,
        // (four times older = 5 times as old)
        model.arithm(sisterThen, "=", roverThen.mul(fractionThen + 1).intVar()).post();

        // but now she is only three times as old.
        model.arithm(sisterNow, "=", roverNow.mul(fractionNow).intVar()).post();


        // Resolve and output
        Solution solution = model.getSolver().findSolution();
        if (solution != null && solution.exists()) {
            System.out.println("Rover is now " + roverNow.getValue());
        } else {
            System.out.println("Sorry - someone goofed, puzzle couldn't be solved!");
        }
    }

}
