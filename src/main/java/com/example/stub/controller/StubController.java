package com.example.stub.controller;

import com.example.stub.service.StubService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@RestController
public class StubController {

    private static final Logger log = LoggerFactory.getLogger(StubController.class);
    private final StubService stubService;

    public StubController(StubService stubService) {
        this.stubService = stubService;
    }

    @RequestMapping("/**")
    public ResponseEntity<byte[]> handleAll(HttpServletRequest request) throws IOException {
        byte[] body = readRequestBody(request);
        log.debug("Received request: {} {}", request.getMethod(), request.getRequestURI());
        return stubService.handleRequest(request, body);
    }

    private byte[] readRequestBody(HttpServletRequest request) throws IOException {
        try (InputStream is = request.getInputStream()) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[8192];
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            return buffer.toByteArray();
        }
    }
}