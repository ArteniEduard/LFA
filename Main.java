import java.util.*;

class CFG {
    private String startSymbol;
    private Map<String, List<List<String>>> rules;

    public CFG(String startSymbol) {
        this.startSymbol = startSymbol;
        this.rules = new HashMap<>();
    }

    public void addRule(String nonTerminal, List<String> production) {
        rules.computeIfAbsent(nonTerminal, k -> new ArrayList<>()).add(production);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Start symbol: ").append(startSymbol).append("\n");
        sb.append("Rules:\n");

        for (Map.Entry<String, List<List<String>>> entry : rules.entrySet()) {
            sb.append(entry.getKey()).append(" -> ");

            List<List<String>> productions = entry.getValue();
            for (int i = 0; i < productions.size(); i++) {
                sb.append(String.join(" ", productions.get(i)));
                if (i < productions.size() - 1) {
                    sb.append(" | ");
                }
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    public Set<String> generateAllWordsOfLength(int k) {
        Set<String> result = new HashSet<>();
        generateRecursive(List.of(startSymbol), k, result);
        return result;
    }

    private void generateRecursive(List<String> currentWord, int k, Set<String> result) {
        int terminalsLength = currentWord.stream().filter(s -> !rules.containsKey(s)).mapToInt(String::length).sum();
        if (terminalsLength > k) return;

        // caut primul simbol neterminal
        for (int i = 0; i < currentWord.size(); i++) {
            String symbol = currentWord.get(i);
            if (rules.containsKey(symbol)) {
                // inlocuim neterminalul cu fiecare producție
                for (List<String> production : rules.get(symbol)) {
                    List<String> nextWord = new ArrayList<>(currentWord);
                    nextWord.remove(i);
                    nextWord.addAll(i, production);
                    generateRecursive(nextWord, k, result);
                }
                return;
            }
        }

        // nu mai avem neterminale
        String wordStr = String.join("", currentWord);
        if (wordStr.length() == k) {
            result.add(wordStr);
        }
    }

    public CFG copy() {
        CFG c = new CFG(startSymbol);
        for (var e : rules.entrySet())
            for (var r : e.getValue())
                c.addRule(e.getKey(), new ArrayList<>(r));
        return c;
    }

    public void startStep() {
        String newStart = startSymbol + "_S";
        rules.put(newStart, List.of(List.of(startSymbol)));
        startSymbol = newStart;
    }

    public void termStep() {
        Map<String, String> terminalMap = new HashMap<>();
        Map<String, List<List<String>>> newRules = new HashMap<>();
        int id = 0;

        for (var e : rules.entrySet()) {
            String A = e.getKey();

            for (List<String> prod : e.getValue()) {
                List<String> newProd = new ArrayList<>();

                for (String s : prod) {
                    if (rules.containsKey(s)) { // non-terminal
                        newProd.add(s);
                    } else { // terminal
                        terminalMap.putIfAbsent(s, "T" + (id++));
                        String T = terminalMap.get(s);
                        newProd.add(T);
                    }
                }

                newRules.computeIfAbsent(A, k -> new ArrayList<>()).add(newProd);
            }
        }

        for (var e : terminalMap.entrySet()) {
            newRules.put(e.getValue(), List.of(List.of(e.getKey())));
        }

        rules = newRules;
    }

    public void binStep() {
        Map<String, List<List<String>>> newRules = new HashMap<>();
        int id = 0;

        for (var e : rules.entrySet()) {
            String A = e.getKey();

            for (List<String> rhs : e.getValue()) {

                if (rhs.size() <= 2) {
                    newRules.computeIfAbsent(A, k -> new ArrayList<>()).add(rhs);
                    continue;
                }

                String prev = A;

                for (int i = 0; i < rhs.size() - 2; i++) {
                    String X = "X" + (id++);
                    newRules.computeIfAbsent(prev, k -> new ArrayList<>()).add(List.of(rhs.get(i), X));
                    prev = X;
                }

                newRules.computeIfAbsent(prev, k -> new ArrayList<>()).add(List.of(rhs.get(rhs.size() - 2), rhs.get(rhs.size() - 1)));
            }
        }

        rules = newRules;
    }

    public void lambdaStep() {
        Set<String> nullable = new HashSet<>();

        boolean changed;
        do {
            changed = false;
            for (var e : rules.entrySet()) {
                for (List<String> prod : e.getValue()) {
                    if (prod.isEmpty() || prod.stream().allMatch(nullable::contains)) {
                        if (nullable.add(e.getKey())) changed = true;
                    }
                }
            }
        } while (changed);

        Map<String, List<List<String>>> newRules = new HashMap<>();

        for (var e : rules.entrySet()) {
            String A = e.getKey();
            Set<List<String>> generated = new HashSet<>();

            for (List<String> prod : e.getValue()) {
                generateNullable(prod, nullable, 0, new ArrayList<>(), generated);
            }

            generated.removeIf(p -> p.isEmpty() && !A.equals(startSymbol));
            newRules.put(A, new ArrayList<>(generated));
        }

        rules = newRules;
    }

    private void generateNullable(List<String> prod, Set<String> nullable, int i, List<String> cur, Set<List<String>> res) {
        if (i == prod.size()) {
            res.add(new ArrayList<>(cur));
            return;
        }

        String s = prod.get(i);

        if (nullable.contains(s)) {
            generateNullable(prod, nullable, i + 1, cur, res);
        }

        cur.add(s);
        generateNullable(prod, nullable, i + 1, cur, res);
        cur.remove(cur.size() - 1);
    }

    public void unitStep() {
        Map<String, Set<String>> unitGraph = new HashMap<>();

        for (String A : rules.keySet()) {
            unitGraph.putIfAbsent(A, new HashSet<>());

            for (List<String> prod : rules.get(A)) {
                if (prod.size() == 1 && rules.containsKey(prod.get(0))) {
                    unitGraph.get(A).add(prod.get(0));
                }
            }
        }

        Map<String, List<List<String>>> newRules = new HashMap<>();

        for (String A : rules.keySet()) {
            Set<String> reachable = new HashSet<>();
            Queue<String> q = new LinkedList<>();
            q.add(A);

            while (!q.isEmpty()) {
                String cur = q.poll();
                for (String nxt : unitGraph.getOrDefault(cur, Set.of())) {
                    if (reachable.add(nxt)) q.add(nxt);
                }
            }

            List<List<String>> prods = new ArrayList<>();

            for (String B : reachable) {
                for (List<String> prod : rules.get(B)) {
                    if (!(prod.size() == 1 && rules.containsKey(prod.get(0)))) {
                        prods.add(prod);
                    }
                }
            }

            for (List<String> prod : rules.get(A)) {
                if (!(prod.size() == 1 && rules.containsKey(prod.get(0)))) {
                    prods.add(prod);
                }
            }

            newRules.put(A, prods);
        }

        rules = newRules;
    }

    public CFG toCNF() {
        CFG c = this.copy();

        c.startStep();
        c.termStep();
        c.binStep();
        c.lambdaStep();
        c.unitStep();

        return c;
    }

    public boolean fakeCYK(String word){
        Set<String> words = this.generateAllWordsOfLength(word.length());
        if(words.contains(word)){
            return true;
        }else {
            return false;
        }
    }


}

public class Main {
    public static void main(String[] args) {
        CFG cfg = new CFG("R");

        cfg.addRule("R", List.of("X","R","X"));
        cfg.addRule("R", List.of("S"));

        cfg.addRule("S", List.of("a", "T","b"));
        cfg.addRule("S", List.of("b", "T","a"));

        cfg.addRule("T", List.of("X", "T", "X"));
        cfg.addRule("T", List.of("X"));
        cfg.addRule("T", List.of(""));
        cfg.addRule("X", List.of("a"));
        cfg.addRule("X", List.of("b"));

        System.out.println(cfg);

        CFG cnf = cfg.toCNF();
        System.out.println(cnf);
        Set<String> words = cfg.generateAllWordsOfLength(4);
        Set<String> words2 = cnf.generateAllWordsOfLength(4);
        System.out.println(words);
        System.out.println(words2);


    }
}