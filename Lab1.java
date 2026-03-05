import java.util.ArrayList;
import java.util.HashMap;

public class Main {

    public static ArrayList<Integer> states = new ArrayList<>();
    public static ArrayList<String> alphabet = new ArrayList<>();
    public static Integer initialState;
    public static ArrayList<Integer> finalStates = new ArrayList<>();
    public static HashMap<Integer, HashMap<String, Integer>> transitions = new HashMap<>();

    public static boolean testWord(String word) {
        Integer currentState = initialState;
        String wordTillNow = "";
        String[] characters = word.split("");
        for (String character : characters) {
            wordTillNow = wordTillNow.concat(character);
            System.out.println("Caracter citit " + character + " cuvant pana acum " + wordTillNow);

            if (alphabet.contains(character)) {
                if (transitions.containsKey(currentState)) {
                    if (transitions.get(currentState).containsKey(character)) {
                        System.out.println("stare inainte " + currentState);
                        currentState = transitions.get(currentState).get(character);
                        System.out.println("stare dupa " + currentState);
                    } else {
                        System.out.println("Cuvant gresit");
                        return false;
                    }
                } else {
                    System.out.println("Acest nod nu are iesiri");
                    return false;
                }
            } else {
                System.out.println("Caracterul acesta nu exista");
                return false;
            }
        }

        System.out.println("Sa terminat cuvantul");
        if (finalStates.contains(currentState)) {
            System.out.println("Cuvant valid===================================================");
            System.out.println("");
            return true;
        } else {
            System.err.println("Ultima stare nu este stare finala==============================");
            System.out.println("");
            return false;
        }

    }

    public static void main(String[] args) {
        states.add(1);
        states.add(2);
        states.add(3);
        states.add(4);
        states.add(5);
        alphabet.add("a");
        alphabet.add("b");
        initialState = 1;
        finalStates.add(1);
        finalStates.add(5);
        transitions.put(1, new HashMap<>());
        transitions.get(1).put("a", 2);
        transitions.get(1).put("b", 3);
        transitions.put(2, new HashMap<>());
        transitions.get(2).put("a", 1);
        transitions.put(3, new HashMap<>());
        transitions.get(3).put("b", 4);
        transitions.put(4, new HashMap<>());
        transitions.get(4).put("b", 5);
        transitions.put(5, new HashMap<>());
        transitions.get(5).put("b", 3);

        testWord("aa");
        testWord("bbb");
        testWord("aaaabbbbbb");
        testWord("ab");
        testWord("aab");
    }
}
