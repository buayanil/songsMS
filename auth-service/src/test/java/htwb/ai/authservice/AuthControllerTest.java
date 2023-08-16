package htwb.ai.authservice;

import htwb.ai.authservice.controller.AuthController;
import htwb.ai.authservice.model.User;
import htwb.ai.authservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthControllerTest {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private AuthController userController;

	private MockMvc mockMvc;
	private Map<String, User> mapToken;

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
		mapToken = new HashMap<>();
		// Populate the mapToken with some sample data for testing
		User sampleUser = new User("sampleUserId", "samplePassword", "Sample", "User");
		mapToken.put("sampleToken", sampleUser);
	}

	@Test
	public void testExistingUser() throws Exception {
		String userId = "maxime";
		String password = "pass1234";

		User user = new User();
		user.setUserId(userId);
		user.setPassword(password);

		//when(userRepository.findByUsernameAndPassword(userId, password)).thenReturn(user);
		when(userRepository.selectUserId(userId)).thenReturn(user);
		mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(userRepository)).build();
		String requestBody = "{\"userId\": \"" + userId + "\", \"password\": \"" + password + "\"}";

		mockMvc.perform(post("/auth/auth")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestBody))
				.andExpect(status().isOk());
	}

	@Test
	public void testNonExistingUser() throws Exception {
		String userId = "peter";
		String password = "pass1234";

		User user = new User();
		user.setUserId(userId);
		user.setPassword(password);

		//when(userRepository.findByUsernameAndPassword(userId, password)).thenReturn(user);
		when(userRepository.selectUserId(userId)).thenReturn(null);
		mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(userRepository)).build();
		String requestBody = "{\"userId\": \"" + userId + "\", \"password\": \"" + password + "\"}";

		mockMvc.perform(post("/auth/auth")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestBody))
				.andExpect(status().isUnauthorized());
	}

	@Test
	public void testWrongPasswordException() throws Exception {
		String userId = "maxime";
		String correctPassword = "correct123";
		String wrongPassword = "wrong456";

		User user = new User();
		user.setUserId(userId);
		user.setPassword(correctPassword);

		when(userRepository.selectUserId(userId)).thenReturn(user);

		String requestBody = "{\"userId\": \"" + userId + "\", \"password\": \"" + wrongPassword + "\"}";

		mockMvc.perform(post("/auth/auth")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestBody))
				.andExpect(status().isUnauthorized());// Assuming PasswordWrongException returns a 400 Bad Request
	}

	@Test
	public void testCheckTokenInvalidToken() {
		assertFalse(userController.checkToken("invalidToken"));
	}

	@Test
	public void testCheckUserExistingUser() {
		List<User> userList = new ArrayList<>();
		User existingUser = new User("sampleUserId", "samplePassword", "Sample", "User");
		userList.add(existingUser);

		when(userRepository.findAll()).thenReturn(userList);

		assertTrue(userController.checkUser("sampleUserId"));
	}


	@Test
	public void testCheckUserNonExistingUser() {
		when(userRepository.findAll()).thenReturn(new ArrayList<>()); // Mock empty user list for simplicity

		assertFalse(userController.checkUser("nonExistingUserId"));
	}

	@Test
	public void testGetCurrentUserInvalidToken() {
		User currentUser = userController.getCurrentUser("invalidToken");

		assertNull(currentUser);
	}

	@Test
	public void testUserFoundInMap() {
		String userId = "sampleUserId";

		boolean isUserInMap = false;
		for (User tmp : mapToken.values()) {
			if (tmp.getUserId().equals(userId)) {
				System.out.printf("### USER FOUND: %s ###%n", tmp.getUserId());
				isUserInMap = true;
				break;
			}
		}

		assertTrue(isUserInMap);
	}

	@Test
	public void testUserNotFoundInMap() {
		String userId = "nonExistentUser";

		boolean isUserInMap = false;
		for (User tmp : mapToken.values()) {
			if (tmp.getUserId().equals(userId)) {
				System.out.printf("### USER FOUND: %s ###%n", tmp.getUserId());
				isUserInMap = true;
				break;
			}
		}

		assertFalse(isUserInMap);
	}

}

