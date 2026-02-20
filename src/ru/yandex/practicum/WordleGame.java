package ru.yandex.practicum;

import java.io.PrintWriter;
import java.util.*;

public class WordleGame {

    private final String answer;
    private int attemptsLeft;
    private final WordleDictionary dictionary;
    private final PrintWriter log;

    private final List<GuessResult> history = new ArrayList<>();
    private Set<String> possibleWords;
    private final Set<Character> presentLetters = new HashSet<>();
    private final Set<Character> absentLetters = new HashSet<>();
    private final Map<Integer, Character> exactMatches = new HashMap<>();
    private final Map<Integer, Set<Character>> forbiddenPositions = new HashMap<>();
    private final Set<String> usedHints = new HashSet<>();
    private final Random random = new Random();

    // Основной конструктор (случайное слово)
    public WordleGame(WordleDictionary dictionary, PrintWriter log) {
        this.dictionary = dictionary;
        this.log = log;
        this.answer = dictionary.getRandomWord();
        this.attemptsLeft = 6;
        this.possibleWords = new HashSet<>(dictionary.getAllWords());

        for (int i = 0; i < 5; i++) {
            forbiddenPositions.put(i, new HashSet<>());
        }

        log.println("=== НОВАЯ ИГРА ===");
        log.println("Загадано слово: " + answer);
    }

    // Второй конструктор для тестов (фиксированное слово)
    public WordleGame(WordleDictionary dictionary, PrintWriter log, String fixedAnswer) {
        this.dictionary = dictionary;
        this.log = log;
        this.answer = fixedAnswer;
        this.attemptsLeft = 6;
        this.possibleWords = new HashSet<>(dictionary.getAllWords());

        for (int i = 0; i < 5; i++) {
            forbiddenPositions.put(i, new HashSet<>());
        }

        log.println("=== НОВАЯ ИГРА ===");
        log.println("Загадано слово (тестовое): " + answer);
    }

    public String makeGuess(String guess) throws WordNotFoundException {
        log.println("Попытка: " + guess);

        if (!dictionary.contains(guess)) {
            log.println("Слово \"" + guess + "\" не найдено в словаре");
            throw new WordNotFoundException("Слово \"" + guess + "\" отсутствует в словаре");
        }

        attemptsLeft--;
        log.println("Осталось попыток: " + attemptsLeft);

        String result = analyzeGuess(guess);
        log.println("Результат: " + result);

        history.add(new GuessResult(guess, result));

        updateLetterInfo(guess, result);
        filterPossibleWords();

        return result;
    }

    private String analyzeGuess(String guess) {
        char[] result = new char[5];
        boolean[] answerUsed = new boolean[5];
        boolean[] guessUsed = new boolean[5];

        for (int i = 0; i < 5; i++) {
            if (guess.charAt(i) == answer.charAt(i)) {
                result[i] = '+';
                answerUsed[i] = true;
                guessUsed[i] = true;
            }
        }

        for (int i = 0; i < 5; i++) {
            if (guessUsed[i]) continue;

            char guessChar = guess.charAt(i);
            boolean found = false;

            for (int j = 0; j < 5; j++) {
                if (!answerUsed[j] && answer.charAt(j) == guessChar) {
                    found = true;
                    answerUsed[j] = true;
                    break;
                }
            }

            if (found) {
                result[i] = '^';
            } else {
                result[i] = '-';
            }
        }

        return new String(result);
    }

    private void updateLetterInfo(String guess, String result) {
        for (int i = 0; i < 5; i++) {
            char c = guess.charAt(i);
            char r = result.charAt(i);

            if (r == '+') {
                exactMatches.put(i, c);
                presentLetters.add(c);
            } else if (r == '^') {
                presentLetters.add(c);
                forbiddenPositions.get(i).add(c);
            } else if (r == '-') {
                if (!presentLetters.contains(c) && !exactMatches.containsValue(c)) {
                    absentLetters.add(c);
                }
            }
        }

        log.println("Известные буквы: " + presentLetters);
        log.println("Точные совпадения: " + exactMatches);
        log.println("Отсутствующие буквы: " + absentLetters);
        log.println("Запрещённые позиции: " + forbiddenPositions);
    }

    private void filterPossibleWords() {
        Set<String> filtered = new HashSet<>();

        for (String word : possibleWords) {
            boolean ok = true;

            // Проверка отсутствующих букв
            for (char c : absentLetters) {
                if (word.indexOf(c) >= 0) {
                    ok = false;
                    break;
                }
            }
            if (!ok) continue;

            // Проверка точных совпадений
            for (Map.Entry<Integer, Character> entry : exactMatches.entrySet()) {
                if (word.charAt(entry.getKey()) != entry.getValue()) {
                    ok = false;
                    break;
                }
            }
            if (!ok) continue;

            // Проверка запрещённых позиций (буквы ^)
            for (Map.Entry<Integer, Set<Character>> entry : forbiddenPositions.entrySet()) {
                int pos = entry.getKey();
                for (char c : entry.getValue()) {
                    if (word.charAt(pos) == c) {
                        ok = false;
                        break;
                    }
                }
                if (!ok) break;
            }
            if (!ok) continue;

            // Проверка присутствующих букв
            for (char c : presentLetters) {
                if (word.indexOf(c) < 0) {
                    ok = false;
                    break;
                }
            }

            if (ok) {
                filtered.add(word);
            }
        }

        possibleWords = filtered;
        log.println("Осталось возможных слов: " + possibleWords.size());
    }

    public String getHint() {
        if (possibleWords.isEmpty()) {
            log.println("Нет возможных слов для подсказки!");
            return "???";
        }

        // Ищем слово, которое ещё не использовали как подсказку
        List<String> availableWords = new ArrayList<>();
        for (String word : possibleWords) {
            if (!usedHints.contains(word)) {
                availableWords.add(word);
            }
        }

        if (availableWords.isEmpty()) {
            // Если все слова уже были подсказаны, сбрасываем
            usedHints.clear();
            availableWords = new ArrayList<>(possibleWords);
        }

        String hint = availableWords.get(random.nextInt(availableWords.size()));
        usedHints.add(hint);

        log.println("Подсказка: " + hint);
        return hint;
    }

    public String getAnswer() {
        return answer;
    }

    public boolean isGameOver() {
        return attemptsLeft <= 0 || isWin();
    }

    public boolean isWin() {
        if (history.isEmpty()) return false;
        return history.get(history.size() - 1).result.equals("+++++");
    }

    public int getAttemptsLeft() {
        return attemptsLeft;
    }

    public List<GuessResult> getHistory() {
        return new ArrayList<>(history);
    }
}