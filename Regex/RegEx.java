package Regex;

import java.util.ArrayList;
import java.util.HashMap;

class RegexTransition {

    State from;

    State to;

    String regex;

    public RegexTransition(State from, State to, String regex) {
        this.from = from;
        this.to = to;
        this.regex = regex;
    }

    @Override
    public String toString() {
        return "(" + from.name + ") --" + regex + "--> (" + to.name + ")";
    }
}

class State {

    String name;

    boolean isFinal;

    public State(String name) {
        this.name = name;
        this.isFinal = false;
    }

    @Override
    public String toString() {
        return name;
    }
}

class LambdaNFA {

    ArrayList<State> states;

    ArrayList<RegexTransition> transitions;

    State initialState;

    ArrayList<State> finalStates;

    public LambdaNFA(String[] stateNames, String initialStateName, String[] finalStateNames) {

        states = new ArrayList<>();

        transitions = new ArrayList<>();

        finalStates = new ArrayList<>();

        for (String name : stateNames) {

            State state = new State(name);

            if (name.equals(initialStateName)) {
                initialState = state;
            }

            for (String finalName : finalStateNames) {

                if (name.equals(finalName)) {
                    state.isFinal = true;
                    finalStates.add(state);
                }
            }

            states.add(state);
        }
    }

    public State getState(String name) {

        for (State state : states) {
            if (state.name.equals(name)) {
                return state;
            }
        }

        return null;
    }

    public void addTransition(String from, String symbol, String to) {

        State fromState = getState(from);

        State toState = getState(to);

        transitions.add(new RegexTransition(fromState, toState, symbol));
    }

    private String unionRegex(String r1, String r2) {

        if (r1 == null || r1.isEmpty()) {
            return r2;
        }

        if (r2 == null || r2.isEmpty()) {
            return r1;
        }

        if (r1.equals(r2)) {
            return r1;
        }

        return "(" + r1 + "|" + r2 + ")";
    }

    private String concatRegex(String r1, String r2) {

        if (r1 == null || r2 == null) {
            return null;
        }

        if (r1.equals("lambda")) {
            return r2;
        }

        if (r2.equals("lambda")) {
            return r1;
        }

        return r1 + r2;
    }

    private String starRegex(String r) {

        if (r == null) {
            return "lambda";
        }

        if (r.equals("lambda")) {
            return "lambda";
        }

        return "(" + r + ")*";
    }

    public String convertToRegex() {

        // PASUL 1
        // standardizarea automatului

        State newStart = new State("qs");

        State newFinal = new State("qf");

        states.add(newStart);
        states.add(newFinal);

        transitions.add(new RegexTransition(newStart, initialState, "lambda"));

        for (State finalState : finalStates) {

            transitions.add(new RegexTransition(finalState, newFinal, "lambda"));
        }

        initialState = newStart;

        finalStates.clear();

        finalStates.add(newFinal);


        // PASUL 2
        // unificare tranzitii

        HashMap<String, String> regexMap = new HashMap<>();

        for (RegexTransition transition : transitions) {

            String key = transition.from.name + "->" + transition.to.name;

            if (!regexMap.containsKey(key)) {

                regexMap.put(key, transition.regex);
            } else {

                regexMap.compute(key, (_, oldRegex) -> unionRegex(oldRegex, transition.regex));
            }
        }

//        System.out.println("regexMap = " + regexMap);

        // PASUL 3 si 4
        // eliminare stari si abstractizare

        ArrayList<State> removableStates = new ArrayList<>();

        for (State state : states) {

            if (state != initialState && !finalStates.contains(state)) {

                removableStates.add(state);
            }
        }

        for (State eliminated : removableStates) {

            HashMap<String, String> newRegexMap = new HashMap<>(regexMap);

            for (State p : states) {

                if (p == eliminated) {
                    continue;
                }

                for (State r : states) {

                    if (r == eliminated) {
                        continue;
                    }

                    String pk = regexMap.get(p.name + "->" + eliminated.name);

                    String kk = regexMap.get(eliminated.name + "->" + eliminated.name);

                    String kr = regexMap.get(eliminated.name + "->" + r.name);

                    if (pk == null || kr == null) {
                        continue;
                    }

                    String part = concatRegex(concatRegex(pk, starRegex(kk)), kr);

                    String prKey = p.name + "->" + r.name;

                    String pr = regexMap.get(prKey);

                    String result = unionRegex(pr, part);

                    newRegexMap.put(prKey, result);
                }
            }

            // stergem toate tranzitiile
            // care folosesc starea eliminata

            ArrayList<String> toRemove = new ArrayList<>();

            for (String key : newRegexMap.keySet()) {

                if (key.startsWith(eliminated.name + "->") || key.endsWith("->" + eliminated.name)) {

                    toRemove.add(key);
                }
            }

            for (String key : toRemove) {
                newRegexMap.remove(key);
            }

            regexMap = newRegexMap;
        }

        String finalKey = initialState.name + "->" + finalStates.get(0).name;

        return regexMap.get(finalKey);
    }

    public void printTransitions() {

        for (RegexTransition transition : transitions) {
            System.out.println(transition);
        }
    }
}

public class RegEx {

    public static void main(String[] args) {

        String[] states = {"q0", "q1", "q2", "q3", "q4", "q5", "q6"};

        String[] finalStates = {"q3"};

        LambdaNFA nfa = new LambdaNFA(states, "q0", finalStates);

        nfa.addTransition("q0", "lambda", "q1");
        nfa.addTransition("q0", "lambda", "q2");
        nfa.addTransition("q1", "lambda", "q2");
        nfa.addTransition("q1", "a", "q2");
        nfa.addTransition("q2", "lambda", "q1");
        nfa.addTransition("q2", "b", "q3");
        nfa.addTransition("q3", "c", "q3");
        System.out.println("Transitions:");

        nfa.printTransitions();

        System.out.println();

        String regex = nfa.convertToRegex();

        System.out.println("Equivalent regex:");

        System.out.println(regex);
    }
}

