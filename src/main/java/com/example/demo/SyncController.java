package com.example.demo;

import java.net.Inet4Address;
import java.time.Instant;
import java.util.Random;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class SyncController {

    private static int SLEEP;
    private static String WEB_SERVER;

    @PostConstruct
    private void init() throws Exception {
        WEB_SERVER = "Web Server: " + System.getProperty("label") + " ( " + Inet4Address.getLocalHost().getHostAddress() + " )";
        SLEEP = Integer.parseInt(System.getProperty("syncSleep", "1")) * 60 * 1000;
        log.info("Configured Web Server {} Sync Sleep {} ", WEB_SERVER, SLEEP);
    }

    @PostMapping("/sync")
    public ResponseEntity startSync(HttpServletRequest request) throws Exception {
        HttpSession session = request.getSession(true);
        String syncNo = String.valueOf(new Random().nextInt(900) + 100);
        long now = Instant.now().toEpochMilli();
        session.setAttribute("syncNo", syncNo);
        session.setAttribute("syncStartedDatetime", now);
        log.info("{} Started Sync {}", WEB_SERVER, syncNo);
        String response = WEB_SERVER + " Started Sync: " + syncNo;

        HttpHeaders headers = new HttpHeaders();
        headers.add("Host", "uno");
        return new ResponseEntity(response, headers, HttpStatus.OK);
    }

    @GetMapping("/status")
    public String syncStatus(HttpServletRequest request) throws Exception {
        HttpSession session = request.getSession(false);
        if (session == null) {
            log.info("{} Request for Sync Status HTTP Session NOT found", WEB_SERVER);
            throw new RuntimeException(WEB_SERVER + " HTTP Session Unavailable Web Server");
        }
        String syncNo = (String) session.getAttribute("syncNo");
        long started = (long) session.getAttribute("syncStartedDatetime");
        long now = Instant.now().toEpochMilli();
        if ((now - started) < SLEEP) {
            double completed = ((now - started) / (SLEEP * 1.0)) * 100.0;
            String state = Math.round(completed) + "%";
            log.info("{} Get Status Sync:{} State:", WEB_SERVER, syncNo, state);
            return WEB_SERVER + " Sync:" + syncNo + " Status:In-Progress State:" + state;
        }
        log.info("{} Request for Status Sync:{} Status:DONE", WEB_SERVER, syncNo);
        return WEB_SERVER + " Sync: " + syncNo + " Status: DONE";
    }

}