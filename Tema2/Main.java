package Tema2;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Stack;

enum AcceptanceMode {
    FINAL_STATE, EMPTY_STACK, BOTH
}

class Symbol {
    String value;

    public Symbol(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}

class PDATransition {

    State fromState;

    String inputSymbol;

    String stackTop;

    State toState;

    String pushString;

    @Override
    public String toString() {
        return "(" + fromState.name + ", " + inputSymbol + ", " + stackTop + ") = (" + toState.name + ", " + pushString + ")";
    }
}

class State {

    String name;

    boolean isFinal;

    ArrayList<PDATransition> transitions;

    public State(String name) {
        this.name = name;
        this.isFinal = false;
        this.transitions = new ArrayList<>();
    }

    @Override
    public String toString() {
        return "{" + name + ", final=" + isFinal + "}";
    }
}

class PDA {

    ArrayList<Symbol> inputAlphabet;

    ArrayList<Symbol> stackAlphabet;

    ArrayList<State> states;

    State initialState;

    String initialStackSymbol;

    AcceptanceMode acceptanceMode;

    public PDA(String[] inputSymbols, String[] stackSymbols, String[] stateNames, String initialStateName, String[] finalStates, String initialStackSymbol, AcceptanceMode acceptanceMode) {

        this.inputAlphabet = new ArrayList<>();
        this.stackAlphabet = new ArrayList<>();
        this.states = new ArrayList<>();

        this.initialStackSymbol = initialStackSymbol;

        this.acceptanceMode = acceptanceMode;

        for (String stateName : stateNames) {

            State state = new State(stateName);

            if (stateName.equals(initialStateName)) {
                this.initialState = state;
            }

            for (String finalState : finalStates) {
                if (finalState.equals(stateName)) {
                    state.isFinal = true;
                    break;
                }
            }

            states.add(state);
        }

        for (String s : inputSymbols) {
            inputAlphabet.add(new Symbol(s));
        }

        for (String s : stackSymbols) {
            stackAlphabet.add(new Symbol(s));
        }
    }

    public void addTransition(String line) {

        String[] split = line.split(" ");

        String from = split[0];
        String input = split[1];
        String stackTop = split[2];
        String to = split[3];
        String push = split[4];

        PDATransition transition = new PDATransition();

        for (State state : states) {
            if (state.name.equals(from)) {
                transition.fromState = state;
                break;
            }
        }

        for (State state : states) {
            if (state.name.equals(to)) {
                transition.toState = state;
                break;
            }
        }

        transition.inputSymbol = input;
        transition.stackTop = stackTop;
        transition.pushString = push;

        transition.fromState.transitions.add(transition);
    }

    public boolean testWord(String word, boolean displaySteps) {

        Stack<String> stack = new Stack<>();

        stack.push(initialStackSymbol);

        return testWordRecursive(word, initialState, stack, displaySteps, new ArrayList<>());
    }

    private boolean testWordRecursive(String word, State currentState, Stack<String> stack, boolean displaySteps, ArrayList<String> visited) {

        String stackContent = stack.toString();

        String key = currentState.name + "|" + word + "|" + stackContent;

        // evitam loop infinit
        if (visited.contains(key)) {
            return false;
        }

        visited.add(key);

        // verificare acceptare
        if (word.isEmpty()) {

            boolean finalStateAccepted = currentState.isFinal;

            boolean emptyStackAccepted = stack.isEmpty();

            switch (acceptanceMode) {

                case FINAL_STATE:
                    if (finalStateAccepted) {
                        return true;
                    }
                    break;

                case EMPTY_STACK:
                    if (emptyStackAccepted) {
                        return true;
                    }
                    break;

                case BOTH:
                    if (finalStateAccepted && emptyStackAccepted) {
                        return true;
                    }
                    break;
            }
        }

        // toate tranzitiile posibile
        for (PDATransition transition : currentState.transitions) {

            // verificare input
            boolean inputMatches = false;

            if (transition.inputSymbol.equals("lambda")) {
                inputMatches = true;
            } else if (!word.isEmpty() && transition.inputSymbol.equals(String.valueOf(word.charAt(0)))) {
                inputMatches = true;
            }

            if (!inputMatches) {
                continue;
            }

            // verificare stiva
            if (stack.isEmpty()) {
                continue;
            }

            String top = stack.peek();

            if (!top.equals(transition.stackTop)) {
                continue;
            }

            // clonam stiva pentru branch nou
            Stack<String> newStack = (Stack<String>) stack.clone();

            // scoatem varful
            newStack.pop();

            // PUSH
            // daca nu e lambda
            if (!transition.pushString.equals("lambda")) {

                // push in ordine inversa
                // deoarece primul caracter trebuie
                // sa ajunga in varf
                for (int i = transition.pushString.length() - 1; i >= 0; i--) {

                    newStack.push(String.valueOf(transition.pushString.charAt(i)));
                }
            }

            // consum input
            String remainingWord = word;

            if (!transition.inputSymbol.equals("lambda")) {
                remainingWord = word.substring(1);
            }

            if (displaySteps) {

                System.out.println("--------------------------------");

                System.out.println("Current state: " + currentState.name);

                System.out.println("Remaining word: " + word);

                System.out.println("Stack before: " + stack);

                System.out.println("Using transition: " + transition);

                System.out.println("Stack after: " + newStack);
            }

            boolean accepted = testWordRecursive(remainingWord, transition.toState, newStack, displaySteps, new ArrayList<>(visited));

            if (accepted) {
                return true;
            }
        }

        return false;
    }

    public void print() {

        System.out.println("Acceptance mode: " + acceptanceMode);
        System.out.println("States: " + states);
        System.out.println("Initial state: " + initialState.name);
        System.out.println("Initial stack symbol: " + initialStackSymbol);
        System.out.println("Transitions:");

        for (State state : states) {
            for (PDATransition transition : state.transitions) {
                System.out.println(transition);
            }
        }

        System.out.println();
    }
}

public class Main {

    public static void main(String[] args) {

        File input = new File("Tema2/input.txt");

        try (Scanner scanner = new Scanner(input)) {

            AcceptanceMode acceptanceMode = AcceptanceMode.valueOf(scanner.nextLine());

            String[] inputSymbols = scanner.nextLine().split(" ");

            String[] stackSymbols = scanner.nextLine().split(" ");

            String[] stateNames = scanner.nextLine().split(" ");

            String initialState = scanner.nextLine().strip();

            String[] finalStates = scanner.nextLine().split(" ");

            String initialStackSymbol = scanner.nextLine().strip();

            PDA pda = new PDA(inputSymbols, stackSymbols, stateNames, initialState, finalStates, initialStackSymbol, acceptanceMode);

            // tranzitii
            while (scanner.hasNextLine()) {

                String line = scanner.nextLine();

                if (line.isBlank()) {
                    continue;
                }

                pda.addTransition(line);
            }

            pda.print();

            System.out.println("ab -> " +
                    pda.testWord("ab", false));

            System.out.println("aabb -> " +
                    pda.testWord("aabb", false));

            System.out.println("aaabbb -> " +
                    pda.testWord("aaabbb", false));

            System.out.println("aaaabbbb -> " +
                    pda.testWord("aaaabbbb", false));

            System.out.println("aaaaabbbbb -> " +
                    pda.testWord("aaaaabbbbb", false));

            System.out.println("aab -> " +
                    pda.testWord("aab", false));

            System.out.println("abb -> " +
                    pda.testWord("abb", false));

            System.out.println("aaabbbb -> " +
                    pda.testWord("aaabbbb", false));

            System.out.println("b -> " +
                    pda.testWord("b", false));

            System.out.println("ba -> " +
                    pda.testWord("ba", false));

            System.out.println("aaaa -> " +
                    pda.testWord("aaaa", false));

        } catch (Exception e) {

            System.out.println("An error occurred.");

            e.printStackTrace();
        }
    }
}