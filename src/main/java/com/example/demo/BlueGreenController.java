package com.example.demo;

import java.net.Inet4Address;
import java.time.Instant;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value = "/v1")
public class BlueGreenController {

    private static int SLEEP;
    private static String WEB_SERVER;
    private static final String SYNC_ID = "SYNC_ID";

    @Value("${bg.version}")
    private String applicationVersion;

    @PostConstruct
    private void init() throws Exception {
        WEB_SERVER = "Web Server: ( " + Inet4Address.getLocalHost().getHostAddress() + " )";
        SLEEP = Integer.parseInt(System.getProperty("syncSleep", "1")) * 60 * 1000;
        log.info("Configured Web Server Local IP {} Sync Sleep {} Version {}", WEB_SERVER, SLEEP, applicationVersion);
    }

    @PostMapping("/initiate")
    public ResponseEntity startSync(HttpServletRequest request) throws Exception {
        HttpSession session = request.getSession(false);
        if (session == null) {
            log.info("{} Request for Sync Initiate HTTP Session NOT found", WEB_SERVER);
            throw new RuntimeException(WEB_SERVER + " HTTP Session Unavailable");
        }
        String syncId = (String) session.getAttribute(SYNC_ID);
        long now = Instant.now().toEpochMilli();
        session.setAttribute("syncNo", syncId);
        session.setAttribute("syncStartedDatetime", now);
        log.info("{} Started Sync {}", WEB_SERVER, syncId);
        String response = WEB_SERVER + " Started Sync " + syncId;
        HttpHeaders headers = new HttpHeaders();
        headers.add(SYNC_ID, syncId);
        return new ResponseEntity(response, headers, HttpStatus.OK);
    }

    @GetMapping("/status")
    public String syncStatus(HttpServletRequest request) throws Exception {
        HttpSession session = request.getSession(false);
        if (session == null) {
            log.info("{} Request for Sync Status HTTP Session NOT found", WEB_SERVER);
            throw new RuntimeException(WEB_SERVER + " HTTP Session Unavailable");
        }
        String syncNo = (String) session.getAttribute("syncNo");
        long started = (long) session.getAttribute("syncStartedDatetime");
        long now = Instant.now().toEpochMilli();
        if ((now - started) < SLEEP) {
            double completed = ((now - started) / (SLEEP * 1.0)) * 100.0;
            String state = Math.round(completed) + "%";
            log.info("{} Get Status Sync:{} State: {}", WEB_SERVER, syncNo, state);
            return WEB_SERVER + " Sync:" + syncNo + " Status:In-Progress State:" + state;
        }
        log.info("{} Request for Status Sync:{} Status:DONE", WEB_SERVER, syncNo);
        return WEB_SERVER + " Sync: " + syncNo + " Status: DONE";
    }

    @DeleteMapping("/logout")
    public void logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            log.info("{} Request for Sync Initiate HTTP Session NOT found", WEB_SERVER);
            throw new RuntimeException(WEB_SERVER + " HTTP Session Unavailable");
        }
        String syncId = (String) session.getAttribute(SYNC_ID);
        if (session != null) {
            session.invalidate();
            log.info("Logged out from Sync:{}", syncId);
        }
    }
}