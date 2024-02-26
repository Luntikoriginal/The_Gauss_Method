package gauss;

import gauss.domain.Fraction;
import gauss.exceptions.BasesFormatException;
import gauss.exceptions.EmptyFileException;
import gauss.utils.FractionUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        try {
            List<String> data = readFile("example.txt");
            printFile(data);

            String[] sizes = data.get(0).split("\\s*x\\s*");
            if (sizes.length != 2) {
                throw new EmptyFileException("Недостаточно или избыток данных о размерности матрицы!");
            }
            int rows = Integer.parseInt(sizes[0]);
            int columns = Integer.parseInt(sizes[1]);

            ArrayList<String> coefficients = new ArrayList<>();
            for (int i = 1; i <= rows; i++) {
                coefficients.add(data.get(i));
            }
            Fraction[][] matrix = createMatrix(coefficients, columns);

            String[] basesStr = data.get(rows + 1).split("\\s*,\\s*");
            ArrayList<Integer> bases = new ArrayList<>();
            for (String s : basesStr) {
                bases.add(Integer.parseInt(s));
                if (bases.get(bases.size() - 1) > columns) {
                    throw new BasesFormatException("Номер базисной переменной больше кол-ва столбцов!");
                }
            }
            if (bases.size() != rows) {
                throw new EmptyFileException("Кол-во базисных переменных не равно кол-ву строк матрицы!");
            }

            System.out.println("\nПреобразование");
            gauss(matrix, bases);

            printResult(matrix, bases);

        } catch (IOException e) {
            System.err.println("Ошибка чтения файла: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("Ошибка при считывании чисел: " + e.getMessage());
        } catch (BasesFormatException e) {
            System.err.println("Ошибка при считывании базисных переменных: " + e.getMessage());
        } catch (EmptyFileException e) {
            System.err.println("Недостаточно данных: " + e.getMessage());
        }
    }

    /**
     * Метод для чтения файла.
     *
     * @param filePath путь к файлу
     * @return массив строк из файла
     * @throws IOException выбрасывается при возникновении ошибок при чтении файла
     */
    public static List<String> readFile(String filePath) throws IOException {
        return Files.readAllLines(Path.of(filePath), StandardCharsets.UTF_8);
    }

    /**
     * Метод для печати считанного из файла.
     *
     * @param lines строки файла
     */
    public static void printFile(List<String> lines) throws EmptyFileException {
        System.out.println("\nСчитано из файла: ");
        if (lines.isEmpty()) {
            throw new EmptyFileException("Файл пуст!");
        } else {
            for (String line : lines) {
                System.out.println(line);
            }
        }
    }

    /**
     * Метод для создания матрицы с коэффициентами уравнений.
     *
     * @param coefficients строки с коэффициентами
     * @return заполненную матрицу
     * @throws NumberFormatException возникает при ошибочно введенном числе
     */
    public static Fraction[][] createMatrix(List<String> coefficients, int columns) throws NumberFormatException, EmptyFileException {
        Fraction[][] matrix = new Fraction[coefficients.size()][];
        for (int i = 0; i < coefficients.size(); i++) {
            String[] values = coefficients.get(i).split("\\s+");
            if (values.length != columns) {
                throw new EmptyFileException("Недостаточно или избыток коэффициентов!");
            }
            matrix[i] = new Fraction[values.length];
            for (int j = 0; j < values.length; j++) {
                String[] fractionParts = values[j].split("/");
                if (fractionParts.length == 1) {
                    matrix[i][j] = new Fraction(Integer.parseInt(fractionParts[0]), 1);
                } else {
                    matrix[i][j] = new Fraction(Integer.parseInt(fractionParts[0]), Integer.parseInt(fractionParts[1]));
                    matrix[i][j].reduction();
                }
            }
        }
        return matrix;
    }

    /**
     * Печатает матрицу.
     *
     * @param matrix матрица для печати
     */
    public static void printMatrix(Fraction[][] matrix) {
        System.out.println();
        for (Fraction[] fractions : matrix) {
            System.out.println(Arrays.toString(fractions));
        }
    }

    /**
     * Метод преобразования минора к диагональному виду методом Гаусса.
     * По очереди берет базисный элемент, делит на него строку и вычитает
     * получившуюся строку из всех других необходимое кол-во раз.
     *
     * @param matrix матрица, в которой выполняются преобразования
     * @param bases  массив с базисными переменными
     */
    public static void gauss(Fraction[][] matrix, ArrayList<Integer> bases) {
        int rows = matrix.length;
        int cols = matrix[0].length;
        printMatrix(matrix);

        for (int i = 0; i < rows; i++) {
            int baseIndex = bases.get(i) - 1;
            Fraction pivot = matrix[i][baseIndex];

            for (int j = 0; j < cols; j++) {
                if (pivot.getNumerator() != 0) {
                    matrix[i][j] = FractionUtils.division(matrix[i][j], pivot);
                    matrix[i][j].reduction();
                }
            }
            printMatrix(matrix);

            for (int k = 0; k < rows; k++) {
                if (k != i) {
                    Fraction factor = matrix[k][baseIndex];
                    for (int j = 0; j < cols; j++) {
                        matrix[k][j] = FractionUtils.difference(matrix[k][j], FractionUtils.multiplication(factor, matrix[i][j]));
                        matrix[k][j].reduction();
                    }
                }
            }
            printMatrix(matrix);
        }
    }

    /**
     * Печатает результат программы, выражая базисные переменные.
     *
     * @param matrix полученная в ходе программы матрица
     * @param bases  массив базисных переменных
     */
    public static void printResult(Fraction[][] matrix, ArrayList<Integer> bases) {
        System.out.println("\nОтвет: ");
        ArrayList<Integer> basesColumns = new ArrayList<>();
        for (int b : bases) {
            basesColumns.add(b - 1);
        }
        for (int i = 0; i < bases.size(); i++) {
            Fraction constant = matrix[i][matrix[i].length - 1];
            StringBuilder builder = new StringBuilder("X" + bases.get(i) + " = ");

            for (int j = 0; j < matrix[i].length - 1; j++) {
                if (!basesColumns.contains(j)) {
                    Fraction coefficient = matrix[i][j];
                    if (coefficient.getNumerator() < 0) {
                        builder.append("+ ").append(coefficient.abs()).append("X").append(j + 1).append(" ");
                    } else {
                        builder.append("- ").append(coefficient.abs()).append("X").append(j + 1).append(" ");
                    }
                }
            }
            if (constant.getNumerator() > 0) {
                builder.append("+ ").append(constant.abs());
            } else {
                builder.append("- ").append(constant.abs());
            }

            System.out.println(builder);
        }
    }
}
