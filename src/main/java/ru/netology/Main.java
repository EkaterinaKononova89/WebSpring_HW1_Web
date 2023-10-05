package ru.netology;

public class Main {
    public static void main(String[] args) {
        final Server server = new Server();
        final int port = 9999;

        server.startServer(port);
    }
}


