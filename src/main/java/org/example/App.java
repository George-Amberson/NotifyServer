package org.example;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import notify.server.server;

import java.io.IOException;

public class App
{
    public static void main( String[] args )  throws Exception
    {
        Server service = ServerBuilder.forPort(8080)
                .addService(new server())
                .build();

        // Start the server
        service.start();

        // Server threads are running in the background.
        System.out.println("Server started");
        // Don't exit the main thread. Wait until server is terminated.
        service.awaitTermination();
    }
}
