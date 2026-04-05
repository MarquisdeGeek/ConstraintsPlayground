package app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.variables.IntVar;



public class VerbalArithmetic {
    public static enum Op {
        ADD,
        SUBTRACT,
    }
    protected Model       theModel;
    protected Question    theQuestion;


    public class VSolution {
        Map<String, Integer> results = new HashMap<>();
    }

    public class Question {

        String[] question;
        Op       op;
        String   answer;


        Question(String[] question, Op op, String answer) {
            this.question = question;
            this.op = op;
            this.answer = answer;
        }
    }

    public class VerbalResult {
        String problemName;
        List<VSolution> solutions;


        VerbalResult(Model model) {
            solutions = new ArrayList<>();
            problemName = model.getName();
        }
    }


    VerbalArithmetic(String[] questionIn, Op op, String answer) {
        theQuestion = new Question(questionIn, op, answer);
        theModel = createModel(theQuestion);
    }


    public VerbalResult solveFirst() {
        VerbalResult vr = new VerbalResult(theModel);
        Solution solution = theModel.getSolver().findSolution();

        vr.solutions.add(captureSolution(vr, solution));

        return vr;
    }


    public VerbalResult solveAll() {
        VerbalResult vr = new VerbalResult(theModel);

        while (theModel.getSolver().solve()) {
            Solution solution = new Solution(theModel);
            solution.record();
            //
            vr.solutions.add(captureSolution(vr, solution));
        }

        return vr;
    }


    protected VSolution captureSolution(VerbalResult vr, Solution solution) {
        VSolution vs = new VSolution();
        
        for (IntVar v : theModel.retrieveIntVars(true)) {
            vs.results.put(v.getName(), v.getValue());
        }

        return vs;
    }


    protected final Model createModel(Question q) {

        class VCharacter {
            Character letter;
            int lower;
            IntVar iv;

            VCharacter(Character ch, int position) {
                letter = ch;
                lower = position == 0 ? 1 : 0;
            }

            IntVar build(Model model) {
                iv = model.intVar(String.valueOf(letter), lower, 9, false);
                return iv;
            }
        }

        Set<VCharacter> uniqueCharacters = new HashSet<>();

        Consumer<String> addString = (String s) -> {
            final char[] asChars = s.toCharArray();
            
            for(int i=0;i<asChars.length;++i) {
                // TODO: Find a less smelly way, since Set<> does most of this for us
                // it's just the special case of changing lower bounds that forces this
                final char currentChar = asChars[i];
                VCharacter ch = uniqueCharacters
                    .stream()
                    .filter(c -> c.letter.equals(currentChar))
                    .findFirst()
                    .orElse(null);

                // We might need to change the lower bounds
                if (ch != null && i == 0) {
                    ch.lower = 1;
                } else if (ch == null) { // add newly found character
                    uniqueCharacters.add(new VCharacter(asChars[i], i));
                }
            }
        };

        Function<Character, IntVar> getIntVar = (Character c) -> {
            VCharacter letter = uniqueCharacters
            .stream()
            .filter(p -> p.letter.equals(c))
            .findFirst()
            .orElse(null); // not that this should happen during the solver
            
            return letter.iv;
        };

        // Get the set of letters used, and create a name for the model
        String modelName = "";
        String concat = "";
        for (String s : q.question) {
            addString.accept(s);
            //
            modelName += concat;
            modelName += s;
            concat = " " + opSymbol(q.op) + " ";
        }

        // ANd...
        addString.accept(q.answer);
        modelName += " = " + q.answer;


        // choco-stuff starts here...
        Model model = new Model(modelName);

        // Construct the IntVars
        List<IntVar> list = new ArrayList<>();

        for(VCharacter c : uniqueCharacters) {
            list.add(c.build(model));
        }
        IntVar[] all = list.toArray(new IntVar[0]);


        // Limit uniqueness
        model.allDifferent(all).post();


        // Construct the question
        List<IntVar> qlist = new ArrayList<>();
        for (String s : q.question) {
            for(Character c : s.toCharArray()) {
                qlist.add(getIntVar.apply(c));
            }
        }
        // And answer
        for(Character c : q.answer.toCharArray()) {
            qlist.add(getIntVar.apply(c));
        }

        // Then put it into choco format
        IntVar[] QUESTION = qlist.toArray(new IntVar[0]);


        // Compute the co-efficients
        List<Integer> clist = new ArrayList<>();
        int multiplier = 1;
        for (String s : q.question) {
            int placeValue = (int)Math.pow(10, s.length());

            for (int i = 0; i < s.length(); i++) {
                clist.add(multiplier * placeValue);
                placeValue /= 10;
            }
            // If we loop a second time, then subtract all placeValue's, iff it's a subtraction
            // (otherwise, we set a var for no reason)
            multiplier = q.op.equals(Op.SUBTRACT) ? -1 : 1;
        }

        // And answer
        int placeValue = (int)Math.pow(10, q.answer.length());
        for (int i = 0; i < q.answer.length(); i++) {
            clist.add(-placeValue);
            placeValue /= 10;
        }

         // Then put it into choco format
        int[] COEFFS = clist.stream()
            .mapToInt(Integer::intValue)
            .toArray();


        // The global checks
        model.scalar(QUESTION, COEFFS, "=", 0).post();

        return model;
    }


    public void renderSolution(VSolution solution) {

        Integer lhsPaddingSize = Stream.concat(Arrays.stream(theQuestion.question), Stream.of(theQuestion.answer))
                                .map(String::length)
                                .max(Integer::compare)
                                .orElse(0);

        String leftPad = "  ";
        String becomes = "  =>  ";
        String padFormat = "%" + lhsPaddingSize + "s";

        for (int i = 0; i < theQuestion.question.length; i++) {
            String row = "";
            for(Character c : theQuestion.question[i].toCharArray()) {
                row += solution.results.get(String.valueOf(c));
            }
            String paddedRow = ((i == theQuestion.question.length -1) ? opSymbol(theQuestion.op) + " " : leftPad) + row;

            String paddedVerbal = String.format(padFormat, theQuestion.question[i]);
            String paddedResultRow = String.format(padFormat, paddedRow);
            paddedVerbal = ((i == theQuestion.question.length -1) ? opSymbol(theQuestion.op) + " " : leftPad) + paddedVerbal;

            System.out.println(paddedVerbal + becomes + paddedResultRow);
        }
        // Magic +2 for the Op symbol and space
        System.out.println(leftPad + "=".repeat(lhsPaddingSize) + " ".repeat(becomes.length()+2) + "=".repeat(lhsPaddingSize));

        String row = "";
        for(Character c : theQuestion.answer.toCharArray()) {
            row += solution.results.get(String.valueOf(c));
        }
        System.out.println(leftPad + String.format(padFormat, theQuestion.answer) + becomes + "  " + String.format(padFormat, row));

    }

    protected String opSymbol(Op op) {
        return op == Op.ADD ? "+" : "-";
    }

}
