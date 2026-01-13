package com.myname.finguard.common.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class PwnedPasswordCheckerTest {

    @Test
    void isPwnedReturnsFalseWhenDisabledOrBlank() {
        PwnedPasswordChecker checker = new PwnedPasswordChecker(false, "http://localhost:1/range", 100);
        assertThat(checker.isPwned(null)).isFalse();
        assertThat(checker.isPwned("")).isFalse();
        assertThat(checker.isPwned("password")).isFalse();
    }

    @Test
    void isPwnedReturnsTrueWhenSuffixIsPresent() throws Exception {
        String password = "password";
        String sha1 = sha1Upper(password);
        String prefix = sha1.substring(0, 5);
        String suffix = sha1.substring(5);

        AtomicReference<String> requested = new AtomicReference<>("");
        try (SimpleHttpServer server = new SimpleHttpServer(200, (path) -> {
            requested.set(path);
            return (suffix.toLowerCase() + ":42\nDEADBEEF:1\n");
        })) {
            PwnedPasswordChecker checker = new PwnedPasswordChecker(true, "http://127.0.0.1:" + server.port() + "/range", 1000);
            assertThat(checker.isPwned(password)).isTrue();
            assertThat(requested.get()).isEqualTo("/range/" + prefix);
        }
    }

    @Test
    void isPwnedReturnsFalseWhenSuffixMissingOrNon200() throws Exception {
        String password = "password";
        String sha1 = sha1Upper(password);
        String suffix = sha1.substring(5);

        try (SimpleHttpServer serverMissing = new SimpleHttpServer(200, (path) -> "DEADBEEF:1\n")) {
            PwnedPasswordChecker checker = new PwnedPasswordChecker(true, "http://127.0.0.1:" + serverMissing.port() + "/range", 1000);
            assertThat(checker.isPwned(password)).isFalse();
        }

        try (SimpleHttpServer serverNon200 = new SimpleHttpServer(500, (path) -> suffix + ":1\n")) {
            PwnedPasswordChecker checker = new PwnedPasswordChecker(true, "http://127.0.0.1:" + serverNon200.port() + "/range", 1000);
            assertThat(checker.isPwned(password)).isFalse();
        }
    }

    private interface BodySupplier {
        String body(String path);
    }

    private String sha1Upper(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    private static final class SimpleHttpServer implements AutoCloseable {
        private final ServerSocket serverSocket;
        private final Thread thread;
        private final CountDownLatch ready = new CountDownLatch(1);
        private final int status;
        private final BodySupplier bodySupplier;

        SimpleHttpServer(int status, BodySupplier bodySupplier) throws IOException, InterruptedException {
            this.status = status;
            this.bodySupplier = bodySupplier;
            this.serverSocket = new ServerSocket(0, 50, InetAddress.getByName("127.0.0.1"));
            this.thread = new Thread(this::serveOnce, "pwned-test-server");
            this.thread.setDaemon(true);
            this.thread.start();
            ready.await();
        }

        int port() {
            return serverSocket.getLocalPort();
        }

        private void serveOnce() {
            ready.countDown();
            try (Socket socket = serverSocket.accept()) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.US_ASCII));
                String requestLine = reader.readLine();
                String path = "/";
                if (requestLine != null && !requestLine.isBlank()) {
                    String[] parts = requestLine.split(" ");
                    if (parts.length >= 2) {
                        path = parts[1];
                    }
                }
                // Consume headers.
                while (true) {
                    String line = reader.readLine();
                    if (line == null || line.isEmpty()) {
                        break;
                    }
                }

                byte[] body = bodySupplier.body(path).getBytes(StandardCharsets.UTF_8);
                String statusText = status == 200 ? "OK" : "ERROR";
                String headers = "HTTP/1.1 " + status + " " + statusText + "\r\n"
                        + "Content-Type: text/plain; charset=utf-8\r\n"
                        + "Content-Length: " + body.length + "\r\n"
                        + "Connection: close\r\n"
                        + "\r\n";
                OutputStream os = socket.getOutputStream();
                os.write(headers.getBytes(StandardCharsets.US_ASCII));
                os.write(body);
                os.flush();
            } catch (IOException ignored) {
                // Best-effort server for unit tests.
            } finally {
                try {
                    serverSocket.close();
                } catch (IOException ignored) {
                }
            }
        }

        @Override
        public void close() throws Exception {
            try {
                serverSocket.close();
            } catch (IOException ignored) {
            }
            thread.join(1000);
        }
    }
}
