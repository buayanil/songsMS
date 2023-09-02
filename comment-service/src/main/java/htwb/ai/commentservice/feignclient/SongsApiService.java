package htwb.ai.commentservice.feignclient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SongsApiService {
    @Autowired
    private SongsServiceClient songsServiceClient;

    public boolean isPrivate(int id){
        return songsServiceClient.isPrivate(id);
    }

    public String getOwner(int id){
        return songsServiceClient.getOwner(id);
    }

    public boolean isSongExistById(int id){
        return songsServiceClient.isSongExistById(id);
    }

    public boolean isPlaylistExistById(int id){
        return songsServiceClient.isPlaylistExistById(id);
    }
}
