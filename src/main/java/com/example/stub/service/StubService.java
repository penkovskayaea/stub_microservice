package com.example.stub.service;

import com.example.stub.config.Rule;
import com.example.stub.config.StubConfig;
import com.example.stub.util.RandomDataGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Service
public class StubService {

    private static final Logger log = LoggerFactory.getLogger(StubService.class);

    private final StubConfig stubConfig;
    private final ProxyService proxyService;
    private final PathMatcher pathMatcher = new AntPathMatcher();

    public StubService(StubConfig stubConfig, ProxyService proxyService) {
        this.stubConfig = stubConfig;
        this.proxyService = proxyService;
    }

    public ResponseEntity<byte[]> handleRequest(HttpServletRequest request, byte[] body) throws IOException {
        String path = request.getRequestURI();
        String method = request.getMethod();

        Rule rule = findRule(path, method);
        if (rule == null) {
            log.warn("No rule found for path={}, method={}", path, method);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No rule matched".getBytes());
        }

        // Задержка
        if (rule.getDelay() != null && rule.getDelay() > 0) {
            try {
                Thread.sleep(rule.getDelay());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Прокси
        if (rule.getProxy() != null) {
            ResponseEntity<byte[]> proxyResponse = proxyService.proxyRequest(rule.getProxy(), request, body);
            if (rule.getResponse() != null && rule.getResponse().getStatus() != null) {
                return ResponseEntity.status(rule.getResponse().getStatus())
                        .headers(proxyResponse.getHeaders())
                        .body(proxyResponse.getBody());
            } else {
                return proxyResponse;
            }
        }

        // Статический ответ
        if (rule.getResponse() == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Rule has no response configuration".getBytes());
        }

        byte[] responseBody;
        HttpStatus status = HttpStatus.valueOf(rule.getResponse().getStatus() != null ?
                rule.getResponse().getStatus() : 200);

        if (rule.getResponse().getRandomSize() != null && rule.getResponse().getRandomSize() > 0) {
            int size = rule.getResponse().getRandomSize();
            log.debug("Generating random body of size {} bytes", size);
            responseBody = RandomDataGenerator.generateRandomBytes(size);
        } else if (rule.getResponse().getBody() != null) {
            responseBody = rule.getResponse().getBody().getBytes();
        } else {
            responseBody = new byte[0];
        }

        return ResponseEntity.status(status).body(responseBody);
    }

    private Rule findRule(String path, String method) {
        return stubConfig.getRules().stream()
                .filter(rule -> method.equalsIgnoreCase(rule.getMethod()) &&
                        pathMatcher.match(rule.getPath(), path))
                .findFirst()
                .orElse(null);
    }
}