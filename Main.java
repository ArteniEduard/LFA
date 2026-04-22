import java.io.File;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Scanner;

class Character {
    String display;

    public Character(String display) {
        this.display = display;
    }

    @Override
    public String toString() {
        return display;
    }
}

class Transition {
    Character character;
    State state;

    @Override
    public String toString() {
        return character.display + " -> " + state.name;
    }
}

class State {
    boolean isFinal;
    String name;
    ArrayList<Transition> transitions;

    public State(String name) {
        this.name = name;
        this.transitions = new ArrayList<>();
        this.isFinal = false;
    }

    @Override
    public String toString() {
        return "{" + name + ", " + isFinal + "," + transitions + "}";
    }
}

class Automaton {
    ArrayList<Character> characters;
    ArrayList<State> states;
    State initialState;
    boolean isLambdaNFA = false;

    public Automaton(String[] chars, String[] names, String initialState, String[] finalStates, boolean isLambdaNFA) {
        characters = new ArrayList<>();
        states = new ArrayList<>();
        if (isLambdaNFA) {
            characters.add(new Character("lambda"));
        }

        for (String name : names) {
            State state = new State(name);
            if (initialState.equals(name)) {
                this.initialState = state;
            }
            for (String finalStateName : finalStates) {
                if (finalStateName.equals(name)) {
                    state.isFinal = true;
                    break;
                }
            }
            this.states.add(state);
        }
        for (String character : chars) {
            Character c = new Character(character);
            characters.add(c);
        }

        this.isLambdaNFA = isLambdaNFA;
    }

    public void addTransition(String s) {
        String[] split = s.split(" ");
        for (State state : states) {
            if (state.name.equals(split[0])) {
                Transition transition = new Transition();
                for (Character character : characters) {
                    if (character.display.equals(split[1])) {
                        transition.character = character;
                        break;
                    }
                }
                for (State state2 : states) {
                    if (state2.name.equals(split[2])) {
                        transition.state = state2;
                        break;
                    }
                }
                state.transitions.add(transition);
                break;
            }
        }
    }

    public boolean testWord(String word, State currentState, boolean displaySteps) {
        return testWord(word, currentState, displaySteps, new ArrayList<>());
    }

    public boolean testWord(String word, State currentState, boolean displaySteps, ArrayList<String> visited) {
        String key = currentState.name + "|" + word;

        if (visited.contains(key)) {
            return false;
        }

        visited.add(key);


        if (word.isEmpty()) {
            if (currentState.isFinal) {
                return true;
            }

            if (isLambdaNFA) {
                for (Transition t : currentState.transitions) {
                    if (t.character.display.equals("lambda")) {
                        if (testWord(word, t.state, displaySteps, new ArrayList<>(visited))) {
                            return true;
                        }
                    }
                }
            }

            return false;
        }

        String ch = String.valueOf(word.charAt(0));

        if (isLambdaNFA) {
            for (Transition t : currentState.transitions) {
                if (t.character.display.equals("lambda")) {
                    if (testWord(word, t.state, displaySteps, new ArrayList<>(visited))) {
                        return true;
                    }
                }
            }
        }

        for (Transition t : currentState.transitions) {
            if (t.character.display.equals(ch)) {
                if (displaySteps)
                    System.out.println("Using transition: " + t);

                if (testWord(word.substring(1), t.state, displaySteps, new ArrayList<>())) {
                    return true;
                }
            }
        }

        return false; // no valid transitions
    }

    public void print() {
        System.out.println("Characters = " + characters);
        System.out.println("States = " + states);
        System.out.println("Initial state = " + initialState.name);

    }
}

public class Main {
    public static void main(String[] args) {
        File input = new File("input.txt");
        try (Scanner scanner = new Scanner(input)) {

            boolean isLambdaNFA = Boolean.parseBoolean(scanner.nextLine());

            String characters = scanner.nextLine();
            String[] chars = characters.split(" ");

            String states = scanner.nextLine();
            String[] names = states.split(" ");

            String initialState = scanner.nextLine();
            initialState = initialState.strip();

            String finalStatesLine = scanner.nextLine();
            String[] finalStates = finalStatesLine.split(" ");

            Automaton automaton = new Automaton(chars, names, initialState, finalStates, isLambdaNFA);

            while (scanner.hasNextLine()) {
                String transition = scanner.nextLine();
                automaton.addTransition(transition);
            }

            automaton.print();
            //DFA
            /*
            System.out.println(automaton.testWord("b", automaton.initialState, false));
            System.out.println(automaton.testWord("ab", automaton.initialState, false));
            System.out.println(automaton.testWord("aab", automaton.initialState, false));
            System.out.println(automaton.testWord("a", automaton.initialState, false));
            System.out.println(automaton.testWord("bb", automaton.initialState, false));
            System.out.println(automaton.testWord("abba", automaton.initialState, false));
            */

            //NFA
            /*
            System.out.println(automaton.testWord("ab", automaton.initialState, false));
            System.out.println(automaton.testWord("aabbb", automaton.initialState, false));
            System.out.println(automaton.testWord("ac", automaton.initialState, false));
            System.out.println(automaton.testWord("a", automaton.initialState, false));
            System.out.println(automaton.testWord("b", automaton.initialState, false));
            System.out.println(automaton.testWord("abc", automaton.initialState, false));
            */

            //lambda-NFA
            /*
            System.out.println(automaton.testWord("b", automaton.initialState, false));
            System.out.println(automaton.testWord("bc", automaton.initialState, false));
            System.out.println(automaton.testWord("aaabccc", automaton.initialState, false));
            System.out.println(automaton.testWord("c", automaton.initialState, false));
            System.out.println(automaton.testWord("a", automaton.initialState, false));
            System.out.println(automaton.testWord("abcb", automaton.initialState, false));
            */

        } catch (Exception e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
}
