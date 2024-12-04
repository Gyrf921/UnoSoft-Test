package com.oladushek;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Main {

    //private final static String regex = "^\"\\d{11}\"$"; // маска для проверки корректности телефона (с "" в начале и конце)
    private final static String regex = "^\"\\d+\\.\\d\"$";// маска для проверки корректности числа (с "" в начале и конце)

    public static void main(String[] args) {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(args[0])); //args[0]

            List<Set<String>> groups = new ArrayList<>(); // список групп - множеств, где индекс это номер группы
            List<Map<String, Integer>> parts = new ArrayList<>();  // Номер столбика -> {слово -> номер группы}

            String line = reader.readLine();
            while (line != null) {
                String[] cellsInRow = getCellOfRow(line);
                Integer groupNumber = null;

                // parts.size() - максимальное количество столбиков в предыдущих строчках
                // cellsInRow.length количество столбиков в текущей строке
                for (int i = 0; i < Math.min(parts.size(), cellsInRow.length); i++) {
                    //берём все встреченные до этого столбики i и ищем в маппе слово из i- столбика текущей строки
                    Integer groupNumber2 = parts.get(i).get(cellsInRow[i]);

                    if (groupNumber2 != null) {
                        if (groupNumber == null) {
                            groupNumber = groupNumber2;
                        }
                        else if (!Objects.equals(groupNumber, groupNumber2)) {
                            //жёстко объединяем 2 множества
                            for (String line2 : groups.get(groupNumber2)) {
                                groups.get(groupNumber).add(line2);
                                apply(getCellOfRow(line2), groupNumber, parts);
                            }
                            groups.set(groupNumber2, new HashSet<>());
                        }
                    }
                }
                if (groupNumber == null) {
                    if (Arrays.stream(cellsInRow).anyMatch(s -> !s.isEmpty())) {
                        groups.add(new HashSet<>(List.of(line)));
                        apply(cellsInRow, groups.size() - 1, parts);
                    }
                } else {
                    groups.get(groupNumber).add(line);
                    apply(cellsInRow, groupNumber, parts);
                }
                line = reader.readLine();
            }
            reader.close();

            groups.forEach(set -> set.remove(""));
            groups.removeIf(Set::isEmpty);
            groups.sort(Comparator.comparingInt(s -> -s.size()));

            //Вывод необходимый по ТЗ
            System.out.println("Групп размера больше 1: " + groups.stream().filter(s -> s.size() > 1).count());

            int i = 0;
            for (Set<String> group : groups) {
                i++;
                System.out.println("\nГруппа " + i);
                for (String val : group) {
                    System.out.println(val);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String[] getCellOfRow(String line) {
        String[] row = line.split(";");
        for (int i = 0; i < row.length; i++) {
            if (!row[i].matches(regex)){
                if (row[i].equals("\"\"")){
                    row[i] = row[i].replaceAll("\"", "");
                    continue;
                }
                return new String[0];
            }
            row[i] = row[i].replaceAll("\"", "");
        }
        return row;
    }

    private static void apply(String[] newValues, int groupNumber, List<Map<String, Integer>> parts) {
        for (int i = 0; i < newValues.length; i++) {

            if (i < parts.size()) {
                if (newValues[i].isEmpty()) {
                    parts.get(i).put("IS_EMPTY", groupNumber);
                    continue;
                }
                parts.get(i).put(newValues[i], groupNumber);
            } //выглядит страшненько, но работает
            else {
                if (newValues[i].isEmpty()) {
                    HashMap<String, Integer> map = new HashMap<>();
                    map.put("IS_EMPTY", groupNumber);
                    parts.add(map);
                    continue;
                }
                HashMap<String, Integer> map = new HashMap<>();
                map.put(newValues[i], groupNumber);
                parts.add(map);
            }
        }
    }
}