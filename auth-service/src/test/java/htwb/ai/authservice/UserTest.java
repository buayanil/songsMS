package htwb.ai.authservice;

import htwb.ai.authservice.model.User;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserTest {

    @Test
    public void testBuilder() {
        User user = new User.Builder()
                .userId("maxime")
                .password("password")
                .firstName("Maxime")
                .lastName("Lastname")
                .build();

        assertEquals("maxime", user.getUserId());
        assertEquals("password", user.getPassword());
        assertEquals("Maxime", user.getFirstName());
        assertEquals("Lastname", user.getLastName());
    }

    @Test
    public void testSettersAndGetters() {
        User user = new User();

        user.setUserId("jane");
        user.setPassword("password");
        user.setFirstName("Jane");
        user.setLastName("Doe");

        assertEquals("jane", user.getUserId());
        assertEquals("password", user.getPassword());
        assertEquals("Jane", user.getFirstName());
        assertEquals("Doe", user.getLastName());
    }

    @Test
    public void testToString() {
        User user = new User("maxime", "password", "Maxime", "Lastname");

        assertEquals("User [userId=maxime, firstName=Maxime, lastName=Lastname, password=password]", user.toString());
    }
}
