package htwb.ai.songsservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import htwb.ai.songsservice.controller.SongController;
import htwb.ai.songsservice.feignclient.ApiService;
import htwb.ai.songsservice.model.Song;
import htwb.ai.songsservice.repository.SongRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class SongControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SongRepository songRepository;

    @MockBean
    private ApiService apiService;

    @Test
    public void testCreateSong_Success() throws Exception {
        Song.Builder songBuilder = new Song.Builder()
                .id(11)
                .title("From The Start")
                .artist("Laufey")
                .album("From The Start")
                .released(2023);

        when(apiService.validateToken(anyString())).thenReturn(true);
        when(songRepository.existsById(anyLong())).thenReturn(false);

        Song mockSong = songBuilder.build(); // Build the Song object

        when(songRepository.save(any(Song.class))).thenReturn(mockSong);

        String requestContent = "{\"id\": 11, \"title\": \"From The Start\", \"artist\": \"Laufey\", \"album\": \"From The Start\", \"released\": 2023}";

        mockMvc.perform(post("/songs")
                        .header("Authorization", "dummyToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestContent))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Song creation successful")))
                .andExpect(content().string(containsString("Created ID: 11")));

        verify(apiService, times(1)).validateToken("dummyToken");
        verify(songRepository, times(1)).existsById(11L);
        verify(songRepository, times(1)).save(any(Song.class));
    }

    @Test
    public void testCreateSong_UnauthorizedToken() throws Exception {
        when(apiService.validateToken(anyString())).thenReturn(false);

        String requestContent = "{\"id\": 11, \"title\": \"From The Start\", \"artist\": \"Laufey\", \"album\": \"From The Start\", \"released\": 2023}";

        mockMvc.perform(post("/songs")
                        .header("Authorization", "invalidToken") // Use an invalid token
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestContent))
                .andExpect(status().isUnauthorized());

        verify(apiService, times(1)).validateToken("invalidToken");
        verifyNoInteractions(songRepository); // No interaction with songRepository expected
    }

    @Test
    public void testCreateSong_SongAlreadyExists() throws Exception {
        Song existingSong = new Song.Builder()
                .id(11)
                .title("Existing Song")
                .artist("Existing Artist")
                .album("Existing Album")
                .released(2020)
                .build();

        when(apiService.validateToken(anyString())).thenReturn(true);
        when(songRepository.existsById(existingSong.getId())).thenReturn(true); // Simulating song already exists

        String requestContent = "{\"id\": 11, \"title\": \"From The Start\", \"artist\": \"Laufey\", \"album\": \"From The Start\", \"released\": 2023}";

        mockMvc.perform(post("/songs")
                        .header("Authorization", "dummyToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestContent))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Song with ID 11 already exists")));

        verify(apiService, times(1)).validateToken("dummyToken");
        verify(songRepository, times(1)).existsById(11L);
    }

    @Test
    public void testCreateSong_MissingOrEmptyToken() throws Exception {
        when(apiService.validateToken(anyString())).thenReturn(false);

        String requestContent = "{\"id\": 11, \"title\": \"From The Start\", \"artist\": \"Laufey\", \"album\": \"From The Start\", \"released\": 2023}";

        mockMvc.perform(post("/songs")
                        .header("Authorization", "") // Empty token
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestContent))
                .andExpect(status().isUnauthorized());

        verify(apiService, never()).validateToken(anyString()); // No token validation
        verifyNoInteractions(songRepository); // No interaction with songRepository expected
    }

    @Test
    public void testGetAllSongs_Success() throws Exception {
        Song mockSong1 = new Song.Builder()
                .id(1)
                .title("Song 1")
                .artist("Artist 1")
                .album("Album 1")
                .released(2021)
                .build();

        Song mockSong2 = new Song.Builder()
                .id(2)
                .title("Song 2")
                .artist("Artist 2")
                .album("Album 2")
                .released(2022)
                .build();

        List<Song> mockSongs = Arrays.asList(mockSong1, mockSong2);

        when(apiService.validateToken(anyString())).thenReturn(true);
        when(songRepository.findAll()).thenReturn(mockSongs);

        mockMvc.perform(get("/songs")
                        .header("Authorization", "dummyToken")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Song 1"))
                .andExpect(jsonPath("$[0].artist").value("Artist 1"))
                .andExpect(jsonPath("$[0].album").value("Album 1"))
                .andExpect(jsonPath("$[0].released").value(2021))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].title").value("Song 2"))
                .andExpect(jsonPath("$[1].artist").value("Artist 2"))
                .andExpect(jsonPath("$[1].album").value("Album 2"))
                .andExpect(jsonPath("$[1].released").value(2022));

        verify(apiService, times(1)).validateToken("dummyToken");
        verify(songRepository, times(1)).findAll();
    }

    @Test
    public void testGetAllSongs_UnauthorizedToken() throws Exception {
        when(apiService.validateToken(anyString())).thenReturn(false);

        mockMvc.perform(get("/songs")
                        .header("Authorization", "invalidToken") // Use an invalid token
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verify(apiService, times(1)).validateToken("invalidToken");
    }

    @Test
    @DisplayName("Get All Songs - Missing or Empty Token")
    public void testGetAllSongs_MissingOrEmptyToken() throws Exception {
        when(apiService.validateToken(anyString())).thenReturn(false);

        mockMvc.perform(get("/songs")
                        .header("Authorization", "")) // Empty token
                .andExpect(status().isUnauthorized());

        verify(apiService, never()).validateToken(anyString()); // No token validation
        verifyNoInteractions(songRepository); // No interaction with songRepository expected
    }

    @Test
    @DisplayName("Get Song by ID - Success")
    public void testGetSongById_Success() throws Exception {
        Song mockSong = new Song.Builder()
                .id(11)
                .title("From The Start")
                .artist("Laufey")
                .album("From The Start")
                .released(2023)
                .build();

        when(apiService.validateToken(anyString())).thenReturn(true);
        when(songRepository.findById(11L)).thenReturn(Optional.of(mockSong));

        mockMvc.perform(get("/songs/{id}", 11)
                        .header("Authorization", "dummyToken"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(11))
                .andExpect(jsonPath("$.title").value("From The Start"))
                .andExpect(jsonPath("$.artist").value("Laufey"))
                .andExpect(jsonPath("$.album").value("From The Start"))
                .andExpect(jsonPath("$.released").value(2023));

        verify(apiService, times(1)).validateToken("dummyToken");
        verify(songRepository, times(1)).findById(11L);
    }

    @Test
    @DisplayName("Get Song by ID - Unauthorized Token")
    public void testGetSongById_UnauthorizedToken() throws Exception {
        when(apiService.validateToken(anyString())).thenReturn(false);

        mockMvc.perform(get("/songs/{id}", 11)
                        .header("Authorization", "invalidToken")) // Use an invalid token
                .andExpect(status().isUnauthorized());

        verify(apiService, times(1)).validateToken("invalidToken");
    }

    @Test
    @DisplayName("Get Song by ID - Missing or Empty Token")
    public void testGetSongById_MissingOrEmptyToken() throws Exception {
        when(apiService.validateToken(anyString())).thenReturn(false);

        mockMvc.perform(get("/songs/{id}", 11)
                        .header("Authorization", "")) // Empty token
                .andExpect(status().isUnauthorized());

        verify(apiService, never()).validateToken(anyString()); // No token validation
        verifyNoInteractions(songRepository); // No interaction with songRepository expected
    }

    @Test
    @DisplayName("Get Song by ID - Song Not Found")
    public void testGetSongById_SongNotFound() throws Exception {
        when(apiService.validateToken(anyString())).thenReturn(true);
        when(songRepository.findById(11L)).thenReturn(Optional.empty()); // Simulate song not found

        mockMvc.perform(get("/songs/{id}", 11)
                        .header("Authorization", "dummyToken"))
                .andExpect(status().isNotFound());

        verify(apiService, times(1)).validateToken("dummyToken");
        verify(songRepository, times(1)).findById(11L);
    }

    @Test
    @DisplayName("Update Song - Success")
    public void testUpdateSong_Success() throws Exception {
        Song existingSong = new Song.Builder()
                .id(11)
                .title("Existing Song")
                .artist("Existing Artist")
                .album("Existing Album")
                .released(2020)
                .build();

        Song updatedSong = new Song.Builder()
                .id(11)
                .title("Updated Song Title")
                .artist("Updated Artist")
                .album("Updated Album")
                .released(2022)
                .build();

        when(apiService.validateToken(anyString())).thenReturn(true);
        when(songRepository.findById(existingSong.getId())).thenReturn(Optional.of(existingSong));
        when(songRepository.save(any(Song.class))).thenReturn(updatedSong);

        String requestContent = "{" +
                "\"id\": \"11\"," +
                "\"title\": \"Updated Song Title\"," +
                "\"artist\": \"Updated Artist\"," +
                "\"album\": \"Updated Album\"," +
                "\"released\": 2022" +
                "}";

        mockMvc.perform(put("/songs/{id}", 11)
                        .header("Authorization", "dummyToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestContent))
                .andExpect(status().isNoContent());

        verify(apiService, times(1)).validateToken("dummyToken");
        verify(songRepository, times(1)).save(any(Song.class));
    }

    @Test
    @DisplayName("Update Song - Unauthorized Token")
    public void testUpdateSong_UnauthorizedToken() throws Exception {
        when(apiService.validateToken(anyString())).thenReturn(false);

        String requestContent = "{" +
                "\"id\": \"11\"," +
                "\"title\": \"Updated Song Title\"," +
                "\"artist\": \"Updated Artist\"," +
                "\"album\": \"Updated Album\"," +
                "\"released\": 2022" +
                "}";

        mockMvc.perform(put("/songs/{id}", 11)
                        .header("Authorization", "invalidToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestContent))
                .andExpect(status().isUnauthorized());

        verify(apiService, times(1)).validateToken("invalidToken");
        verifyNoInteractions(songRepository); // No interaction with songRepository expected
    }

    @Test
    @DisplayName("Update Song - Empty Authorization Header")
    public void testUpdateSong_EmptyAuthorizationHeader() throws Exception {
        when(apiService.validateToken(anyString())).thenReturn(false);

        String requestContent = "{" +
                "\"id\": \"11\"," +
                "\"title\": \"Updated Song Title\"," +
                "\"artist\": \"Updated Artist\"," +
                "\"album\": \"Updated Album\"," +
                "\"released\": 2022" +
                "}";

        mockMvc.perform(put("/songs/{id}", 11)
                        .header("Authorization", "") // Empty Authorization header
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestContent))
                .andExpect(status().isUnauthorized());

        verify(apiService, never()).validateToken(anyString()); // No token validation
        verifyNoInteractions(songRepository); // No interaction with songRepository expected
    }

    @Test
    @DisplayName("Update Song - Song Not Found")
    public void testUpdateSong_SongNotFound() throws Exception {
        Song updatedSong = new Song.Builder()
                .id(11)
                .title("Updated Song Title")
                .artist("Updated Artist")
                .album("Updated Album")
                .released(2022)
                .build();

        when(apiService.validateToken(anyString())).thenReturn(true);

        String requestContent = "{" +
                "\"id\": \"11\"," +
                "\"title\": \"Updated Song Title\"," +
                "\"artist\": \"Updated Artist\"," +
                "\"album\": \"Updated Album\"," +
                "\"released\": 2022" +
                "}";

        mockMvc.perform(put("/songs/{id}", 11)
                        .header("Authorization", "dummyToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestContent))
                .andExpect(status().isNotFound());

        verify(apiService, times(1)).validateToken("dummyToken");
    }

    @Test
    @DisplayName("Update Song - Different id")
    public void testUpdateSong_differentID() throws Exception {
        Song existingSong = new Song.Builder()
                .id(11)
                .title("Existing Song")
                .artist("Existing Artist")
                .album("Existing Album")
                .released(2020)
                .build();

        Song updatedSong = new Song.Builder()
                .id(11)
                .title("Updated Song Title")
                .artist("Updated Artist")
                .album("Updated Album")
                .released(2022)
                .build();

        when(apiService.validateToken(anyString())).thenReturn(true);
        when(songRepository.findById(existingSong.getId())).thenReturn(Optional.of(existingSong));
        when(songRepository.save(any(Song.class))).thenReturn(updatedSong);

        String requestContent = "{" +
                "\"id\": \"1\"," +
                "\"title\": \"Updated Song Title\"," +
                "\"artist\": \"Updated Artist\"," +
                "\"album\": \"Updated Album\"," +
                "\"released\": 2022" +
                "}";

        mockMvc.perform(put("/songs/{id}", 11)
                        .header("Authorization", "dummyToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestContent))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testToString() {
        Song song = new Song.Builder()
                .id(11)
                .title("Test Song")
                .artist("Test Artist")
                .album("Test Album")
                .released(2023)
                .build();

        String expectedToString = "Song [id=11, title=Test Song, artist=Test Artist, album=Test Album, released=2023]";
        String actualToString = song.toString();

        assertEquals(expectedToString, actualToString);
    }



    // Add more test cases for different scenarios
}