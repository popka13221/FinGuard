package com.myname.finguard.common.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class PwnedPasswordCheckerTest {

    @Test
    void isPwnedReturnsFalseWhenDisabledOrBlank() throws Exception {
        HttpClient client = mock(HttpClient.class);
        PwnedPasswordChecker checker = PwnedPasswordChecker.createForTest(false, "https://example.test/range", 100, client);
        assertThat(checker.isPwned(null)).isFalse();
        assertThat(checker.isPwned("")).isFalse();
        assertThat(checker.isPwned("password")).isFalse();
        verify(client, never()).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    void isPwnedReturnsTrueWhenSuffixIsPresent() throws Exception {
        String password = "password";
        String sha1 = sha1Upper(password);
        String prefix = sha1.substring(0, 5);
        String suffix = sha1.substring(5);

        HttpClient client = mock(HttpClient.class);
        @SuppressWarnings("unchecked")
        HttpResponse<String> response = (HttpResponse<String>) mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn(suffix.toLowerCase() + ":42\nDEADBEEF:1\n");
        when(client.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);

        PwnedPasswordChecker checker = PwnedPasswordChecker.createForTest(true, "https://example.test/range", 1000, client);
        assertThat(checker.isPwned(password)).isTrue();

        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(client).send(requestCaptor.capture(), any(HttpResponse.BodyHandler.class));
        assertThat(requestCaptor.getValue().uri().getPath()).isEqualTo("/range/" + prefix);
    }

    @Test
    void isPwnedReturnsFalseWhenSuffixMissingOrNon200() throws Exception {
        HttpClient clientMissing = mock(HttpClient.class);
        @SuppressWarnings("unchecked")
        HttpResponse<String> responseMissing = (HttpResponse<String>) mock(HttpResponse.class);
        when(responseMissing.statusCode()).thenReturn(200);
        when(responseMissing.body()).thenReturn("DEADBEEF:1\n");
        when(clientMissing.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(responseMissing);
        PwnedPasswordChecker checkerMissing = PwnedPasswordChecker.createForTest(true, "https://example.test/range", 1000, clientMissing);
        assertThat(checkerMissing.isPwned("password")).isFalse();

        HttpClient clientNon200 = mock(HttpClient.class);
        @SuppressWarnings("unchecked")
        HttpResponse<String> responseNon200 = (HttpResponse<String>) mock(HttpResponse.class);
        when(responseNon200.statusCode()).thenReturn(500);
        when(responseNon200.body()).thenReturn("ANY:1\n");
        when(clientNon200.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(responseNon200);
        PwnedPasswordChecker checkerNon200 = PwnedPasswordChecker.createForTest(true, "https://example.test/range", 1000, clientNon200);
        assertThat(checkerNon200.isPwned("password")).isFalse();
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
}
