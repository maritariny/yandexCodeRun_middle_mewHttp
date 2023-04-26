package ru.maritariny;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class Main {

    private static final String MY_URL = "http://127.0.0.1:7777";

    public static void main(String[] args) throws IOException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String[] variables = new String[4];
        for (int i = 0; i < variables.length; i++) {
            variables[i] = reader.readLine();
        }

        allowMethods("MEW");

        URL yahoo = new URL(MY_URL);
        List<List> listOfValues = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            int count = 3;
            String variable = "";

            switch (i) {
                case 2:
                    variable = variables[i];
                    count = 1;
                    break;
                default:
                    variable = variables[i] + "," + variables[i + 1] + "," + variables[i + 2];
            }
            HttpURLConnection con = (HttpURLConnection) yahoo.openConnection();
            con.setRequestMethod("MEW");
            con.setRequestProperty("X-cat-VAriable", variable);
            int responseCode = con.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) { // success
                Map<String, List<String>> headers = con.getHeaderFields();
                List<String> headerValues = null;
                for (String key : headers.keySet()) {
                    if (key == null) {
                        continue;
                    }
                    if (key.toUpperCase(Locale.ROOT).equals("X-CAT-VALUE")) {
                        headerValues = headers.get(key);
                        break;
                    }
                }
                List b = new ArrayList(count);
                for (int j = 0; j < headerValues.size(); j++) {
                    b.add(headerValues.get(j));
                }
                listOfValues.add(b);
            }
        }
        String[] values = new String[4];
        List<String> first = new ArrayList<String>(listOfValues.get(0)); // 1 2 3
        List<String> second = new ArrayList<String>(listOfValues.get(1)); // 2 3 4
        List<String> third = listOfValues.get(2);

        values[2] = third.get(0);
        List<String> pair = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (first.get(i).equals(second.get(j))) {
                    pair.add(first.get(i));
                    first.set(i, "");
                    second.set(j, "");
                    break;
                }
            }
            if (pair.size() == 2) {
                break;
            }
        }

        first = new ArrayList<String>(listOfValues.get(0));
        removeDuplicates(first, pair);
        values[0] = first.get(0);

        second = new ArrayList<String>(listOfValues.get(1));
        removeDuplicates(second, pair);
        values[3] = second.get(0);

        ArrayList<String> a = new ArrayList<String>(1);
        a.add(values[2]);
        removeDuplicates(pair, a);
        values[1] = pair.get(0);

        for (String v : values) {
            System.out.println(v);
        }
        reader.close();
    }

    private static void removeDuplicates(List<String> first, List<String> second) {
        for (int i = 0; i < second.size(); i++) {
            if (first.contains(second.get(i))) {
                first.remove(second.get(i));
            }
        }
    }
    private static void allowMethods(String meth) {
        try {
            Field methodsField = HttpURLConnection.class.getDeclaredField("methods");

            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(methodsField, methodsField.getModifiers() & ~Modifier.FINAL);

            methodsField.setAccessible(true);

            String[] oldMethods = (String[]) methodsField.get(null);
            Set<String> methodsSet = new LinkedHashSet<>(Arrays.asList(oldMethods));
            methodsSet.addAll(Arrays.asList(meth));
            String[] newMethods = methodsSet.toArray(new String[0]);
            methodsField.set(null, newMethods);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}

