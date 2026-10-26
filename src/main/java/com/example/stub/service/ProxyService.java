package com.example.stub.service;

import com.example.stub.config.ProxyConfig;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.util.Enumeration;

@Service
public class ProxyService {

    private final RestTemplate restTemplate = new RestTemplate();

    public ResponseEntity<byte[]> proxyRequest(ProxyConfig proxyConfig,
                                               HttpServletRequest request,
                                               byte[] body) throws IOException {
        String targetUrl = proxyConfig.getUrl();
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(targetUrl);
        request.getParameterMap().forEach((key, values) -> {
            for (String value : values) {
                builder.queryParam(key, value);
            }
        });
        URI uri = builder.build().toUri();

        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            if ("host".equalsIgnoreCase(headerName) || "content-length".equalsIgnoreCase(headerName)) {
                continue;
            }
            Enumeration<String> headerValues = request.getHeaders(headerName);
            while (headerValues.hasMoreElements()) {
                headers.add(headerName, headerValues.nextElement());
            }
        }
        if (body != null && body.length > 0 && !headers.containsKey(HttpHeaders.CONTENT_TYPE)) {
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        }

        HttpEntity<byte[]> entity = new HttpEntity<>(body, headers);
        HttpMethod method = HttpMethod.valueOf(request.getMethod());

        ResponseEntity<byte[]> response = restTemplate.exchange(uri, method, entity, byte[].class);
        return response;
    }
}