package org.example;

import org.example.proxy.HttpServer;

public class Startup {
    public static void main(String[] args) {
        new HttpServer(8989).start();
    }
}
