package htwb.ai.songsservice.controller;

import htwb.ai.songsservice.feignclient.ApiService;
import htwb.ai.songsservice.model.Playlist;
import htwb.ai.songsservice.model.Song;
import htwb.ai.songsservice.repository.PlaylistRepository;
import htwb.ai.songsservice.repository.SongRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.*;


@RestController
@RequestMapping("/songs")
public class PlaylistController {

    private final PlaylistRepository playlistRepository;
    @Autowired
    private SongRepository songRepository;
    @Autowired
    private ApiService apiService;

    @Autowired
    public PlaylistController(PlaylistRepository playlistRepository){
        this.playlistRepository=playlistRepository;
    }

    @PostMapping("/playlists")
    public ResponseEntity<?> createPlaylist(@RequestBody Map<String, Object> payload, @RequestHeader(value = "Authorization", required = false, defaultValue = "") String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (apiService.validateToken(authorizationHeader)) {
            String name = (String) payload.get("name");
            boolean isPrivate = (boolean) payload.get("isPrivate");
            String currentUser = apiService.getCurrentUser(authorizationHeader);
            List<Map<String, Object>> songList = (List<Map<String, Object>>) payload.get("songList");

            // Check if each song's title matches its counterpart in the database
            for (Map<String, Object> song : songList) {
                long songId = ((Number) song.get("id")).longValue();
                String songTitle = (String) song.get("title");

                Optional<Song> optionalSong = songRepository.findById(songId);
                if (optionalSong.isPresent()) {
                    Song dbSong = optionalSong.get();
                    if (!dbSong.getTitle().equals(songTitle)) {
                        return ResponseEntity.badRequest().body("Invalid title for song with ID " + songId);
                    }
                } else {
                    return ResponseEntity.badRequest().body("Song with ID " + songId + " does not exist");
                }
            }

            // Create a set to store the songs
            Set<Song> songs = new HashSet<>();

            // Get the songs from the database and add them to the set
            for (Map<String, Object> song : songList) {
                long songId = ((Number) song.get("id")).longValue();
                Optional<Song> optionalSong = songRepository.findById(songId);
                optionalSong.ifPresent(songs::add);
            }

            // Create the playlist and set the songs
            Playlist playlist = new Playlist.Builder()
                    .name(name)
                    .isPrivate(isPrivate)
                    .user(currentUser)
                    .songs(songs)
                    .build();

            // Save the playlist
            Playlist savedPlaylist = playlistRepository.save(playlist);

            // Return the response with the location header
            URI location = ServletUriComponentsBuilder
                    .fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(savedPlaylist.getId())
                    .toUri();
            return ResponseEntity.created(location).build();
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping("/playlists")
    public ResponseEntity<List<Playlist>> getSongListsByUserId(@RequestHeader(value = "Authorization", required = false, defaultValue = "") String authorizationHeader, @RequestParam("userId") String userId) {
        if (authorizationHeader == null || authorizationHeader.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        System.out.println("\tpublic ResponseEntity<List<Playlist>> getSongListsByUserId(@RequestHeader(\"Authorization\") String authorizationHeader, @RequestParam(\"userId\") String userId)");
        System.out.println("authorization token: " +authorizationHeader);
        String currentUser = apiService.getCurrentUser(authorizationHeader); // Retrieve the current user
        System.out.println("######### Current User #########");
        System.out.println(currentUser);
        if (apiService.validateToken(authorizationHeader)) {
            if(apiService.checkUser(userId)) {
                if (currentUser.equals(userId)) {
                    List<Playlist> playlists = playlistRepository.findByUserId(userId);
                    // User is requesting their own playlists, return all playlists
                    return ResponseEntity.ok(playlists);
                } else {
                    // User is requesting playlists of another user, return only public playlists
                    List<Playlist> playlists = new ArrayList<>();
                    List<Playlist> tmpPlaylists = playlistRepository.findByUserId(userId);
                    for (Playlist tmpPlaylist : tmpPlaylists) {
                        if (!tmpPlaylist.isPrivate()) {
                            playlists.add(tmpPlaylist);
                        }
                    }
                    return ResponseEntity.ok(playlists);
                }
            }else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping("/playlists/{id}")
    public ResponseEntity<Playlist> getSongListById(@PathVariable("id") int id, @RequestHeader(value = "Authorization", required = false, defaultValue = "") String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (apiService.validateToken(authorizationHeader)) {
            Optional<Playlist> playlistOptional = playlistRepository.findById(id);
            String currentUser = apiService.getCurrentUser(authorizationHeader);
            if (playlistOptional.isPresent()) {
                Playlist playlist = playlistOptional.get();

                if (playlist.getUserid().equals(currentUser) || !playlist.isPrivate())
                    return ResponseEntity.ok(playlist);
                else
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @DeleteMapping("playlists/{id}")
    public ResponseEntity<Void> deletePlaylist(@PathVariable("id") int id, @RequestHeader(value = "Authorization", required = false, defaultValue = "") String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (apiService.validateToken(authorizationHeader)) {
            String currentUser = apiService.getCurrentUser(authorizationHeader);
            Optional<Playlist> playlistOptional = playlistRepository.findById(id);
            if (!playlistOptional.isPresent()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            if (!currentUser.equals(playlistOptional.get().getUserid())) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
            playlistRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PutMapping("/playlists/{id}")
    public ResponseEntity<Void> updatePlaylist(@PathVariable("id") int id, @RequestBody Map<String, Object> payload, @RequestHeader(value = "Authorization", required = false, defaultValue = "") String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (apiService.validateToken(authorizationHeader)) {
            Optional<Playlist> playlistOptional = playlistRepository.findById(id);
            String currentUser = apiService.getCurrentUser(authorizationHeader);

            if (playlistOptional.isPresent()) {
                Playlist playlist = playlistOptional.get();

                // Check if the current user is the owner of the playlist
                if (currentUser.equals(playlist.getUserid())) {
                    // Update the playlist properties based on the payload
                    String name = (String) payload.get("name");
                    boolean isPrivate = (boolean) payload.get("isPrivate");
                    List<Map<String, Object>> songListData = (List<Map<String, Object>>) payload.get("songList");

                    // Check if songListData is not null and update the playlist's songList
                    if (songListData != null) {
                        Set<Song> updatedSongList = new HashSet<>();

                        for (Map<String, Object> songData : songListData) {
                            long songId = ((Number) songData.get("id")).longValue();

                            // Retrieve the song from the database using songId
                            Optional<Song> optionalSong = songRepository.findById(songId);

                            if (optionalSong.isPresent()) {
                                // If the song exists, add it to the updatedSongList
                                updatedSongList.add(optionalSong.get());
                            } else {
                                // Handle the case when the song with the given ID does not exist
                                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
                            }
                        }

                        // Update the playlist's songList
                        playlist.setSongs(updatedSongList);
                    }

                    // Update other properties as needed (name, isPrivate, etc.)
                    playlist.setName(name);
                    playlist.setPrivate(isPrivate);
                    // Update other properties as needed

                    // Save the updated playlist
                    playlistRepository.save(playlist);

                    return ResponseEntity.noContent().build();
                } else {
                    // Current user is not the owner of the playlist
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            } else {
                // Playlist not found
                return ResponseEntity.notFound().build();
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping("/playlists/isPrivate/{id}")
    public boolean isPrivate(@PathVariable("id") int id){
        return playlistRepository.findById(id).get().isPrivate();
    }

    @GetMapping("/playlists/getOwner/{id}")
    public String getOwner(@PathVariable("id") int id){
        return playlistRepository.findById(id).get().getUserid();
    }

    @GetMapping("playlists/isPlaylistExistById/{id}")
    public boolean isPlaylistExistById(@PathVariable("id")int id){
        return playlistRepository.existsById(id);
    }


}
