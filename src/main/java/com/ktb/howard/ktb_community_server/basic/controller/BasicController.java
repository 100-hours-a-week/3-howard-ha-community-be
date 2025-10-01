package com.ktb.howard.ktb_community_server.basic.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/basic")
public class BasicController {

    @GetMapping
    public ResponseEntity<String> basicGetRequest() {
        return ResponseEntity.ok("This is basic get request!");
    }

    @PostMapping
    public ResponseEntity<String> basicPostRequest() {
        return ResponseEntity.ok("This is basic post request!");
    }

    @PutMapping
    public ResponseEntity<String> basicPutRequest() {
        return ResponseEntity.ok("This is basic put request!");
    }

    @DeleteMapping
    public ResponseEntity<String> basicDeleteRequest() {
        return ResponseEntity.ok("This is basic delete request!");
    }

}
