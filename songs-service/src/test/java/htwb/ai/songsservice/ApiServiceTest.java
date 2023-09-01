package htwb.ai.songsservice;

import htwb.ai.songsservice.feignclient.ApiService;
import htwb.ai.songsservice.feignclient.AuthServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class ApiServiceTest {

    @Mock
    private AuthServiceClient authServiceClient;

    @InjectMocks
    private ApiService apiService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testValidateToken_ValidToken() {
        when(authServiceClient.checkToken(anyString())).thenReturn(true);

        boolean result = apiService.validateToken("validToken");

        assertTrue(result);
    }

    @Test
    public void testValidateToken_InvalidToken() {
        when(authServiceClient.checkToken(anyString())).thenReturn(false);

        boolean result = apiService.validateToken("invalidToken");

        assertFalse(result);
    }

    @Test
    public void testGetCurrentUser_ValidToken() {
        String expectedUser = "testUser";
        when(authServiceClient.getCurrentUser(anyString())).thenReturn(expectedUser);

        String result = apiService.getCurrentUser("validToken");

        assertEquals(expectedUser, result);
    }

    @Test
    public void testCheckUser_ExistingUser() {
        when(authServiceClient.checkUser(anyString())).thenReturn(true);

        boolean result = apiService.checkUser("existingUser");

        assertTrue(result);
    }

    @Test
    public void testCheckUser_NonExistingUser() {
        when(authServiceClient.checkUser(anyString())).thenReturn(false);

        boolean result = apiService.checkUser("nonExistingUser");

        assertFalse(result);
    }
}
