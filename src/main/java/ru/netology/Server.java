package ru.netology;

import org.apache.http.client.utils.URLEncodedUtils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Server {
    //final List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");
    ConcurrentHashMap<String, Handler> handlers = new ConcurrentHashMap<>();
    final ExecutorService threadPool = Executors.newFixedThreadPool(64);


    public void startServer(int port) {

        try (final var serverSocket = new ServerSocket(port)) {
            while (true) {
                try {                                         // убрала трай с ресурсами
                    final var socket = serverSocket.accept();

                    threadPool.submit(() -> {
                        try {
                            processingTheConnection(socket);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                } catch (IOException e) {
                    throw new IOException();
                } finally {  // нужен ли этот блок
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void processingTheConnection(Socket socket) throws IOException {
        try (final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             final var out = new BufferedOutputStream(socket.getOutputStream())) {

            Request request = new Request(socket);

    // парсинг реквест-лайн

            final var requestLine = in.readLine();
            final var parts = requestLine.split(" ");

            if (parts.length != 3) {
                return;
            }

            final var methodRequest = parts[0]; // метод
            request.setMethod(methodRequest);

            final var fullPath = parts[1]; // путь
            if(!fullPath.startsWith("/")){
                return;
            }

            final var requestURL = URLEncodedUtils.parse(fullPath, StandardCharsets.UTF_8, '?', '&');

            request.setPath(requestURL.get(0).toString()); // главный путь

            requestURL.remove(0);
            request.setQueryParams(requestURL);

//            final var httpVersion = parts[2]; // версия http для полноты картины?
//            request.setHttpVersion(httpVersion);

    // поиск хендлера
            if(handlers.containsKey(request.getMethod() + request.getPath())) {
                Handler handler = handlers.get(request.getMethod() + request.getPath());
                handler.handle(request, new BufferedOutputStream(request.getSocket().getOutputStream()));
            } else {
                out.write((
                        "HTTP/1.1 404 Not Found\r\n" +
                                "Content-Length: 0\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                out.flush();
                return;
            }

    // парсим заголовки и тело запроса
            StringBuilder otherHeadersSb = new StringBuilder();

            while (in.readLine() != null) {
                otherHeadersSb.append(in.readLine());
                otherHeadersSb.append("\n");
            }
            String otherHeaders = otherHeadersSb.toString();

            if (otherHeaders.contains("\r\n\r\n") && !otherHeaders.endsWith("\r\n\r\n")) {
                final var partsHeadersAndBody = otherHeaders.split("\r\n\r\n");
                final var headers = partsHeadersAndBody[0];
                final var body = partsHeadersAndBody[1];
                request.setHeaders(headers);
                request.setBody(body);
            } else {
                request.setHeaders(otherHeaders);
                request.setBody(null);
            }
        } catch (
                IOException e) {
            e.printStackTrace();
        }
    }

    public void addHandler(String method, String path, Handler handler) {
        handlers.put(method+path, handler);
    }
}
