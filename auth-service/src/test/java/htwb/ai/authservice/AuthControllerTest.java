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

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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

		mockMvc.perform(post("/auth")
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

		mockMvc.perform(post("/auth")
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

		mockMvc.perform(post("/auth")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestBody))
				.andExpect(status().isUnauthorized());// Assuming PasswordWrongException returns a 400 Bad Request
	}

	private void setMapTokenField(Map<String, User> newMap) throws Exception {
		Field mapTokenField = AuthController.class.getDeclaredField("mapToken");
		mapTokenField.setAccessible(true);
		mapTokenField.set(userController, newMap);
	}

	private void restoreOriginalMapTokenField() throws Exception {
		Field mapTokenField = AuthController.class.getDeclaredField("mapToken");
		mapTokenField.setAccessible(true);
		mapTokenField.set(userController, new ConcurrentHashMap<>());
	}

	@Test
	public void testCheckToken() throws Exception {
		String validToken = "sampleToken"; // This should be a valid token in your mapToken
		String invalidToken = "invalidToken";

		// Simulate the mapToken with a valid token for testing
		Map<String, User> mapToken = new ConcurrentHashMap<>();
		User sampleUser = new User("sampleUserId", "samplePassword", "Sample", "User");
		mapToken.put(validToken, sampleUser);

		setMapTokenField(mapToken);

		// Test the checkToken endpoint with valid and invalid tokens
		mockMvc.perform(get("/auth/checkToken/" + validToken))
				.andExpect(status().isOk())
				.andExpect(content().string("true"));

		mockMvc.perform(get("/auth/checkToken/" + invalidToken))
				.andExpect(status().isOk())
				.andExpect(content().string("false"));

		// Restore the original mapToken field state
		restoreOriginalMapTokenField();
	}

	@Test
	public void testCheckUser() throws Exception {
		String existingUserId = "sampleUserId"; // A user ID that should exist in your mocked data
		String nonExistingUserId = "nonExistingUser"; // A user ID that should not exist in your mocked data

		when(userRepository.findAll()).thenReturn(Arrays.asList(new User(existingUserId, "password", "Sample", "User")));

		mockMvc.perform(get("/auth/checkUser/" + existingUserId))
				.andExpect(status().isOk())
				.andExpect(content().string("true"));

		mockMvc.perform(get("/auth/checkUser/" + nonExistingUserId))
				.andExpect(status().isOk())
				.andExpect(content().string("false"));
	}

	@Test
	public void testGetCurrentUser() throws Exception {
		String validToken = "sampleToken"; // This should be a valid token in your mapToken
		String invalidToken = "invalidToken";

		when(userRepository.selectUserId("sampleUserId")).thenReturn(new User("sampleUserId", "samplePassword", "Sample", "User"));

		// Simulate the mapToken with a valid token for testing
		Field mapTokenField = AuthController.class.getDeclaredField("mapToken");
		mapTokenField.setAccessible(true);
		ConcurrentHashMap<String, User> mapToken = new ConcurrentHashMap<>();
		User sampleUser = new User("sampleUserId", "samplePassword", "Sample", "User");
		mapToken.put(validToken, sampleUser);
		mapTokenField.set(userController, mapToken);

		// Test the getCurrentUser endpoint with valid and invalid tokens
		mockMvc.perform(get("/auth/getCurrentUser/Bearer " + validToken))
				.andExpect(status().isOk())
				.andExpect(content().string("sampleUserId"));

		mockMvc.perform(get("/auth/getCurrentUser/Bearer " + invalidToken))
				.andExpect(status().isOk())
				.andExpect(content().string("")); // Adjust this based on your controller logic
	}




}

