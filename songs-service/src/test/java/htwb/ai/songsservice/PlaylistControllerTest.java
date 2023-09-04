package htwb.ai.songsservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import htwb.ai.songsservice.controller.PlaylistController;
import htwb.ai.songsservice.feignclient.ApiService;
import htwb.ai.songsservice.model.Playlist;
import htwb.ai.songsservice.model.Song;
import htwb.ai.songsservice.repository.PlaylistRepository;
import htwb.ai.songsservice.repository.SongRepository;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

@WebMvcTest(PlaylistController.class)
@AutoConfigureMockMvc
public class PlaylistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlaylistRepository playlistRepository;

    @MockBean
    private SongRepository songRepository;

    @MockBean
    private ApiService apiService;

    private Set<Song> sampleSongs;
    private Playlist samplePlaylist;

    private Playlist playlist;

    @BeforeEach
    public void setUp() {
        // Initialize sampleSongs and samplePlaylist
        sampleSongs = new HashSet<>();
        sampleSongs.add(new Song.Builder()
                .id(1L)
                .title("KOMM SUSSER TOD")
                .artist("Arianne")
                .album("Evangelion Finally")
                .released(2020)
                .build());

        sampleSongs.add(new Song.Builder()
                .id(4L)
                .title("Sussudio")
                .artist("Phil Collins")
                .album("Virgin")
                .released(1985)
                .build());

        samplePlaylist = new Playlist.Builder()
                .isPrivate(false)
                .name("Test Playlist")
                .user("UserIdHere") // Replace with a valid user ID
                .songs(sampleSongs)
                .build();

        // Mock other behaviors as needed
        playlist = new Playlist.Builder()
                .isPrivate(false)
                .name("Test Playlist")
                .user("TestUser")
                .build();
    }

    @Test
    public void testCreatePlaylist() throws Exception {
        // Create a sample song list
        List<Map<String, Object>> songList = new ArrayList<>();

        // Create a song map for the first song
        Map<String, Object> song1 = new HashMap<>();
        song1.put("id", 1);
        song1.put("title", "KOMM SUSSER TOD");
        song1.put("artist", "Arianne");
        song1.put("album", "Evangelion Finally");
        song1.put("released", 2020);

        // Create a song map for the second song
        Map<String, Object> song2 = new HashMap<>();
        song2.put("id", 4);
        song2.put("title", "Sussudio");
        song2.put("artist", "Phil Collins");
        song2.put("album", "Virgin");
        song2.put("released", 1985);

        // Add the songs to the songList
        songList.add(song1);
        songList.add(song2);

        // Create the payload as a map
        Map<String, Object> payload = new HashMap<>();
        payload.put("isPrivate", false);
        payload.put("name", "MaximesPublic2");
        payload.put("songList", songList);

        // Mock the behavior of the ApiService to return true for token validation
        Mockito.when(apiService.validateToken(Mockito.anyString())).thenReturn(true);

        // Mock the behavior of playlistRepository.save to return the saved playlist
        Mockito.when(playlistRepository.save(Mockito.any(Playlist.class))).thenReturn(samplePlaylist);

        // Mock the behavior of songRepository.findById to return non-empty Optionals
        // For the first song with ID 1
        Mockito.when(songRepository.findById(1L)).thenReturn(Optional.of(new Song.Builder()
                .id(1L)
                .title("KOMM SUSSER TOD")
                .artist("Arianne")
                .album("Evangelion Finally")
                .released(2020)
                .build()));

        // For the second song with ID 4
        Mockito.when(songRepository.findById(4L)).thenReturn(Optional.of(new Song.Builder()
                .id(4L)
                .title("Sussudio")
                .artist("Phil Collins")
                .album("Virgin")
                .released(1985)
                .build()));

        // Perform a POST request to create a playlist
        mockMvc.perform(MockMvcRequestBuilders.post("/songs/playlists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(payload))
                        .header("Authorization", "ValidTokenHere"))
                .andExpect(MockMvcResultMatchers.status().isCreated()) // Expecting a 201 Created status code
                .andExpect(MockMvcResultMatchers.header().exists("Location")); // Expecting a Location header
    }

    @Test
    public void testCreatePlaylistWithEmptyHeader() throws Exception {
        // Create a sample song list
        List<Map<String, Object>> songList = new ArrayList<>();

        // Create a song map for the first song
        Map<String, Object> song1 = new HashMap<>();
        song1.put("id", 1);
        song1.put("title", "KOMM SUSSER TOD");
        song1.put("artist", "Arianne");
        song1.put("album", "Evangelion Finally");
        song1.put("released", 2020);

        // Create a song map for the second song
        Map<String, Object> song2 = new HashMap<>();
        song2.put("id", 4);
        song2.put("title", "Sussudio");
        song2.put("artist", "Phil Collins");
        song2.put("label", "Virgin");
        song2.put("released", 1985);

        // Add the songs to the songList
        songList.add(song1);
        songList.add(song2);

        // Create the payload as a map
        Map<String, Object> payload = new HashMap<>();
        payload.put("isPrivate", false);
        payload.put("name", "MaximesPublic2");
        payload.put("songList", songList);

        // Mock the behavior of the ApiService to return false for token validation
        Mockito.when(apiService.validateToken(Mockito.anyString())).thenReturn(false);

        // Perform a POST request to create a playlist with an empty header
        mockMvc.perform(MockMvcRequestBuilders.post("/songs/playlists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(payload)))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized()); // Expecting a 401 Unauthorized status code
    }

    @Test
    public void testCreatePlaylistWithUnauthorizedToken() throws Exception {
        // Create a sample song list
        List<Map<String, Object>> songList = new ArrayList<>();

        // Create a song map for the first song
        Map<String, Object> song1 = new HashMap<>();
        song1.put("id", 1);
        song1.put("title", "KOMM SUSSER TOD");
        song1.put("artist", "Arianne");
        song1.put("album", "Evangelion Finally");
        song1.put("released", 2020);

        // Create a song map for the second song
        Map<String, Object> song2 = new HashMap<>();
        song2.put("id", 4);
        song2.put("title", "Sussudio");
        song2.put("artist", "Phil Collins");
        song2.put("label", "Virgin");
        song2.put("released", 1985);

        // Add the songs to the songList
        songList.add(song1);
        songList.add(song2);

        // Create the payload as a map
        Map<String, Object> payload = new HashMap<>();
        payload.put("isPrivate", false);
        payload.put("name", "MaximesPublic2");
        payload.put("songList", songList);

        // Mock the behavior of the ApiService to return false for token validation
        Mockito.when(apiService.validateToken(Mockito.anyString())).thenReturn(false);

        // Perform a POST request to create a playlist with an unauthorized token
        mockMvc.perform(MockMvcRequestBuilders.post("/songs/playlists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(payload))
                        .header("Authorization", "UnauthorizedTokenHere"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized()); // Expecting a 401 Unauthorized status code
    }

    @Test
    public void testCreatePlaylistWithMismatchedTitle() throws Exception {
        // Create a sample song list
        List<Map<String, Object>> songList = new ArrayList<>();

        // Create a song map for the first song
        Map<String, Object> song1 = new HashMap<>();
        song1.put("id", 1);
        song1.put("title", "KOMM SUSSER TOD"); // Title in the payload
        song1.put("artist", "Arianne");
        song1.put("album", "Evangelion Finally");
        song1.put("released", 2020);

        // Create a song map for the second song
        Map<String, Object> song2 = new HashMap<>();
        song2.put("id", 4);
        song2.put("title", "Sussudio"); // Title in the payload
        song2.put("artist", "Phil Collins");
        song2.put("label", "Virgin");
        song2.put("released", 1985);

        // Add the songs to the songList
        songList.add(song1);
        songList.add(song2);

        // Create the payload as a map
        Map<String, Object> payload = new HashMap<>();
        payload.put("isPrivate", false);
        payload.put("name", "MaximesPublic2");
        payload.put("songList", songList);

        // Mock the behavior of the ApiService to return true for token validation
        Mockito.when(apiService.validateToken(Mockito.anyString())).thenReturn(true);

        // Mock the behavior of playlistRepository.save to return the saved playlist
        Mockito.when(playlistRepository.save(Mockito.any(Playlist.class))).thenReturn(samplePlaylist);

        // Mock the behavior of songRepository.findById to return non-empty Optionals
        // For the first song with ID 1, but with a different title
        Mockito.when(songRepository.findById(1L)).thenReturn(Optional.of(new Song.Builder()
                .id(1L)
                .title("DifferentTitle") // Title in the repository is different
                .artist("Arianne")
                .album("Evangelion Finally")
                .released(2020)
                .build()));

        // For the second song with ID 4, but with a different title
        Mockito.when(songRepository.findById(4L)).thenReturn(Optional.of(new Song.Builder()
                .id(4L)
                .title("AnotherDifferentTitle") // Title in the repository is different
                .artist("Phil Collins")
                .album("Virgin")
                .released(1985)
                .build()));

        // Perform a POST request to create a playlist
        mockMvc.perform(MockMvcRequestBuilders.post("/songs/playlists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(payload))
                        .header("Authorization", "ValidTokenHere"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest()) // Expecting a 400 Bad Request status code
                .andExpect(MockMvcResultMatchers.content().string(
                        "Invalid title for song with ID 1")); // Expecting an error message
    }

    @Test
    public void testCreatePlaylistWithNonExistentSongId() throws Exception {
        // Create a sample song list
        List<Map<String, Object>> songList = new ArrayList<>();

        // Create a song map for the first song
        Map<String, Object> song1 = new HashMap<>();
        song1.put("id", 1); // Existing song ID in the payload
        song1.put("title", "KOMM SUSSER TOD");
        song1.put("artist", "Arianne");
        song1.put("album", "Evangelion Finally");
        song1.put("released", 2020);

        // Create a song map for the second song with a non-existent ID
        Map<String, Object> song2 = new HashMap<>();
        song2.put("id", 999); // Non-existent song ID in the payload
        song2.put("title", "Sussudio");
        song2.put("artist", "Phil Collins");
        song2.put("label", "Virgin");
        song2.put("released", 1985);

        // Add the songs to the songList
        songList.add(song1);
        songList.add(song2);

        // Create the payload as a map
        Map<String, Object> payload = new HashMap<>();
        payload.put("isPrivate", false);
        payload.put("name", "MaximesPublic2");
        payload.put("songList", songList);

        // Mock the behavior of the ApiService to return true for token validation
        Mockito.when(apiService.validateToken(Mockito.anyString())).thenReturn(true);

        // Mock the behavior of playlistRepository.save to return the saved playlist
        Mockito.when(playlistRepository.save(Mockito.any(Playlist.class))).thenReturn(samplePlaylist);

        // Mock the behavior of songRepository.findById to return empty Optional for non-existent song
        Mockito.when(songRepository.findById(1L)).thenReturn(Optional.of(new Song.Builder()
                .id(1L)
                .title("KOMM SUSSER TOD")
                .artist("Arianne")
                .album("Evangelion Finally")
                .released(2020)
                .build()));

        // For the second song with a non-existent ID, return an empty Optional
        Mockito.when(songRepository.findById(999L)).thenReturn(Optional.empty());

        // Perform a POST request to create a playlist
        mockMvc.perform(MockMvcRequestBuilders.post("/songs/playlists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(payload))
                        .header("Authorization", "ValidTokenHere"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest()) // Expecting a 400 Bad Request status code
                .andExpect(MockMvcResultMatchers.content().string(
                        "Song with ID 999 does not exist")); // Expecting an error message for the non-existent song ID
    }

    @Test
    public void testGetSongListsByUserIdSuccess() throws Exception {
        // Mock the behavior of the ApiService to return true for token validation
        Mockito.when(apiService.validateToken(Mockito.anyString())).thenReturn(true);

        // Mock the behavior of apiService.getCurrentUser to return a valid user ID
        Mockito.when(apiService.getCurrentUser(Mockito.anyString())).thenReturn("ValidUser123");

        // Mock the behavior of apiService.checkUser to return true for an authorized user
        Mockito.when(apiService.checkUser(Mockito.anyString())).thenReturn(true);

        // Mock the behavior of playlistRepository.findByUserId to return a list of playlists
        List<Playlist> playlists = new ArrayList<>();
        playlists.add(samplePlaylist); // Add sample playlist to the list
        Mockito.when(playlistRepository.findByUserId(Mockito.anyString())).thenReturn(playlists);

        // Perform a GET request to retrieve song lists by user ID
        mockMvc.perform(MockMvcRequestBuilders.get("/songs/playlists")
                        .param("userId", "ValidUser123") // Provide a valid user ID
                        .header("Authorization", "ValidTokenHere"))
                .andExpect(MockMvcResultMatchers.status().isOk()) // Expecting a 200 OK status code
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(1))) // Expecting one playlist in the response
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name", Matchers.is("Test Playlist"))); // Verify the name of the playlist
    }

    @Test
    public void testGetSongListsByUserIdEmptyAuthorizationHeader() throws Exception {
        // Perform a GET request to retrieve song lists with an empty authorization header
        mockMvc.perform(MockMvcRequestBuilders.get("/songs/playlists")
                        .param("userId", "ValidUser123") // Provide a valid user ID
                        .header("Authorization", ""))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized()); // Expecting a 401 Unauthorized status code
    }


    @Test
    public void testGetPublicPlaylistsOfAnotherUser() throws Exception {
        // Mock the behavior of the ApiService to return true for token validation
        Mockito.when(apiService.validateToken(Mockito.anyString())).thenReturn(true);

        // Mock the behavior of apiService.getCurrentUser to return a valid user ID
        Mockito.when(apiService.getCurrentUser(Mockito.anyString())).thenReturn("ValidUser123");

        // Mock the behavior of apiService.checkUser to return true for an authorized user
        Mockito.when(apiService.checkUser(Mockito.anyString())).thenReturn(true);

        // Mock the behavior of playlistRepository.findByUserId to return a list of playlists
        List<Playlist> playlists = new ArrayList<>();
        // Add public playlists to the list (replace this with actual public playlists)
        playlists.add(createSamplePublicPlaylist());
        playlists.add(createSamplePublicPlaylist());
        Mockito.when(playlistRepository.findByUserId(Mockito.anyString())).thenReturn(playlists);

        // Perform a GET request to retrieve public playlists of another user
        mockMvc.perform(MockMvcRequestBuilders.get("/songs/playlists")
                        .param("userId", "AnotherUser456") // Provide another user's ID
                        .header("Authorization", "ValidTokenHere"))
                .andExpect(MockMvcResultMatchers.status().isOk()) // Expecting a 200 OK status code
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2))); // Expecting two public playlists in the response
    }

    // Helper method to create a sample public playlist for testing
    private Playlist createSamplePublicPlaylist() {
        return new Playlist.Builder()
                .name("Sample Public Playlist")
                .isPrivate(false)
                .user("AnotherUser456")
                .songs(new HashSet<>())
                .build();
    }

    @Test
    public void testGetSongListsByUserIdUnauthorizedUser() throws Exception {
        // Mock the behavior of the ApiService to return true for token validation
        Mockito.when(apiService.validateToken(Mockito.anyString())).thenReturn(true);

        // Mock the behavior of apiService.getCurrentUser to return a valid user ID
        Mockito.when(apiService.getCurrentUser(Mockito.anyString())).thenReturn("ValidUser123");

        // Mock the behavior of apiService.checkUser to return false for an unauthorized user
        Mockito.when(apiService.checkUser(Mockito.anyString())).thenReturn(false);

        // Perform a GET request to retrieve song lists with an unauthorized user ID
        mockMvc.perform(MockMvcRequestBuilders.get("/songs/playlists")
                        .param("userId", "UnauthorizedUser456") // Provide an unauthorized user ID
                        .header("Authorization", "ValidTokenHere"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest()); // Expecting a 400 Bad Request status code
    }

    @Test
    public void testGetSongListsByUserIdInvalidToken() throws Exception {
        // Mock the behavior of the ApiService to return false for token validation (invalid token)
        Mockito.when(apiService.validateToken(Mockito.anyString())).thenReturn(false);

        // Perform a GET request to retrieve song lists with an invalid token
        mockMvc.perform(MockMvcRequestBuilders.get("/songs/playlists")
                        .param("userId", "ValidUser123") // Provide a valid user ID
                        .header("Authorization", "InvalidTokenHere")) // Provide an invalid token
                .andExpect(MockMvcResultMatchers.status().isUnauthorized()); // Expecting a 401 Unauthorized status code
    }

    @Test
    public void testGetSongListByIdSuccessWithAuthorizationHeader() throws Exception {
        // Create a sample playlist with known attributes
        Playlist samplePlaylist = new Playlist.Builder()
                .isPrivate(false)
                .name("Sample Playlist")
                .user("ValidUser123")
                .songs(new HashSet<>()) // Add songs if needed
                .build();

        // Mock the behavior of apiService.validateToken to return true (valid token)
        Mockito.when(apiService.validateToken(Mockito.anyString())).thenReturn(true);

        // Mock the behavior of apiService.getCurrentUser to return a valid user ID
        Mockito.when(apiService.getCurrentUser(Mockito.anyString())).thenReturn("ValidUser123");

        // Mock the behavior of playlistRepository.findById to return the sample playlist
        Mockito.when(playlistRepository.findById(1)).thenReturn(Optional.of(samplePlaylist));

        // Perform a GET request to retrieve the playlist by its ID with a valid authorization header
        mockMvc.perform(MockMvcRequestBuilders.get("/songs/playlists/{id}", 1)
                        .header("Authorization", "ValidTokenHere")) // Replace with a valid token
                .andExpect(MockMvcResultMatchers.status().isOk()) // Expecting a 200 OK status code
                .andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.is("Sample Playlist"))); // Verify the name
    }

    @Test
    public void testGetSongListByIdUnauthorizedEmptyAuthorizationHeader() throws Exception {
        // Perform a GET request to retrieve the playlist by its ID with an empty authorization header
        mockMvc.perform(MockMvcRequestBuilders.get("/songs/playlists/{id}", 1)
                        .header("Authorization", "")) // Empty authorization header
                .andExpect(MockMvcResultMatchers.status().isUnauthorized()); // Expecting a 401 Unauthorized status code
    }

    @Test
    public void testGetSongListByIdPrivatePlaylistUnauthorizedUser() throws Exception {
        // Create a sample playlist with known attributes
        Playlist privatePlaylist = new Playlist.Builder()
                .isPrivate(true) // Private playlist
                .name("Private Playlist")
                .user("OwnerUser123") // Owner's user ID
                .songs(new HashSet<>()) // Add songs if needed
                .build();

        // Mock the behavior of apiService.validateToken to return true (valid token)
        Mockito.when(apiService.validateToken(Mockito.anyString())).thenReturn(true);

        // Mock the behavior of apiService.getCurrentUser to return a different user ID than the owner
        Mockito.when(apiService.getCurrentUser(Mockito.anyString())).thenReturn("DifferentUser456");

        // Mock the behavior of playlistRepository.findById to return the private playlist
        Mockito.when(playlistRepository.findById(1)).thenReturn(Optional.of(privatePlaylist));

        // Perform a GET request to retrieve the private playlist by its ID with a valid authorization header
        mockMvc.perform(MockMvcRequestBuilders.get("/songs/playlists/{id}", 1)
                        .header("Authorization", "ValidTokenHere")) // Replace with a valid token
                .andExpect(MockMvcResultMatchers.status().isForbidden()); // Expecting a 403 Forbidden status code
    }

    @Test
    public void testGetSongListByIdPlaylistNotFound() throws Exception {
        // Mock the behavior of apiService.validateToken to return true (valid token)
        Mockito.when(apiService.validateToken(Mockito.anyString())).thenReturn(true);

        // Mock the behavior of playlistRepository.findById to return an empty Optional, indicating a not found playlist
        Mockito.when(playlistRepository.findById(1)).thenReturn(Optional.empty());

        // Perform a GET request to retrieve a playlist by its ID with a valid authorization header
        mockMvc.perform(MockMvcRequestBuilders.get("/songs/playlists/{id}", 1)
                        .header("Authorization", "ValidTokenHere")) // Replace with a valid token
                .andExpect(MockMvcResultMatchers.status().isNotFound()); // Expecting a 404 Not Found status code
    }

    @Test
    public void testGetSongListByIdUnauthorizedToken() throws Exception {
        // Mock the behavior of apiService.validateToken to return false (invalid token)
        Mockito.when(apiService.validateToken(Mockito.anyString())).thenReturn(false);

        // Perform a GET request to retrieve a playlist by its ID with an invalid authorization header
        mockMvc.perform(MockMvcRequestBuilders.get("/songs/playlists/{id}", 1)
                        .header("Authorization", "InvalidTokenHere")) // Replace with an invalid token
                .andExpect(MockMvcResultMatchers.status().isUnauthorized()); // Expecting a 401 Unauthorized status code
    }

    @Test
    public void testDeletePlaylistSuccess() throws Exception {
        // Mock the behavior of apiService.validateToken to return true (valid token)
        Mockito.when(apiService.validateToken(Mockito.anyString())).thenReturn(true);

        // Mock the behavior of apiService.getCurrentUser to return the user ID of the owner of the playlist
        Mockito.when(apiService.getCurrentUser(Mockito.anyString())).thenReturn("OwnerUser123");

        // Mock the behavior of playlistRepository.findById to return the playlist to be deleted
        Playlist playlistToDelete = new Playlist.Builder()
                .isPrivate(false)
                .name("Sample Playlist")
                .user("OwnerUser123")
                .songs(new HashSet<>()) // Add songs if needed
                .build();
        Mockito.when(playlistRepository.findById(1)).thenReturn(Optional.of(playlistToDelete));

        // Perform a DELETE request to delete the playlist by its ID with a valid authorization header
        mockMvc.perform(MockMvcRequestBuilders.delete("/songs/playlists/{id}", 1)
                        .header("Authorization", "ValidTokenHere")) // Replace with a valid token
                .andExpect(MockMvcResultMatchers.status().isNoContent()); // Expecting a 204 No Content status code
    }

    @Test
    public void testDeletePlaylistUnauthorizedEmptyAuthorizationHeader() throws Exception {
        // Perform a DELETE request to delete a playlist by its ID with an empty authorization header
        mockMvc.perform(MockMvcRequestBuilders.delete("/songs/playlists/{id}", 1)
                        .header("Authorization", "")) // Empty authorization header
                .andExpect(MockMvcResultMatchers.status().isUnauthorized()); // Expecting a 401 Unauthorized status code
    }

    @Test
    public void testDeletePlaylistNotFound() throws Exception {
        // Mock the behavior of apiService.validateToken to return true (valid token)
        Mockito.when(apiService.validateToken(Mockito.anyString())).thenReturn(true);

        // Mock the behavior of playlistRepository.findById to return an empty Optional, indicating a not found playlist
        Mockito.when(playlistRepository.findById(1)).thenReturn(Optional.empty());

        // Perform a DELETE request to delete a playlist with a non-existent ID using a valid authorization header
        mockMvc.perform(MockMvcRequestBuilders.delete("/songs/playlists/{id}", 1)
                        .header("Authorization", "ValidTokenHere")) // Replace with a valid token
                .andExpect(MockMvcResultMatchers.status().isNotFound()); // Expecting a 404 Not Found status code
    }

    @Test
    public void testDeletePlaylistForbidden() throws Exception {
        // Mock the behavior of apiService.validateToken to return true (valid token)
        Mockito.when(apiService.validateToken(Mockito.anyString())).thenReturn(true);

        // Mock the behavior of apiService.getCurrentUser to return the user ID of the current user
        Mockito.when(apiService.getCurrentUser(Mockito.anyString())).thenReturn("CurrentUser123");

        // Mock the behavior of playlistRepository.findById to return a playlist with a different owner
        Playlist playlistWithDifferentOwner = new Playlist.Builder()
                .isPrivate(false)
                .name("Sample Playlist")
                .user("OwnerUser456") // Different owner than the current user
                .songs(new HashSet<>()) // Add songs if needed
                .build();
        Mockito.when(playlistRepository.findById(1)).thenReturn(Optional.of(playlistWithDifferentOwner));

        // Perform a DELETE request to delete the playlist with a different owner using a valid authorization header
        mockMvc.perform(MockMvcRequestBuilders.delete("/songs/playlists/{id}", 1)
                        .header("Authorization", "ValidTokenHere")) // Replace with a valid token
                .andExpect(MockMvcResultMatchers.status().isForbidden()); // Expecting a 403 Forbidden status code
    }

    @Test
    public void testDeletePlaylistUnauthorizedToken() throws Exception {
        // Mock the behavior of apiService.validateToken to return false (invalid token)
        Mockito.when(apiService.validateToken(Mockito.anyString())).thenReturn(false);

        // Perform a DELETE request to delete a playlist with an invalid authorization header
        mockMvc.perform(MockMvcRequestBuilders.delete("/songs/playlists/{id}", 1)
                        .header("Authorization", "InvalidTokenHere")) // Replace with an invalid token
                .andExpect(MockMvcResultMatchers.status().isUnauthorized()); // Expecting a 401 Unauthorized status code
    }

    @Test
    public void testUpdatePlaylistSuccessWithSongList() throws Exception {
        // Mock the behavior of apiService.validateToken to return true (valid token)
        Mockito.when(apiService.validateToken(Mockito.anyString())).thenReturn(true);

        // Mock the behavior of apiService.getCurrentUser to return the current user's ID
        Mockito.when(apiService.getCurrentUser(Mockito.anyString())).thenReturn("CurrentUser123");

        // Mock the behavior of playlistRepository.findById to return a playlist with the same owner as the current user
        Playlist existingPlaylist = new Playlist.Builder()
                .isPrivate(false)
                .name("Old Playlist Name")
                .user("CurrentUser123") // Same owner as the current user
                .songs(new HashSet<>()) // Add songs if needed
                .build();
        Mockito.when(playlistRepository.findById(1)).thenReturn(Optional.of(existingPlaylist));

        // Define the updated payload with song list data
        Map<String, Object> updatedPayload = new HashMap<>();
        updatedPayload.put("name", "New Playlist Name");
        updatedPayload.put("isPrivate", true);

        // Create a song list with song data
        List<Map<String, Object>> songListData = new ArrayList<>();
        Map<String, Object> songData1 = new HashMap<>();
        songData1.put("id", 1);
        // Add other song data fields as needed
        songListData.add(songData1);

        Map<String, Object> songData2 = new HashMap<>();
        songData2.put("id", 2);
        // Add other song data fields as needed
        songListData.add(songData2);

        updatedPayload.put("songList", songListData);

        // Mock the behavior of songRepository.findById to return non-empty Optionals for each song
        // Replace these with your song data as needed
        Mockito.when(songRepository.findById(1L)).thenReturn(Optional.of(new Song.Builder()
                .id(1L)
                .title("Song Title 1")
                .artist("Artist 1")
                .album("Album 1")
                .released(2021)
                .build()));

        Mockito.when(songRepository.findById(2L)).thenReturn(Optional.of(new Song.Builder()
                .id(2L)
                .title("Song Title 2")
                .artist("Artist 2")
                .album("Album 2")
                .released(2022)
                .build()));

        // Perform a PUT request to update the playlist with the updated payload and valid authorization header
        mockMvc.perform(MockMvcRequestBuilders.put("/songs/playlists/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(updatedPayload))
                        .header("Authorization", "ValidTokenHere")) // Replace with a valid token
                .andExpect(MockMvcResultMatchers.status().isNoContent()); // Expecting a 204 No Content status code
    }



    @Test
    public void testUpdatePlaylistUnauthorizedEmptyToken() throws Exception {
        // Perform a PUT request to update the playlist with an empty authorization header
        mockMvc.perform(MockMvcRequestBuilders.put("/songs/playlists/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")) // Empty payload
                .andExpect(MockMvcResultMatchers.status().isUnauthorized()); // Expecting a 401 Unauthorized status code
    }

    @Test
    public void testUpdatePlaylistNotFound() throws Exception {
        // Mock the behavior of apiService.validateToken to return true (valid token)
        Mockito.when(apiService.validateToken(Mockito.anyString())).thenReturn(true);

        // Mock the behavior of apiService.getCurrentUser to return the current user's ID
        Mockito.when(apiService.getCurrentUser(Mockito.anyString())).thenReturn("CurrentUser123");

        // Mock the behavior of playlistRepository.findById to return an empty Optional (playlist not found)
        Mockito.when(playlistRepository.findById(1)).thenReturn(Optional.empty());

        // Define the updated payload
        Map<String, Object> updatedPayload = new HashMap<>();
        updatedPayload.put("name", "New Playlist Name");
        updatedPayload.put("isPrivate", true);
        // Add any other fields or updates needed

        // Perform a PUT request to update the non-existent playlist using a valid authorization header
        mockMvc.perform(MockMvcRequestBuilders.put("/songs/playlists/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(updatedPayload))
                        .header("Authorization", "ValidTokenHere")) // Replace with a valid token
                .andExpect(MockMvcResultMatchers.status().isNotFound()); // Expecting a 404 Not Found status code
    }

    @Test
    public void testUpdatePlaylistUnauthorizedToken() throws Exception {
        // Mock the behavior of apiService.validateToken to return false (invalid token)
        Mockito.when(apiService.validateToken(Mockito.anyString())).thenReturn(false);

        // Define the updated payload
        Map<String, Object> updatedPayload = new HashMap<>();
        updatedPayload.put("name", "New Playlist Name");
        updatedPayload.put("isPrivate", true);
        // Add any other fields or updates needed

        // Perform a PUT request to update the playlist using an invalid authorization header
        mockMvc.perform(MockMvcRequestBuilders.put("/songs/playlists/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(updatedPayload))
                        .header("Authorization", "InvalidTokenHere")) // Replace with an invalid token
                .andExpect(MockMvcResultMatchers.status().isUnauthorized()); // Expecting a 401 Unauthorized status code
    }

    @Test
    public void testUpdatePlaylistForbidden() throws Exception {
        // Mock the behavior of apiService.validateToken to return true (valid token)
        Mockito.when(apiService.validateToken(Mockito.anyString())).thenReturn(true);

        // Mock the behavior of apiService.getCurrentUser to return the current user's ID
        Mockito.when(apiService.getCurrentUser(Mockito.anyString())).thenReturn("CurrentUser123");

        // Mock the behavior of playlistRepository.findById to return a playlist owned by another user
        Playlist playlistOwnedByAnotherUser = new Playlist.Builder()
                .name("OtherUserPlaylist")
                .user("OtherUser456")
                .build();
        Mockito.when(playlistRepository.findById(1)).thenReturn(Optional.of(playlistOwnedByAnotherUser));

        // Define the updated payload
        Map<String, Object> updatedPayload = new HashMap<>();
        updatedPayload.put("name", "New Playlist Name");
        updatedPayload.put("isPrivate", true);
        // Add any other fields or updates needed

        // Perform a PUT request to update the playlist owned by another user
        mockMvc.perform(MockMvcRequestBuilders.put("/songs/playlists/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(updatedPayload))
                        .header("Authorization", "ValidTokenHere")) // Replace with a valid token
                .andExpect(MockMvcResultMatchers.status().isForbidden()); // Expecting a 403 Forbidden status code
    }

    @Test
    public void testUpdatePlaylistFailureSongNotFound() throws Exception {
        // Mock the behavior of apiService.validateToken to return true (valid token)
        Mockito.when(apiService.validateToken(Mockito.anyString())).thenReturn(true);

        // Mock the behavior of apiService.getCurrentUser to return the current user's ID
        Mockito.when(apiService.getCurrentUser(Mockito.anyString())).thenReturn("CurrentUser123");

        // Mock the behavior of playlistRepository.findById to return a playlist with the same owner as the current user
        Playlist existingPlaylist = new Playlist.Builder()
                .isPrivate(false)
                .name("Old Playlist Name")
                .user("CurrentUser123") // Same owner as the current user
                .songs(new HashSet<>()) // Add songs if needed
                .build();
        Mockito.when(playlistRepository.findById(1)).thenReturn(Optional.of(existingPlaylist));

        // Define the updated payload with song list data that includes a non-existent song ID
        Map<String, Object> updatedPayload = new HashMap<>();
        updatedPayload.put("name", "New Playlist Name");
        updatedPayload.put("isPrivate", true);

        // Create a song list with song data that includes a non-existent song ID
        List<Map<String, Object>> songListData = new ArrayList<>();
        Map<String, Object> songData1 = new HashMap<>();
        songData1.put("id", 1); // An existing song ID
        // Add other song data fields as needed
        songListData.add(songData1);

        Map<String, Object> songData2 = new HashMap<>();
        songData2.put("id", 3); // A non-existent song ID
        // Add other song data fields as needed
        songListData.add(songData2);

        updatedPayload.put("songList", songListData);

        // Mock the behavior of songRepository.findById to return a non-empty Optional for the existing song
        // Replace this with your existing song data as needed
        Mockito.when(songRepository.findById(1L)).thenReturn(Optional.of(new Song.Builder()
                .id(1L)
                .title("Song Title 1")
                .artist("Artist 1")
                .album("Album 1")
                .released(2021)
                .build()));

        // Mock the behavior of songRepository.findById to return an empty Optional for the non-existent song
        Mockito.when(songRepository.findById(3L)).thenReturn(Optional.empty());

        // Perform a PUT request to update the playlist with the updated payload and valid authorization header
        mockMvc.perform(MockMvcRequestBuilders.put("/songs/playlists/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(updatedPayload))
                        .header("Authorization", "ValidTokenHere")) // Replace with a valid token
                .andExpect(MockMvcResultMatchers.status().isBadRequest()); // Expecting a 400 Bad Request status code
    }


    @Test
    public void testSetSongs() {
        // Create a set of mock Song objects for testing
        Song song1 = mock(Song.class);
        Song song2 = mock(Song.class);
        Song song3 = mock(Song.class);

        // Create a set of songs
        Set<Song> songs = new HashSet<>();
        songs.add(song1);
        songs.add(song2);
        songs.add(song3);

        // Set the songs in the playlist
        playlist.setSongs(songs);

        // Verify that the songs field in the playlist has been correctly set
        assertEquals(songs, playlist.getSongs());
    }

    @Test
    public void testIsPrivate() throws Exception {
        int playlistId = 1;

        // Mock the behavior of the playlistRepository to return a Playlist with isPrivate true
        Playlist existingPlaylist = new Playlist.Builder()
                .isPrivate(true)
                .name("Old Playlist Name")
                .user("CurrentUser123") // Same owner as the current user
                .songs(new HashSet<>()) // Add songs if needed
                .build();
        Mockito.when(playlistRepository.findById(1)).thenReturn(Optional.of(existingPlaylist));

        mockMvc.perform(MockMvcRequestBuilders.get("/songs/playlists/isPrivate/{id}", playlistId))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("true"));
    }


    @Test
    public void testIsPlaylistExistById() throws Exception {
        int playlistId = 1;

        // Mock the behavior of the playlistRepository to return a Playlist with the given ID
        Mockito.when(playlistRepository.existsById(playlistId)).thenReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.get("/songs/playlists/isPlaylistExistById/{id}", playlistId))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("true"));
    }

    @Test
    public void testGetOwner() throws Exception {
        int playlistId = 1;

        // Mock the behavior of the playlistRepository to return a Playlist with a specific user ID
        Playlist existingPlaylist = new Playlist.Builder()
                .isPrivate(true)
                .name("Old Playlist Name")
                .user("CurrentUser123") // Same owner as the current user
                .songs(new HashSet<>()) // Add songs if needed
                .build();
        Mockito.when(playlistRepository.findById(1)).thenReturn(Optional.of(existingPlaylist));

        mockMvc.perform(MockMvcRequestBuilders.get("/songs/playlists/getOwner/{id}", playlistId))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("CurrentUser123"));
    }
}

