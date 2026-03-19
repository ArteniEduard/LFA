import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

class Character{
    String display;
    public Character(String display){
        this.display = display;
    }

    @Override
    public String toString() {
        return display;
    }
}

class Transition{
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

    public State(String name){
        this.name = name;
        this.transitions =  new ArrayList<>();
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

    public Automaton(String[] chars, String[] names, String initialState, String[] finalStates){
        characters = new ArrayList<>();
        states = new ArrayList<>();

        for (String name: names) {
            State state = new State(name);
            if(initialState.equals(name)){
                this.initialState = state;
            }
            for (String finalStateName: finalStates) {
                if (finalStateName.equals(name)) {
                    state.isFinal = true;
                    break;
                }
            }
            this.states.add(state);
        }
        for (String character: chars) {
            Character c = new Character(character);
            characters.add(c);
        }
    }

    public void addTransition(String s){
        String[] split = s.split(" ");
        for (State state: states) {
            if(state.name.equals(split[0])){
                Transition transition = new Transition();
                for (Character character:characters) {
                    if(character.display.equals(split[1])){
                        transition.character = character;
                        break;
                    }
                }
                for (State state2: states) {
                    if(state.name.equals(split[2])){
                        transition.state = state2;
                        break;
                    }
                }
                state.transitions.add(transition);
                break;
            }
        }
    }

    public boolean testWord(String word, State currentState){
        boolean valid = true;
        int len = 1;
        Transition transition = null;
        while (valid){
            valid = false;
            for (Transition t: currentState.transitions) {
                if (t.character.display.equals(word.substring(0, len))) {
                    transition = t;
                    valid = true;
                    len++;
                    break;
                }
            }
        }
        System.out.println("Finally we are using the transition " + transition);
        return true;
    }
    public void print(){
        System.out.println("Characters = " + characters);
        System.out.println("States = " + states);
        System.out.println("Initial state = " + initialState);

    }
}

public class Main{
    public static void main(String[] args) {
        File input = new File("/Users/student/IdeaProjects/java243/lab2java/src/input.txt");
        try (Scanner scanner = new Scanner(input)) {
            String characters = scanner.nextLine();
            String[] chars = characters.split(" ");

            String states = scanner.nextLine();
            String[] names = states.split(" ");

            String initialState = scanner.nextLine();
            initialState = initialState.strip();

            String finalStatesLine = scanner.nextLine();
            String[] finalStates = finalStatesLine.split(" ");

            Automaton automaton = new Automaton(chars, names, initialState, finalStates);

            while (scanner.hasNextLine()) {
                String transition = scanner.nextLine();
                automaton.addTransition(transition);
            }

            automaton.print();
            automaton.testWord("abc", automaton.initialState);


        } catch (Exception e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
}
