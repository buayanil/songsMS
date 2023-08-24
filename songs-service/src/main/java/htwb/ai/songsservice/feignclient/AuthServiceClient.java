package htwb.ai.songsservice.feignclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "auth-service") // Replace with the actual service name
public interface AuthServiceClient {

    @GetMapping("/auth/checkToken/{token}") // Replace with the actual endpoint path
    boolean checkToken(@PathVariable String token);
}

