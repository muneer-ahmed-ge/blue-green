package com.example.demo;


import static org.springframework.http.HttpMethod.GET;

import java.util.List;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class SyncTest {

    public static final String HOST = "https://bgd.servicemax-api.com/bgdemo";

    public static void main(String[] args) throws Exception {
        while (true) {
            runSync();
            Thread.sleep(5 * 1000);
            System.out.println("\n\n\n");
        }
    }

    private static void runSync() throws Exception {
        RestTemplate template = new RestTemplate();
        ResponseEntity<String> response =
                template.postForEntity(HOST + "/sync", "", String.class);
        assert response.getStatusCodeValue() == 200;
        System.out.println(response.getBody());
        List<String> cookies = response.getHeaders().get("Set-Cookie");
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", cookies.get(0));
        headers.add("Cookie", cookies.get(1));
        boolean flag = true;
        while (flag) {
            response = template.exchange(HOST + "/status",
                    GET,
                    new HttpEntity<String>(headers),
                    String.class);
            assert response.getStatusCodeValue() == 200;
            System.out.println(response.getBody());
            if (response.getBody().contains("Status: DONE")) {
                flag = false;
            }
            Thread.sleep(5 * 1000);
        }
    }
}
