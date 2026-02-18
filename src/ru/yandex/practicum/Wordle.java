package ru.yandex.practicum;

import java.io.*;
import java.util.Scanner;

public class Wordle {

    public static void main(String[] args) {
        try (PrintWriter log = new PrintWriter("wordle.log", "UTF-8")) {

            log.println("=== ЗАПУСК ИГРЫ ===");

            WordleDictionaryLoader loader = new WordleDictionaryLoader(log);
            WordleDictionary dictionary = loader.loadDictionary("words_ru.txt");

            WordleGame game = new WordleGame(dictionary, log);

            System.out.println("   ДОБРО ПОЖАЛОВАТЬ В WORDLE!   ");
            System.out.println("Я загадала слово из 5 букв.");
            System.out.println("У тебя 6 попыток, чтобы угадать.");
            System.out.println("Подсказки: + буква на месте, ^ буква есть, - буквы нет");
            System.out.println("Если нужна подсказка — нажми Enter");
            System.out.println();

            playGame(game, log);

        } catch (DictionaryNotFoundException e) {
            System.err.println("Файл словаря не найден!");
            System.err.println(e.getMessage());
        } catch (DictionaryEmptyException e) {
            System.err.println("В словаре нет подходящих слов из 5 букв!");
        } catch (IOException e) {
            System.err.println("Ошибка ввода-вывода: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Неожиданная ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void playGame(WordleGame game, PrintWriter log) {
        Scanner scanner = new Scanner(System.in);

        while (!game.isGameOver()) {
            System.out.println("Осталось попыток: " + game.getAttemptsLeft());
            System.out.print("Твой вариант: ");

            String input = scanner.nextLine().trim().toLowerCase();

            if (input.isEmpty()) {
                String hint = game.getHint();
                System.out.println("Подсказка: попробуй слово \"" + hint + "\"");
                log.println("Игрок запросил подсказку, предложено: " + hint);
                continue;
            }

            if (input.length() != 5) {
                System.out.println("Слово должно быть из 5 букв!");
                log.println("Игрок ввёл слово не из 5 букв: " + input);
                continue;
            }

            if (!input.matches("[а-я]+")) {
                System.out.println("Используй только русские буквы!");
                log.println("Игрок ввёл недопустимые символы: " + input);
                continue;
            }

            try {
                String result = game.makeGuess(input);
                System.out.println("Результат: " + result);
                System.out.println();

                if (game.isWin()) {
                    System.out.println("Ты угадал слово!");
                    log.println("Игрок победил! Слово: " + input);
                }

            } catch (WordNotFoundException e) {
                System.out.println("Такого слова нет в словаре. Попробуй другое.");
                log.println("Игрок ввёл слово не из словаря: " + input);
            }
        }

        if (!game.isWin()) {
            System.out.println("К сожалению, попытки закончились.");
            System.out.println("Загаданное слово было: " + game.getAnswer());
            log.println("Игрок проиграл. Попытки исчерпаны.");
        }
    }
}