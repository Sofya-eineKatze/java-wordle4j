package ru.yandex.practicum;

import org.junit.jupiter.api.*;
import java.io.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class WordleTest {

    private PrintWriter testLog;
    private StringWriter logOutput;

    @BeforeEach
    void setUp() {
        logOutput = new StringWriter();
        testLog = new PrintWriter(logOutput, true);
    }

    @Test
    @DisplayName("Загрузка словаря из файла")
    void testLoadDictionary() throws IOException {
        File tempFile = File.createTempFile("test_dict", ".txt");
        tempFile.deleteOnExit();

        try (PrintWriter writer = new PrintWriter(tempFile, "UTF-8")) {
            writer.println("абзац");
            writer.println("аббат");
            writer.println("аборт");
            writer.println("абвер");
            writer.println("абгаз");
        }

        WordleDictionaryLoader loader = new WordleDictionaryLoader(testLog);
        WordleDictionary loadedDict = loader.loadDictionary(tempFile.getAbsolutePath());

        assertEquals(5, loadedDict.size());
        assertTrue(loadedDict.contains("абзац"));
        assertTrue(loadedDict.contains("аббат"));
        assertTrue(loadedDict.contains("аборт"));
        assertTrue(loadedDict.contains("абвер"));
        assertTrue(loadedDict.contains("абгаз"));
    }

    @Test
    @DisplayName("Замена 'ё' на 'е' при загрузке")
    void testYoReplacement() throws IOException {
        File tempFile = File.createTempFile("test_yo", ".txt");
        tempFile.deleteOnExit();

        try (PrintWriter writer = new PrintWriter(tempFile, "UTF-8")) {
            writer.println("пенёк");
            writer.println("тёлка");
            writer.println("пёсик");
            writer.println("сёрфинг");
            writer.println("ёжик");
            writer.println("котик");
        }

        WordleDictionaryLoader loader = new WordleDictionaryLoader(testLog);
        WordleDictionary loadedDict = loader.loadDictionary(tempFile.getAbsolutePath());

        List<String> words = loadedDict.getAllWords();
        assertEquals(4, words.size());
        assertTrue(words.contains("пенек"));
        assertTrue(words.contains("телка"));
        assertTrue(words.contains("песик"));
        assertTrue(words.contains("котик"));
    }

    @Test
    @DisplayName("Фильтрация слов не из 5 букв")
    void testFilterByLength() throws IOException {
        File tempFile = File.createTempFile("test_length", ".txt");
        tempFile.deleteOnExit();

        try (PrintWriter writer = new PrintWriter(tempFile, "UTF-8")) {
            writer.println("абзац");
            writer.println("абажур");
            writer.println("аббат");
            writer.println("абракадабра");
            writer.println("аборт");
            writer.println("абрикос");
        }

        WordleDictionaryLoader loader = new WordleDictionaryLoader(testLog);
        WordleDictionary loadedDict = loader.loadDictionary(tempFile.getAbsolutePath());

        assertEquals(3, loadedDict.size());
        assertTrue(loadedDict.contains("абзац"));
        assertTrue(loadedDict.contains("аббат"));
        assertTrue(loadedDict.contains("аборт"));
    }

    @Test
    @DisplayName("Проверка наличия слова в словаре")
    void testContains() {
        List<String> words = Arrays.asList("абзац", "аббат", "аборт", "абсурд");
        WordleDictionary dict = new WordleDictionary(words, testLog);

        assertTrue(dict.contains("абзац"));
        assertTrue(dict.contains("аббат"));
        assertTrue(dict.contains("аборт"));
        assertTrue(dict.contains("абсурд"));
        assertFalse(dict.contains("абажур"));
        assertFalse(dict.contains("несуществующее"));
    }

    @Test
    @DisplayName("Анализ полного совпадения")
    void testExactMatch() throws WordNotFoundException {
        List<String> words = Arrays.asList("абзац");
        WordleDictionary testDict = new WordleDictionary(words, testLog);
        WordleGame testGame = new WordleGame(testDict, testLog);

        String result = testGame.makeGuess("абзац");
        assertEquals("+++++", result);
        assertTrue(testGame.isWin());
    }

    @Test
    @DisplayName("Анализ частичного совпадения")
    void testPartialMatch() throws WordNotFoundException {
        List<String> words = Arrays.asList("абзац", "аборт");
        WordleDictionary testDict = new WordleDictionary(words, testLog);
        WordleGame testGame = new WordleGame(testDict, testLog, "абзац");

        String result = testGame.makeGuess("аборт");
        assertEquals("++---", result);
        assertFalse(testGame.isWin());
    }

    @Test
    @DisplayName("Анализ без совпадений")
    void testNoMatch() throws WordNotFoundException {
        List<String> words = Arrays.asList("абзац", "входы");
        WordleDictionary testDict = new WordleDictionary(words, testLog);
        WordleGame testGame = new WordleGame(testDict, testLog, "абзац");

        String result = testGame.makeGuess("входы");
        assertEquals("-----", result);
    }

    @Test
    @DisplayName("Буква есть, но позиция не та")
    void testMisplacedLetter() throws WordNotFoundException {
        List<String> words = Arrays.asList("аборт", "табор");
        WordleDictionary testDict = new WordleDictionary(words, testLog);
        WordleGame testGame = new WordleGame(testDict, testLog, "аборт");

        String result = testGame.makeGuess("табор");
        assertEquals("^^^^^", result);
    }

    @Test
    @DisplayName("Обработка повторяющихся букв")
    void testDuplicateLetters() throws WordNotFoundException {
        List<String> words = Arrays.asList("аббат", "абажур");
        WordleDictionary testDict = new WordleDictionary(words, testLog);
        WordleGame game = new WordleGame(testDict, testLog, "аббат");

        String result = game.makeGuess("абажур");
        assertEquals("++^--", result);
    }

    @Test
    @DisplayName("Исключение при слове не из словаря")
    void testWordNotFound() {
        List<String> words = Arrays.asList("абзац");
        WordleDictionary testDict = new WordleDictionary(words, testLog);
        WordleGame testGame = new WordleGame(testDict, testLog);

        assertThrows(WordNotFoundException.class, () -> {
            testGame.makeGuess("ххххх");
        });
    }

    @Test
    @DisplayName("Подсказка без введенных слов")
    void testHintNoGuesses() {
        List<String> words = Arrays.asList("абзац", "аборт", "аббат", "абрис");
        WordleDictionary testDict = new WordleDictionary(words, testLog);
        WordleGame hintGame = new WordleGame(testDict, testLog);

        String hint = hintGame.getHint();
        assertNotNull(hint);
        assertEquals(5, hint.length());
    }

    @Test
    @DisplayName("Подсказка с учетом известных букв")
    void testHintWithKnownLetters() throws WordNotFoundException {
        List<String> words = Arrays.asList("абзац", "аборт", "аббат", "абрис");
        WordleDictionary testDict = new WordleDictionary(words, testLog);
        WordleGame hintGame = new WordleGame(testDict, testLog);

        hintGame.makeGuess("аборт");
        String hint = hintGame.getHint();

        assertNotNull(hint);
        assertEquals('а', hint.charAt(0));
        assertEquals('б', hint.charAt(1));
    }

    @Test
    @DisplayName("Исключение при пустом файле")
    void testEmptyFile() throws IOException {
        File tempFile = File.createTempFile("empty", ".txt");
        tempFile.deleteOnExit();

        WordleDictionaryLoader loader = new WordleDictionaryLoader(testLog);

        assertThrows(DictionaryEmptyException.class, () -> {
            loader.loadDictionary(tempFile.getAbsolutePath());
        });
    }

    @Test
    @DisplayName("Исключение при отсутствии файла")
    void testFileNotFound() {
        WordleDictionaryLoader loader = new WordleDictionaryLoader(testLog);

        assertThrows(DictionaryNotFoundException.class, () -> {
            loader.loadDictionary("несуществующий_файл.txt");
        });
    }

    @Test
    @DisplayName("Создание игрового исключения")
    void testWordNotFoundException() {
        WordNotFoundException exception = new WordNotFoundException("Слово не найдено");
        assertEquals("Слово не найдено", exception.getMessage());
    }

    @Test
    @DisplayName("Создание системных исключений")
    void testSystemExceptions() {
        DictionaryNotFoundException ex1 = new DictionaryNotFoundException("Файл не найден");
        assertEquals("Файл не найден", ex1.getMessage());

        DictionaryEmptyException ex2 = new DictionaryEmptyException("Словарь пуст");
        assertEquals("Словарь пуст", ex2.getMessage());

        Exception cause = new Exception("Причина");
        LogFileCreateException ex3 = new LogFileCreateException("Ошибка лога", cause);
        assertEquals("Ошибка лога", ex3.getMessage());
        assertEquals(cause, ex3.getCause());
    }
}