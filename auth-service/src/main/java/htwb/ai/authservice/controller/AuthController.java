package htwb.ai.authservice.controller;

import htwb.ai.authservice.exception.UserNotFoundException;
import htwb.ai.authservice.model.User;
import htwb.ai.authservice.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private static ConcurrentHashMap<String, User> mapToken = new ConcurrentHashMap<>();
    private final UserRepository userRepo;
    public AuthController (UserRepository repo) {
        this.userRepo = repo;
    }

    @PostMapping(value = "/auth", consumes = { "application/json", "text/json", "text/plain;charset=UTF-8" }, produces = { "application/json", "text/json", "text/plain;charset=UTF-8" })
    public ResponseEntity<String> authUser(@RequestBody User user, HttpServletResponse response) {
        System.err.println("public ResponseEntity<String> authUser(@RequestBody User user, HttpServletResponse response)");
        User tmpUser = userRepo.selectUserId(user.getUserId());
        String token;
        boolean isUserInMap = true;

        if (tmpUser == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            throw new UserNotFoundException("User", "userId", user.getUserId());
        }
        else if (user.getPassword().equals(tmpUser.getPassword())) {
            System.out.println(user);


            for (User tmp : mapToken.values()) {
                if (tmp.getUserId().equals(user.getUserId())) {
                    System.out.printf("### USER FOUND: %s ###\n", tmp.getUserId());
                    isUserInMap = false;
                    break;
                }
            }

            if (isUserInMap) {
                token = this.nextToken(user);//generateChecksum(user.getUserId());
                User userInserted = mapToken.put(token, new User(user.getUserId(), user.getPassword(), user.getFirstName(), user.getLastName()));
                System.out.printf("### userInserted: %s ###\n", userInserted);
                System.out.printf("### userToken: %s ###\n", token);
            }


            return ResponseEntity.ok(this.nextToken(user));
        }
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping("/checkToken/{token}")
    public boolean checkToken(@PathVariable String token) {
        return this.checkMap(token);
    }

    private boolean checkMap(String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7).trim(); // Extract token value without "Bearer" prefix
        }
        return mapToken.containsKey(token);
    }

    public boolean checkUser(String userId){
        Iterable<User> users = userRepo.findAll();
        List<User> userList = new ArrayList<>();
        users.forEach(userList::add);
        for (User user : userList) {
            if (user.getUserId().equals(userId)) {
                return true;
            }
        }
        System.out.println("User not found!");
        return false;
    }

    public User getCurrentUser(String token) {
        System.out.printf("TOKEN: %s%n", token);
        System.out.printf("HashMapEmtpy: %s%n", mapToken.isEmpty());
        // Extract the token value without the "Bearer" prefix
        if (token.startsWith("Bearer ")) {
            token = token.substring(7).trim(); // Extract token value without "Bearer" prefix
        }
        for (String tmp : mapToken.keySet()) {
            User user = mapToken.get(tmp);
            System.out.printf("TOKENMAP: %s, USER: %s%n", tmp, user);
        }
        User user = mapToken.get(token);
        System.out.printf("USER: %s%n", user);
        if (user != null) {
            return user;
        }
        return mapToken.get(token);
    }

    private String nextToken(User user) {
        String tmp;
        tmp = generateChecksum(user.getUserId() + user.getPassword());
        System.out.println(tmp);
        return tmp;
    }

    private static String generateChecksum(String input) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            System.err.println("MD5-Algorithmus nicht gefunden!");
            System.exit(1);
        }

        // Konvertiere den String in ein Byte-Array
        byte[] bytes = input.getBytes(StandardCharsets.UTF_8);

        // Berechne den Hashwert des Byte-Arrays
        byte[] hash = digest.digest(bytes);

        // Konvertiere den Hash in einen hexadezimalen String
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }

        // Gib den generierten Hex-String zurück
        return hexString.toString();
    }

}