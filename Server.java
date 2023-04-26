package ru.maritariny;

import com.sun.net.httpserver.*;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.Executor;

public class Server {

    final String inputFile;
    final String answerFile;
    Map<String, String> variables;
    int requests;

    public Server(String inputFile, String answerFile) {
        this.inputFile = inputFile;
        this.answerFile = answerFile;
    }

    private List<String> readFile(String fileName) throws IOException {
        List<String> result = new ArrayList<>();
        try (BufferedReader in = new BufferedReader(new FileReader(fileName))) {
            while (true) {
                String word = readWord(in);
                if (word.isEmpty()) {
                    break;
                } else {
                    result.add(word);
                }
            }
        }
        return result;
    }

    private void readVariables() throws IOException {
        List<String> varNames = readFile(inputFile);
        if (varNames.size() != 4) {
            System.err.println("Expected 4 variable names");
            System.exit(1);
        }
        List<String> varValues = readFile(answerFile);
        if (varValues.size() != 4) {
            System.err.println("Expected 4 variable values");
            System.exit(1);
        }
        variables = new HashMap<>();
        for (int i = 0; i < varNames.size(); i++) {
            variables.put(varNames.get(i), varValues.get(i));
        }
    }

    public void run() throws Exception {
        readVariables();
        HttpServer server = HttpServer.create();
        server.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), 7777), 0);
        requests = 0;
        server.createContext("/", new HttpHandler() {

            void parse(String header, List<String> variables) {
                for (String value : header.split(",",-1)) {
                    variables.add(value.strip());
                }
            }
            
            @Override
            public void handle(HttpExchange request) throws IOException {
                if (!"MEW".equals(request.getRequestMethod())) {
                    System.out.println("Invalid method");
                    request.sendResponseHeaders(404, 0);
                    return;
                }
                Headers headers = request.getRequestHeaders();
                List<String> headerValues = headers.get("X-Cat-Variable");
                if (headerValues == null) {
                    headerValues = new ArrayList<>();
                }
                List<String> variableNames = new ArrayList<>();
                for (String headerValue : headerValues) {
                    parse(headerValue, variableNames);
                }
                Set<String> variablesSet = new HashSet<>(variableNames);
                if (variablesSet.size() != variableNames.size()) {
                    System.out.println("Repeating variables");
                    request.sendResponseHeaders(404, 0);
                    return;
                }
                List<String> variableValues = new ArrayList<>();
                for (String variable : variableNames) {
                    if (variables.containsKey(variable)) {
                        variableValues.add(variables.get(variable));
                    } else {
                        System.out.println("Variable " + variable + " is not known");
                        request.sendResponseHeaders(404, 0);
                        return;
                    }
                }
                if (requests >= 3) {
                    System.out.println("Too many requests");
                    request.sendResponseHeaders(404, 0);
                    return;
                }
                requests++;
                variableValues.sort(String::compareTo);
                System.out.println("Request: " + requests);
                System.out.println("Variables: " + String.join(" ", variableNames));
                System.out.println("Values: " + String.join(" ", variableValues));
                for (String value : variableValues) {
                    request.getResponseHeaders().add("X-Cat-Value", value);
                }
                request.sendResponseHeaders(200, 0);
            }
        });
        server.setExecutor(null);
        server.start();
    }

    public String readWord(BufferedReader in) throws IOException {
        StringBuilder b = new StringBuilder();
        int c;
        c = in.read();
        while (c >= 0 && c <= ' ')
            c = in.read();
        if (c < 0)
            return "";
        while (c > ' ') {
            b.append((char) c);
            c = in.read();
        }
        return b.toString();
    }

    private static void printUsage() {
        System.out.println("usage: java -jar server.jar -i INPUT -a ANSWER");
    }

    public static void main(String[] args) {
        String inputFile = null;
        String answerFile = null;
        if (args.length == 1) {
            printUsage();
            return;
        }
        for (int i = 0; i < args.length; i++) {
            if (i + 1 < args.length) {
                if ("-i".equals(args[i]) || "--input".equals(args[i])) {
                    inputFile = args[i + 1];
                    i++;
                    continue;
                }
                if ("-a".equals(args[i]) || "--answer".equals(args[i])) {
                    answerFile = args[i + 1];
                    i++;
                    continue;
                }
            }
            printUsage();
            System.out.println("unknown option: " + args[i]);
            return;
        }
        if (inputFile == null) {
            printUsage();
            System.out.println("missing parameter -i/--input");
            return;
        }
        if (answerFile == null) {
            printUsage();
            System.out.println("missing parameter -a/--answer");
            return;
        }
        try {
            new Server(inputFile, answerFile).run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
