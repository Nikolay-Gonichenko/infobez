package ru.itmo.infobezLab1;

import ru.itmo.infobezLab1.exception.EmptyInputFileException;
import ru.itmo.infobezLab1.exception.IncorrectInputException;
import ru.itmo.infobezLab1.exception.WrongAlphabetException;

import java.io.*;
import java.util.*;

public class Main {

    /**
     * Алфавит
     * Можно менять, но тогда надо не забыть поменять ROW_NUMBER
     * в общем и целом, количество символов в ALPHABET.length / ROW_NUMBER == integer
     */
    private static final Character[] ALPHABET = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o',
    'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', ' ', '.', '?', '!'};

    /**
     * Количество строк в таблице
     */
    private static final int ROW_NUMBER = 5;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Print the type of work you want: 1 is encryption, 2 is decryption");
        //режим работы, где 1 это кодирование, 2 - декодирование
        int workType = scanner.nextInt();
        try {
            if (workType == 1) {
                encryption(scanner);
            } else if (workType == 2) {
                decryption(scanner);
            } else {
                System.out.println("Try again. Only 1 or 2");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    /**
     * Метод дешифрования
     * @param scanner сканер для ввода пользователя
     */
    private static void decryption(Scanner scanner) throws WrongAlphabetException, IncorrectInputException, EmptyInputFileException {
        if (!checkAlphabet())
            throw new WrongAlphabetException("Проверьте алфавит");

        //считывание таблиц с консоли
        char[][] tableLeft = readTable(scanner);
        char[][] tableRight = readTable(scanner);

        //получаем строку с output.txt
        StringBuilder line = null;
        try (BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/output.txt"))){
            line = new StringBuilder(reader.readLine());

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (line == null)
            throw new EmptyInputFileException("Исходный файл пустой");
        //исходная строка
        StringBuilder outputLine = new StringBuilder();

        //читаем строку по два символа
        long first = System.currentTimeMillis();
        for (int i = 0; i < line.length(); i += 2) {
            //получаем координаты вершин прямоугольника
            char firstSymbol = line.charAt(i);
            char secondSymbol = line.charAt(i+1);
            SymbolInfo firstSymbolInfo = findSymbol(firstSymbol, tableRight);
            SymbolInfo secondSymbolInfo = findSymbol(secondSymbol, tableLeft);

            //кодируем символы
            //отдельно рассматриваем случай, когда символы в таблице оказались на одной строке
            char encryptedFirstSymbol;
            char encryptedSecondSymbol;
            if (firstSymbolInfo.rowNumber() == secondSymbolInfo.rowNumber()) {
                //берём символы из этой же строки, но из противоположных таблиц с противоположных столбцов
                encryptedFirstSymbol = tableLeft[firstSymbolInfo.rowNumber()][secondSymbolInfo.columnNumber()];
                encryptedSecondSymbol = tableRight[firstSymbolInfo.rowNumber()][firstSymbolInfo.columnNumber()];
            } else {
                //строим квадрат и берём другие вершины
                encryptedFirstSymbol = tableLeft[firstSymbolInfo.rowNumber()][secondSymbolInfo.columnNumber()];
                encryptedSecondSymbol = tableRight[secondSymbolInfo.rowNumber()][firstSymbolInfo.columnNumber()];
            }

            //добавляем символы к зашифровонной строке
            outputLine.append(encryptedFirstSymbol).append(encryptedSecondSymbol);
        }

        //вывод исходной строки
        System.out.println(System.currentTimeMillis() + " " +  first);
        System.out.println(outputLine);
    }

    /**
     * Читаем таблицу с консоли
     * @param scanner
     * @return
     */
    private static char[][] readTable(Scanner scanner) {
        char[][] table = new char[ROW_NUMBER][ALPHABET.length / ROW_NUMBER];
        scanner = new Scanner(System.in);
        System.out.println("Print table");
        for (int i = 0; i < ROW_NUMBER; i++) {
            char[] line = scanner.nextLine().toCharArray();
            int k = 0;
            for (int j = 0; j < ALPHABET.length / ROW_NUMBER; j++) {
                table[i][j] = line[k];
                k+=2;
            }
        }
        return table;
    }

    /**
     * Метод шифрования
     * @param scanner сканер для ввода пользователя
     */
    private static void encryption(Scanner scanner) throws WrongAlphabetException, EmptyInputFileException, IncorrectInputException {
        if (!checkAlphabet())
            throw new WrongAlphabetException("Проверьте алфавит");

        //получение таблиц
        char[][] tableLeft = generateTable();
        char[][] tableRight = generateTable();
        printTables(tableLeft, tableRight);
        saveTables(tableLeft, tableRight);

        //читаем строку в файле
        StringBuilder line = null;
        try (BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/input.txt"))){
            line = new StringBuilder(reader.readLine());
            //если кол-во символов нечётное, то добавляем в конец пробел
            if (line.length() % 2 != 0)
                line.append(' ');
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (line == null)
            throw new EmptyInputFileException("Исходный файл пустой");

        //зашифрованная строка
        StringBuilder outputLine = new StringBuilder();

        //читаем строку по два символа
        for (int i = 0; i < line.length(); i += 2) {
            //получаем координаты вершин прямоугольника
            char firstSymbol = line.charAt(i);
            char secondSymbol = line.charAt(i+1);
            SymbolInfo firstSymbolInfo = findSymbol(firstSymbol, tableLeft);
            SymbolInfo secondSymbolInfo = findSymbol(secondSymbol, tableRight);

            //кодируем символы
            //отдельно рассматриваем случай, когда символы в таблице оказались на одной строке
            char encryptedFirstSymbol;
            char encryptedSecondSymbol;
            if (firstSymbolInfo.rowNumber() == secondSymbolInfo.rowNumber()) {
                //берём символы из этой же строки, но из противоположных таблиц с противоположных столбцов
                encryptedFirstSymbol = tableRight[firstSymbolInfo.rowNumber()][secondSymbolInfo.columnNumber()];
                encryptedSecondSymbol = tableLeft[firstSymbolInfo.rowNumber()][firstSymbolInfo.columnNumber()];
            } else {
                //строим квадрат и берём другие вершины
                encryptedFirstSymbol = tableRight[firstSymbolInfo.rowNumber()][secondSymbolInfo.columnNumber()];
                encryptedSecondSymbol = tableLeft[secondSymbolInfo.rowNumber()][firstSymbolInfo.columnNumber()];
            }

            //добавляем символы к зашифровонной строке
            outputLine.append(encryptedFirstSymbol).append(encryptedSecondSymbol);
        }

        //записываем зашифровонную строку в файл
        System.out.println(outputLine);
        try (FileWriter fileWriter = new FileWriter("src/main/resources/output.txt")) {
            fileWriter.write(outputLine.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Находит символ в таблице и возвращает всю информацию о нём
     * @param symbol
     * @param table
     * @return
     */
    private static SymbolInfo findSymbol(char symbol, char[][] table) throws IncorrectInputException {
        for (int i = 0; i < ROW_NUMBER; i++) {
            for (int j = 0; j < ALPHABET.length / ROW_NUMBER; j++) {
                if (table[i][j] == symbol)
                    return new SymbolInfo(symbol, i, j);
            }
        }
        throw new IncorrectInputException("В исходном файле есть недопустимые символы");
    }

    /**
     * Печатаем таблицы для пользователя
     * @param tableLeft
     * @param tableRight
     */
    private static void printTables(char[][] tableLeft, char[][] tableRight) {
        System.out.print("Left table: \n");
        printTable(tableLeft);
        System.out.print("\n");
        System.out.print("Right table: \n");
        printTable(tableRight);
    }

    /**
     * Печатает одну таблицу
     * @param table
     */
    private static void printTable(char[][] table) {
        for (int i = 0; i < ROW_NUMBER; i++) {
            for (int j = 0; j < ALPHABET.length / ROW_NUMBER; j++) {
                System.out.print(table[i][j] + " ");
            }
            System.out.print("\n");
        }
        System.out.println();
    }

    /**
     * Сохраняем таблицы в файл, чтобы потом не потерять
     * @param tableLeft
     * @param tableRight
     */
    private static void saveTables(char[][] tableLeft, char[][] tableRight) {
        try (FileWriter fileWriter = new FileWriter("src/main/resources/tables.txt")) {
            fileWriter.write("Left              Right\n");
            for (int i = 0; i < ROW_NUMBER; i++) {
                for (int j = 0; j < ALPHABET.length / ROW_NUMBER; j++) {
                    fileWriter.write(tableLeft[i][j] + " ");
                }
                fileWriter.write("      ");
                for (int j = 0; j < ALPHABET.length / ROW_NUMBER; j++) {
                    fileWriter.write(tableRight[i][j] + " ");
                }
                fileWriter.write("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Проверка алфавита
     * @return true если можно дальше работать
     */
    private static boolean checkAlphabet() {
        int number = ALPHABET.length / ROW_NUMBER;
        return number * ROW_NUMBER == ALPHABET.length;
    }

    /**
     * Генерация таблицы на основе алфавита
     * @return
     */
    private static char[][] generateTable() {
        //перемешали алфавит
        List<Character> alphabet = new ArrayList<>(Arrays.asList(ALPHABET));
        //Collections.shuffle(alphabet);

        //сделали таблицу
        char[][] table = new char[ROW_NUMBER][ALPHABET.length / ROW_NUMBER];
        for (int i = 0; i < ROW_NUMBER; i++) {
            for (int j = 0; j < ALPHABET.length / ROW_NUMBER; j++) {
                table[i][j] = alphabet.get(0);
                alphabet.remove(0);
            }
        }
        return table;
    }
}
