package htwb.ai.songsservice.controller;

import htwb.ai.songsservice.feignclient.ApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/songs")
public class SongController {
    @Autowired
    private ApiService apiService;

    @GetMapping("/validateToken/{token}")
    public boolean validateToken(@PathVariable String token) {
        return apiService.validateToken(token);
    }
    @GetMapping("/message")
    public String test() {
        return "Hello JavaInUse Called in First Service";
    }
}
