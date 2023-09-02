package htwb.ai.commentservice.feignclient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthApiService {
    @Autowired
    private AuthServiceClient authServiceClient;

    public boolean validateToken(String token) {
        return authServiceClient.checkToken(token);
    }

    public String getCurrentUser(String authorizationHeader){
        return  authServiceClient.getCurrentUser(authorizationHeader);
    }

    public boolean checkUser(String userId){
        return authServiceClient.checkUser(userId);
    }
}
