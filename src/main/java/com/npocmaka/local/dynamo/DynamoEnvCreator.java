package com.npocmaka.local.dynamo;

import com.amazonaws.services.dynamodbv2.local.main.ServerRunner;
import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import javax.net.ServerSocketFactory;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.util.Random;

public class DynamoEnvCreator {

    // https://stackoverflow.com/a/76828577/388389
    public static int findAvailableTcpPort() {
        int minPort = 1024;
        int maxPort = 65535;
        int portRange = maxPort - minPort;
        int maxAttempts = 1000;
        int candidatePort;
        int searchCounter = 0;
        Random random = new Random(System.nanoTime());
        do {
            if (searchCounter > maxAttempts) {
                throw new IllegalStateException(String.format(
                        "Could not find an available TCP port in the range [%d, %d] after %d attempts",
                        minPort, maxPort, maxAttempts));
            }
            candidatePort = minPort + random.nextInt(portRange + 1);
            searchCounter++;
        } while (!isPortAvailable(candidatePort));

        return candidatePort;
    }

    private static boolean isPortAvailable(int port) {
        try {
            ServerSocket serverSocket = ServerSocketFactory.getDefault().createServerSocket(port, 1, InetAddress.getByName("localhost"));
            serverSocket.close();
            return true;
        } catch (Exception ex){
            return false;
        }
    }

    static DynamoEnv createDynamoEnv() throws Exception {
        System.setProperty("sqlite4java.library.path", "DynamoDBLocal/native-libs");
        int port = findAvailableTcpPort();
        String uri = "http://localhost:" + port;
        // Create an in-memory and in-process instance of DynamoDB Local that runs over HTTP
        final String[] localArgs = {"-inMemory", "-port", String.valueOf(port)};
        System.out.println("Starting DynamoDB Local...");
        DynamoDBProxyServer server = ServerRunner.createServerFromCommandLineArgs(localArgs);
        server.start();

        //  Create a client and connect to DynamoDB Local
        //  Note: This is a dummy key and secret and AWS_ACCESS_KEY_ID can contain only letters (A–Z, a–z) and numbers (0–9).
        DynamoDbClient ddbClient = DynamoDbClient.builder()
                .endpointOverride(URI.create(uri))
                .httpClient(UrlConnectionHttpClient.builder().build())
                .region(Region.US_WEST_2)
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("dummyKey", "dummySecret")))
                .build();


        return new DynamoEnv(server, ddbClient, uri, port);
    }
}
