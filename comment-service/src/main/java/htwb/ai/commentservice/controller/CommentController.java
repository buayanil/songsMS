package htwb.ai.commentservice.controller;

import htwb.ai.commentservice.feignclient.AuthApiService;
import htwb.ai.commentservice.feignclient.SongsApiService;
import htwb.ai.commentservice.model.Comment;
import htwb.ai.commentservice.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/comments")
public class CommentController {

    private final CommentRepository commentRepository;

    @Autowired
    private AuthApiService authApiService;

    @Autowired
    private SongsApiService songsApiService;

    @Autowired
    public CommentController(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    @PostMapping("/songs/{id}")
    public ResponseEntity<Void> createSongComment(@PathVariable("id") int id,
                                                  @RequestHeader("Authorization") String authorizationHeader,
                                                  @RequestBody String commentText) {

        if (authorizationHeader == null || authorizationHeader.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if(!songsApiService.isSongExistById(id)){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        if (authApiService.validateToken(authorizationHeader)) {
            // Extract userId from the authorization header using AuthServiceClient
            String userId = authApiService.getCurrentUser(authorizationHeader);

            // Create a new Comment object with "song" as commentType and provided data
            Comment comment = new Comment(userId, "song", id, commentText);

            // Save the comment to the database using CommentRepository
            commentRepository.save(comment);

            // Return a ResponseEntity with a 204 No Content status code
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/playlists/{id}")
    public ResponseEntity<Void> createPlaylistComment(@PathVariable("id") int id,
                                                      @RequestHeader("Authorization") String authorizationHeader,
                                                      @RequestBody String commentText) {

        if (authorizationHeader == null || authorizationHeader.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if(!songsApiService.isPlaylistExistById(id)){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        if (authApiService.validateToken(authorizationHeader)){
            if(songsApiService.isPrivate(id)==false||authApiService.getCurrentUser(authorizationHeader).equals(songsApiService.getOwner(id))){
                // Extract userId from the authorization header using AuthServiceClient
                String userId = authApiService.getCurrentUser(authorizationHeader);

                // Create a new Comment object with "playlist" as commentType and provided data
                Comment comment = new Comment(userId, "playlist", id, commentText);

                // Save the comment to the database using CommentRepository
                commentRepository.save(comment);

                // Return a ResponseEntity with a 204 No Content status code
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping("/songs/{id}")
    public ResponseEntity<String> getCommentsBySongId(@PathVariable("id") int id, @RequestHeader("Authorization") String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<Comment> comments = commentRepository.findByCommentTypeAndReferencedItemId("song", id);
        if (comments.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(comments.toString());
    }

    @GetMapping("/playlists/{id}")
    public ResponseEntity<String> getCommentsByPlaylistId(@PathVariable("id") int id, @RequestHeader("Authorization") String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if(songsApiService.isPrivate(id)==false||authApiService.getCurrentUser(authorizationHeader).equals(songsApiService.getOwner(id))) {
            List<Comment> comments = commentRepository.findByCommentTypeAndReferencedItemId("playlist", id);
            if (comments.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(comments.toString());
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateComment(@PathVariable("id") String id,
                                              @RequestHeader("Authorization") String authorizationHeader,
                                              @RequestBody String updatedCommentText) {

        if (authorizationHeader == null || authorizationHeader.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<Comment> optionalComment = commentRepository.findById(id);

        if (!optionalComment.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Comment comment = optionalComment.get();

        if (!authApiService.validateToken(authorizationHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String userId = authApiService.getCurrentUser(authorizationHeader);

        // Check if the user is the owner of the comment
        if (!userId.equals(comment.getUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Update the comment text
        comment.setCommentText(updatedCommentText);
        commentRepository.save(comment);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable("id") String id,
                                              @RequestHeader("Authorization") String authorizationHeader) {

        if (authorizationHeader == null || authorizationHeader.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<Comment> optionalComment = commentRepository.findById(id);

        if (!optionalComment.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Comment comment = optionalComment.get();

        if (!authApiService.validateToken(authorizationHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String userId = authApiService.getCurrentUser(authorizationHeader);

        if (comment.getCommentType().equals("playlist")) {
            // Check if the user is the owner of the playlist or the owner of the comment
            if (!userId.equals(songsApiService.getOwner(comment.getReferencedItemId())) &&
                    !userId.equals(comment.getUserId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } else if (comment.getCommentType().equals("song")) {
            // For song comments, only the owner of the comment can delete it
            if (!userId.equals(comment.getUserId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        // If the ownership conditions are met, delete the comment
        commentRepository.delete(comment);

        return ResponseEntity.noContent().build();
    }



}
