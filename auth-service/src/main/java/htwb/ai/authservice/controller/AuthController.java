package htwb.ai.authservice.controller;

import htwb.ai.authservice.exception.PasswordWrongException;
import htwb.ai.authservice.exception.UserNotFoundException;
import htwb.ai.authservice.model.User;
import htwb.ai.authservice.repository.UserRepository;
import htwb.ai.authservice.request.AuthRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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

    @RequestMapping(value="/auths", method=RequestMethod.GET, produces = "text/plain;charset=UTF-8")
    public Iterable<User> getAllUsers() {
        return userRepo.findAll();
    }

    // http://localhost:8080/auth?userId=maxime
    @RequestMapping(value="/auth", method=RequestMethod.POST, produces = { "application/json", "text/json", "text/plain;charset=UTF-8" })
    public ResponseEntity<String> returnUser(@RequestParam("userId") String userId, @RequestParam("password") String password) {
        User user = userRepo.selectUserId(userId);
        String token;


        if (user == null) {
            throw new UserNotFoundException("User", "userId", userId);
        }
        else if (user.getPassword().equals(password)) {
            System.out.println(user);


            boolean isUserInMap = true;
            for (User tmp : mapToken.values()) {
                if (tmp.getUserId().equals(user.getUserId())) {
                    isUserInMap = false;
                    break;
                }
            }

            if (isUserInMap) {
                token = this.nextToken(user); //generateChecksum(user.getUserId());
                User userInserted = mapToken.put(token, new User(user.getUserId(), user.getPassword(), user.getFirstName(), user.getLastName()));
                System.out.println(userInserted);
            }

            return ResponseEntity.ok(this.nextToken(user));
        }
        else {
            throw new PasswordWrongException("User", "password", userId);
        }
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
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            throw new PasswordWrongException("User", "password", user.getUserId());
        }
    }

    @PostMapping
    public ResponseEntity<String> login(@RequestBody AuthRequest authRequest) {
        System.err.println("public ResponseEntity<String> login(@RequestBody AuthRequest authRequest)");
        // Überprüfen Sie die Benutzeranmeldeinformationen (userId und password)
        if (isValidCredentials(authRequest.getUserId(), authRequest.getPassword())) {
            // Generieren Sie den Token für den Benutzer (hier: "qwertyuiiooxd1a245")
            String token = generateChecksum(authRequest.getUserId());
            User userInserted = mapToken.put(token, new User(authRequest.getUserId(), authRequest.getPassword(), authRequest.getFirstName(), authRequest.getLastName()));
            System.out.println(userInserted);

            for (User tmpUser : mapToken.values()) {
                System.out.println(tmpUser);
            }

            return ResponseEntity.ok(token);
        } else {
            throw new PasswordWrongException("User", "password", authRequest.getUserId());
            //return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping("/token")
    public ResponseEntity<String> exampleEndpoint(@RequestHeader("Authorization") String authorizationHeader) {
        if (authorizationHeader != null && !authorizationHeader.isEmpty()) {
            // Token aus dem Authorization-Header extrahieren
            String token = authorizationHeader;

            // Überprüfe, ob das Token in der ConcurrentHashMap vorhanden ist
            User user = mapToken.get(token);
            if (user != null) {
                // Token ist gültig, füge die Logik hier hinzu...
                return ResponseEntity.ok("Benutzer " + user.getUserId() + " hat Zugriff.");
            }
        }

        // Der Benutzer ist nicht authentifiziert oder das Token ist ungültig
        // Führen Sie Ihre Fehlerbehandlung durch...
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Nicht autorisiert.");
    }

    public boolean checkToken(String token) {
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

    private boolean isValidCredentials(String userId, String password) {
        // Überprüfen Sie die Anmeldeinformationen des Benutzers gegen Ihre Datenbank oder einen anderen Mechanismus
        // Zum Beispiel können Sie eine Liste von Benutzern und deren Passwörtern haben
        // und überprüfen, ob die eingegebenen Anmeldeinformationen übereinstimmen

        // Beispiel: Benutzer "maxime" mit Passwort "pass1234" ist gültig
        User tmpUser = userRepo.selectUserId(userId);
        if (tmpUser != null) {
            if (tmpUser.getPassword().equals(password)) {
                System.out.println("true: "+tmpUser);
                return true;
            }
            else {
                System.out.println("false: "+tmpUser);
                return false;
            }
        }
        else {
            System.out.println("false: "+tmpUser);
            // Wenn die Anmeldeinformationen nicht übereinstimmen, geben Sie false zurück
            return false;
        }
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

    @GetMapping("/message")
    public String test() {
        return "Hello JavaInUse Called in Second Service";
    }

}