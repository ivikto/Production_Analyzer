package org.example.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.Runner;
import org.example.ZNP;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api")
public class ApiController {

    private final Runner runner;

    public ApiController(Runner runner) {
        this.runner = runner;
    }


    @GetMapping("/get_info_productions")
    public List<ZNP> productions() {
        List<ZNP> znplist = runner.run();
        return znplist;
    }
}
