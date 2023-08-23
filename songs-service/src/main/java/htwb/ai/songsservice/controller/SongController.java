package htwb.ai.songsservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/songs")
public class SongController {
    @GetMapping("/message")
    public String test() {
        return "Hello JavaInUse Called in First Service";
    }
}
