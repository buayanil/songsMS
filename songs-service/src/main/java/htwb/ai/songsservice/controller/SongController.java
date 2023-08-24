package htwb.ai.songsservice.controller;

import htwb.ai.songsservice.feignclient.ApiService;
import htwb.ai.songsservice.model.Song;
import htwb.ai.songsservice.repository.SongRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/songs")
public class SongController {

    private final SongRepository songRepository;
    @Autowired
    private ApiService apiService;

    @Autowired
    public SongController (SongRepository songRepository){
        this.songRepository=songRepository;
    }

    @GetMapping("/validateToken/{token}")
    public boolean validateToken(@PathVariable String token) {
        return apiService.validateToken(token);
    }
    @GetMapping("/message")
    public String test() {
        return "Hello JavaInUse Called in First Service";
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<String> createSongOrSongs(@RequestBody List<Song> songs, @RequestHeader(value = "Authorization", required = false, defaultValue = "") String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (apiService.validateToken(authorizationHeader)) {
            List<String> createdIds = new ArrayList<>();

            for (Song song : songs) {
                if (songRepository.existsById(song.getId())) {
                    return ResponseEntity.badRequest().body("Song with ID " + song.getId() + " already exists.");
                }

                Song savedSong = songRepository.save(song);
                createdIds.add(String.valueOf(savedSong.getId()));
            }

            // Construct a response or return created IDs
            String responseMessage = "Batch creation successful. Created IDs: " + String.join(", ", createdIds);
            return ResponseEntity.ok(responseMessage);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<Song>> getAllSongs(@RequestHeader(value = "Authorization", required = false, defaultValue = "") String authorizationHeader)
    {
        if (authorizationHeader == null || authorizationHeader.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Iterable<Song> songs = songRepository.findAll();
        List<Song> songList = new ArrayList<>();
        songs.forEach(songList::add);
        System.out.println("Token: " +authorizationHeader);

        if(!apiService.validateToken(authorizationHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } else return ResponseEntity.ok(songList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Song> getSongById(@PathVariable("id") Long id, @RequestHeader(value = "Authorization", required = false, defaultValue = "") String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<Song> song = songRepository.findById(id);
        if (apiService.validateToken(authorizationHeader)) {
            if (song.isPresent()) {
                return ResponseEntity.ok(song.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateSong(@PathVariable("id") Long id, @RequestBody Song song, @RequestHeader(value = "Authorization", required = false, defaultValue = "") String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if(apiService.validateToken(authorizationHeader)) {
            long sid = song.getId();
            if (!id.equals(sid)) {
                return ResponseEntity.badRequest().build();
            }

            Optional<Song> existingSong = songRepository.findById(id);
            if (existingSong.isPresent()) {
                songRepository.save(song);
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }



}
