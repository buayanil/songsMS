package htwb.ai.commentservice.feignclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "songs-service")
public interface SongsServiceClient {
    @GetMapping("/songs/playlists/isPrivate/{id}")
    boolean isPrivate(@PathVariable("id") int id);

    @GetMapping("/songs/playlists/getOwner/{id}")
    String getOwner(@PathVariable("id") int id);

    @GetMapping("/songs/isSongExistById/{id}")
    boolean isSongExistById(@PathVariable("id")int id);

    @GetMapping("/songs/playlists/isPlaylistExistById/{id}")
    boolean isPlaylistExistById(@PathVariable("id")int id);

}
