package ru.netology;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Main {
    public static void main(String[] args) {
        final Server server = new Server();
        final int port = 9999;

        // создала папку для новых файлов
        File dir = new File("./publicNew");
        if (dir.mkdir()) {
            System.out.println("OK");
        } else if (dir.exists()) {
            System.out.println("exist yet");
        } else {
            System.out.println("dir error");
        }

        server.addHandler("GET", "/messages", (request, responseStream) -> {
            // создаю новый файл
            File file = new File(dir, "/messages.txt");
            if (file.createNewFile()) {
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    String text = "Create message in new file";
                    byte[] bytes = text.getBytes();
                    fos.write(bytes, 0, bytes.length);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("Файл не создан. Скорее всего файл с таким названием уже существует");
            }
            // отправляю этот файл в качестве ответа
            final var filePath = Path.of("./publicNew", "/messages.txt");
            final var mimeType = Files.probeContentType(filePath);

            final var length = Files.size(filePath);
            responseStream.write((
                    "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: " + mimeType + "\r\n" +
                            "Content-Length: " + length + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            Files.copy(filePath, responseStream);
            responseStream.flush();
        });

// ДЗ 2, задача №1, открывается через FormsForRequest.html
        server.addHandler("GET", "/hello", (request, responseStream) -> {
            // создаю новый файл
            File file = new File(dir, "/hello.txt"); //
            if (file.createNewFile()) {
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    String text = "{name}, hello from Ekaterinburg!";
                    byte[] bytes = text.getBytes();
                    fos.write(bytes, 0, bytes.length);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("Файл не создан. Скорее всего файл с таким названием уже существует");
            }
            // отправляю этот файл в качестве ответа
            final var filePath = Path.of("./publicNew", "/hello.txt");
            final var mimeType = Files.probeContentType(filePath);

            final var template = Files.readString(filePath);
            final var content = template.replace(
                    "{name}",
                    request.getQueryParam("name").get(0).getValue() + " " +
                            request.getQueryParam("surname").get(0).getValue()
            ).getBytes();

            responseStream.write((
                    "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: " + mimeType + "\r\n" +
                            "Content-Length: " + content.length + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());

            responseStream.write(content);
            responseStream.flush();
        });

        server.addHandler("GET", "/time", (request, responseStream) -> {
            // создаю новый файл
            File file = new File(dir, "/time.txt"); //
            if (file.createNewFile()) {
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    String text = "Current time {time}";
                    byte[] bytes = text.getBytes();
                    fos.write(bytes, 0, bytes.length);
                    System.out.println("create new file");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("Файл не создан. Скорее всего файл с таким названием уже существует");
            }
            // отправляю с заменой контента
            final var filePath = Path.of("./publicNew", "/time.txt");
            final var mimeType = Files.probeContentType(filePath);

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");

            final var template = Files.readString(filePath);
            final var content = template.replace(
                    "{time}",
                    LocalTime.now().format(dtf)
            ).getBytes();

            responseStream.write((
                    "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: " + mimeType + "\r\n" +
                            "Content-Length: " + content.length + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            responseStream.write(content);
            responseStream.flush();
        });

        server.startServer(port);
    }
}


