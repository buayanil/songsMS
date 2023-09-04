package htwb.ai.commentservice;

import htwb.ai.commentservice.controller.CommentController;
import htwb.ai.commentservice.feignclient.AuthApiService;
import htwb.ai.commentservice.feignclient.SongsApiService;
import htwb.ai.commentservice.model.Comment;
import htwb.ai.commentservice.repository.CommentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

public class CommentControllerTest {

    @InjectMocks
    private CommentController commentController;

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private AuthApiService authApiService;

    @Mock
    private SongsApiService songsApiService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        commentController = new CommentController(commentRepository, authApiService, songsApiService);
    }

    @Test
    public void testGetCommentsBySongId() {
        // Mock the behavior of CommentRepository
        int songId = 1; // Replace with a valid song ID
        List<Comment> mockComments = new ArrayList<>();
        // Add some mock comments to the list
        mockComments.add(new Comment("user1", "song", songId, "Comment 1"));
        mockComments.add(new Comment("user2", "song", songId, "Comment 2"));

        // Define the expected behavior of the CommentRepository when findByCommentTypeAndReferencedItemId is called
        when(commentRepository.findByCommentTypeAndReferencedItemId("song", songId)).thenReturn(mockComments);

        // Call the controller method
        ResponseEntity<String> responseEntity = commentController.getCommentsBySongId(songId, "AuthorizationHeader");

        // Verify the response
        assert responseEntity.getStatusCodeValue() == 200; // HTTP 200 OK
        assert responseEntity.getBody().contains("Comment 1");
        assert responseEntity.getBody().contains("Comment 2");

        // Verify that CommentRepository's findByCommentTypeAndReferencedItemId method was called
        verify(commentRepository, times(2)).findByCommentTypeAndReferencedItemId("song", songId);
    }

    @Test
    public void testCreateSongComment() {
        // Mocking necessary dependencies and data
        int songId = 1;
        String authorizationHeader = "Bearer token";
        String commentText = "Test comment";
        Comment comment = new Comment("userId", "song", songId, commentText);

        when(songsApiService.isSongExistById(songId)).thenReturn(true);
        when(authApiService.validateToken(authorizationHeader)).thenReturn(true);
        when(authApiService.getCurrentUser(authorizationHeader)).thenReturn("userId");


        // Perform the test
        ResponseEntity<Void> response = commentController.createSongComment(songId, authorizationHeader, commentText);

        assert response.getStatusCode() == HttpStatus.NO_CONTENT;
    }

    @Test
    public void testCreatePlaylistComment() {
        int playlistId = 1;
        String authorizationHeader = "Bearer token";
        String commentText = "Test comment";
        Comment comment = new Comment("userId", "playlist", playlistId, commentText);

        when(songsApiService.isPlaylistExistById(playlistId)).thenReturn(true);
        when(authApiService.validateToken(authorizationHeader)).thenReturn(true);
        when(authApiService.getCurrentUser(authorizationHeader)).thenReturn("userId");

        // Perform the test
        ResponseEntity<Void> response = commentController.createPlaylistComment(playlistId, authorizationHeader, commentText);

        assert response.getStatusCode() == HttpStatus.NO_CONTENT;
    }

    @Test
    public void testUpdateComment() {
        String commentId = "1"; // Replace with a valid comment ID
        String authorizationHeader = "Bearer token";
        String updatedCommentText = "Updated comment text";

        Comment comment = new Comment("userId", "song", 1, "Original comment text");

        when(authApiService.validateToken(authorizationHeader)).thenReturn(true);
        when(authApiService.getCurrentUser(authorizationHeader)).thenReturn("userId");
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        // Perform the test
        ResponseEntity<Void> response = commentController.updateComment(commentId, authorizationHeader, updatedCommentText);

        assert response.getStatusCode() == HttpStatus.NO_CONTENT;
    }

    @Test
    public void testDeleteComment() {
        String commentId = "1"; // Replace with a valid comment ID
        String authorizationHeader = "Bearer token";

        Comment comment = new Comment("userId", "song", 1, "Comment text");

        when(authApiService.validateToken(authorizationHeader)).thenReturn(true);
        when(authApiService.getCurrentUser(authorizationHeader)).thenReturn("userId");
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        // Perform the test
        ResponseEntity<Void> response = commentController.deleteComment(commentId, authorizationHeader);

        assert response.getStatusCode() == HttpStatus.NO_CONTENT;
        verify(commentRepository, times(1)).delete(comment);
    }

    @Test
    public void testCreatePlaylistComment_Unauthorized() {
        int playlistId = 1;
        String authorizationHeader = "";  // Empty authorization header

        ResponseEntity<Void> response = commentController.createPlaylistComment(playlistId, authorizationHeader, "Test comment");

        assert response.getStatusCode() == HttpStatus.UNAUTHORIZED;
    }

    @Test
    public void testCreatePlaylistComment_NotFound() {
        int playlistId = 1;
        String authorizationHeader = "Bearer token";
        String commentText = "Test comment";

        when(songsApiService.isPlaylistExistById(playlistId)).thenReturn(false);

        ResponseEntity<Void> response = commentController.createPlaylistComment(playlistId, authorizationHeader, commentText);

        assert response.getStatusCode() == HttpStatus.NOT_FOUND;
    }

    @Test
    public void testCreatePlaylistComment_Forbidden() {
        int playlistId = 1;
        String authorizationHeader = "Bearer token";
        String commentText = "Test comment";

        when(songsApiService.isPlaylistExistById(playlistId)).thenReturn(true);
        when(authApiService.validateToken(authorizationHeader)).thenReturn(true);
        when(authApiService.getCurrentUser(authorizationHeader)).thenReturn("otherUser");
        when(songsApiService.isPrivate(playlistId)).thenReturn(true);
        when(songsApiService.getOwner(playlistId)).thenReturn("ownerUser");

        ResponseEntity<Void> response = commentController.createPlaylistComment(playlistId, authorizationHeader, commentText);

        assert response.getStatusCode() == HttpStatus.FORBIDDEN;
    }

    @Test
    public void testUpdateComment_Unauthorized() {
        String commentId = "1"; // Replace with a valid comment ID
        String authorizationHeader = "";  // Empty authorization header

        ResponseEntity<Void> response = commentController.updateComment(commentId, authorizationHeader, "Updated comment text");

        assert response.getStatusCode() == HttpStatus.UNAUTHORIZED;
    }

    @Test
    public void testUpdateComment_NotFound() {
        String commentId = "1"; // Replace with a valid comment ID
        String authorizationHeader = "Bearer token";
        String updatedCommentText = "Updated comment text";

        when(authApiService.validateToken(authorizationHeader)).thenReturn(true);
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        ResponseEntity<Void> response = commentController.updateComment(commentId, authorizationHeader, updatedCommentText);

        assert response.getStatusCode() == HttpStatus.NOT_FOUND;
    }

    @Test
    public void testUpdateComment_Forbidden() {
        String commentId = "1"; // Replace with a valid comment ID
        String authorizationHeader = "Bearer token";
        String updatedCommentText = "Updated comment text";

        Comment comment = new Comment("otherUser", "song", 1, "Original comment text");

        when(authApiService.validateToken(authorizationHeader)).thenReturn(true);
        when(authApiService.getCurrentUser(authorizationHeader)).thenReturn("userId");
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        ResponseEntity<Void> response = commentController.updateComment(commentId, authorizationHeader, updatedCommentText);

        assert response.getStatusCode() == HttpStatus.FORBIDDEN;
    }

    @Test
    public void testDeleteComment_Unauthorized() {
        String commentId = "1"; // Replace with a valid comment ID
        String authorizationHeader = "";  // Empty authorization header

        ResponseEntity<Void> response = commentController.deleteComment(commentId, authorizationHeader);

        assert response.getStatusCode() == HttpStatus.UNAUTHORIZED;
    }

    @Test
    public void testDeleteComment_NotFound() {
        String commentId = "1"; // Replace with a valid comment ID
        String authorizationHeader = "Bearer token";

        when(authApiService.validateToken(authorizationHeader)).thenReturn(true);
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        ResponseEntity<Void> response = commentController.deleteComment(commentId, authorizationHeader);

        assert response.getStatusCode() == HttpStatus.NOT_FOUND;
    }

    @Test
    public void testDeleteComment_Forbidden_Playlist() {
        String commentId = "1"; // Replace with a valid comment ID
        String authorizationHeader = "Bearer token";

        Comment comment = new Comment("otherUser", "playlist", 1, "Comment text");

        when(authApiService.validateToken(authorizationHeader)).thenReturn(true);
        when(authApiService.getCurrentUser(authorizationHeader)).thenReturn("userId");
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(songsApiService.getOwner(comment.getReferencedItemId())).thenReturn("ownerUser");

        ResponseEntity<Void> response = commentController.deleteComment(commentId, authorizationHeader);

        assert response.getStatusCode() == HttpStatus.FORBIDDEN;
    }

    @Test
    public void testDeleteComment_Forbidden_Song() {
        String commentId = "1"; // Replace with a valid comment ID
        String authorizationHeader = "Bearer token";

        Comment comment = new Comment("otherUser", "song", 1, "Comment text");

        when(authApiService.validateToken(authorizationHeader)).thenReturn(true);
        when(authApiService.getCurrentUser(authorizationHeader)).thenReturn("userId");
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        ResponseEntity<Void> response = commentController.deleteComment(commentId, authorizationHeader);

        assert response.getStatusCode() == HttpStatus.FORBIDDEN;
    }

    @Test
    public void testDeleteComment_Success() {
        String commentId = "1"; // Replace with a valid comment ID
        String authorizationHeader = "Bearer token";

        Comment comment = new Comment("userId", "song", 1, "Comment text");

        when(authApiService.validateToken(authorizationHeader)).thenReturn(true);
        when(authApiService.getCurrentUser(authorizationHeader)).thenReturn("userId");
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        ResponseEntity<Void> response = commentController.deleteComment(commentId, authorizationHeader);

        assert response.getStatusCode() == HttpStatus.NO_CONTENT;
        verify(commentRepository, times(1)).delete(comment);
    }

    @Test
    public void testGetCommentsByPlaylistId_Unauthorized() {
        int playlistId = 1; // Replace with a valid playlist ID
        String authorizationHeader = "";  // Empty authorization header

        ResponseEntity<String> response = commentController.getCommentsByPlaylistId(playlistId, authorizationHeader);

        assert response.getStatusCode() == HttpStatus.UNAUTHORIZED;
    }

    @Test
    public void testGetCommentsByPlaylistId_NotFound() {
        int playlistId = 1; // Replace with a valid playlist ID
        String authorizationHeader = "Bearer token";

        // Mock a valid user ID and a valid authorization token
        when(authApiService.validateToken(authorizationHeader)).thenReturn(true);
        when(authApiService.getCurrentUser(authorizationHeader)).thenReturn("userId"); // Mocked user ID

        // Mock that the playlist does not exist
        when(songsApiService.isPrivate(playlistId)).thenReturn(true); // Assuming the playlist does not exist

        when(songsApiService.getOwner(playlistId)).thenReturn("userId");

        // Call the controller method
        ResponseEntity<String> response = commentController.getCommentsByPlaylistId(playlistId, authorizationHeader);

        // Verify the response code
        assert response.getStatusCode() == HttpStatus.NOT_FOUND : "Expected HttpStatus.NOT_FOUND but got " + response.getStatusCode();
    }



    @Test
    public void testGetCommentsByPlaylistId_Forbidden() {
        int playlistId = 1; // Replace with a valid playlist ID
        String authorizationHeader = "Bearer token";

        when(authApiService.validateToken(authorizationHeader)).thenReturn(true);
        when(songsApiService.isPrivate(playlistId)).thenReturn(true); // Assuming the playlist is private
        when(authApiService.getCurrentUser(authorizationHeader)).thenReturn("otherUser");
        when(songsApiService.getOwner(playlistId)).thenReturn("ownerUser");

        ResponseEntity<String> response = commentController.getCommentsByPlaylistId(playlistId, authorizationHeader);

        assert response.getStatusCode() == HttpStatus.FORBIDDEN;
    }

    @Test
    public void testGetCommentsByPlaylistId_Success_Public() {
        int playlistId = 1; // Replace with a valid playlist ID
        String authorizationHeader = "Bearer token";

        when(authApiService.validateToken(authorizationHeader)).thenReturn(true);
        when(songsApiService.isPrivate(playlistId)).thenReturn(false); // Assuming the playlist is public
        List<Comment> comments = new ArrayList<>();
        comments.add(new Comment("user1", "playlist", playlistId, "Comment 1"));
        comments.add(new Comment("user2", "playlist", playlistId, "Comment 2"));
        when(commentRepository.findByCommentTypeAndReferencedItemId("playlist", playlistId)).thenReturn(comments);

        ResponseEntity<String> response = commentController.getCommentsByPlaylistId(playlistId, authorizationHeader);

        assert response.getStatusCode() == HttpStatus.OK;
        assert response.getBody().contains("Comment 1");
        assert response.getBody().contains("Comment 2");
    }

    @Test
    public void testCreateSongComment_NullAuthorizationHeader() {
        int songId = 1; // Replace with a valid song ID
        String authorizationHeader = null;  // Null authorization header
        String commentText = "Test comment";

        // Call the controller method
        ResponseEntity<Void> response = commentController.createSongComment(songId, authorizationHeader, commentText);

        // Verify the response code
        assert response.getStatusCode() == HttpStatus.UNAUTHORIZED;
    }

    @Test
    public void testCreateSongComment_EmptyAuthorizationHeader() {
        int songId = 1; // Replace with a valid song ID
        String authorizationHeader = "";  // Empty authorization header
        String commentText = "Test comment";

        // Call the controller method
        ResponseEntity<Void> response = commentController.createSongComment(songId, authorizationHeader, commentText);

        // Verify the response code
        assert response.getStatusCode() == HttpStatus.UNAUTHORIZED;
    }

    @Test
    public void testCreateSongComment_SongNotFound() {
        int songId = 1; // Replace with a valid song ID
        String authorizationHeader = "Bearer token";
        String commentText = "Test comment";

        // Mock that the song does not exist
        when(songsApiService.isSongExistById(songId)).thenReturn(false);

        // Call the controller method
        ResponseEntity<Void> response = commentController.createSongComment(songId, authorizationHeader, commentText);

        // Verify the response code
        assert response.getStatusCode() == HttpStatus.NOT_FOUND;
    }

    @Test
    public void testCreateSongComment_UnauthorizedToken() {
        int songId = 1; // Replace with a valid song ID
        String authorizationHeader = "Bearer invalid_token"; // Invalid or unauthorized token
        String commentText = "Test comment";

        // Mock that the token is not valid
        when(authApiService.validateToken(authorizationHeader)).thenReturn(false);
        when(songsApiService.isSongExistById(songId)).thenReturn(true);

        // Call the controller method
        ResponseEntity<Void> response = commentController.createSongComment(songId, authorizationHeader, commentText);

        // Verify the response code
        assert response.getStatusCode() == HttpStatus.UNAUTHORIZED : "Expected HttpStatus.UNAUTHORIZED but got " + response.getStatusCodeValue();
    }


    @Test
    public void testCreatePlaylistComment_UnauthorizedToken() {
        int songId = 1; // Replace with a valid song ID
        String authorizationHeader = "Bearer invalid_token"; // Invalid or unauthorized token
        String commentText = "Test comment";

        // Mock that the token is not valid
        when(authApiService.validateToken(authorizationHeader)).thenReturn(false);
        when(songsApiService.isPlaylistExistById(songId)).thenReturn(true);

        // Call the controller method
        ResponseEntity<Void> response = commentController.createPlaylistComment(songId, authorizationHeader, commentText);

        // Verify the response code
        assert response.getStatusCode() == HttpStatus.UNAUTHORIZED : "Expected HttpStatus.UNAUTHORIZED but got " + response.getStatusCodeValue();
    }

    @Test
    public void testUpdateComment_UnauthorizedToken() {
        String commentId = "1"; // Replace with a valid comment ID
        String authorizationHeader = "Bearer invalid_token"; // Invalid or unauthorized token
        String updatedCommentText = "Updated comment text";

        // Mock that the token is not valid
        when(authApiService.validateToken(authorizationHeader)).thenReturn(false);

        // Mock the existence of the comment with the specified ID
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(new Comment("userId", "song", 1, "Sample comment")));

        // Call the controller method
        ResponseEntity<Void> response = commentController.updateComment(commentId, authorizationHeader, updatedCommentText);

        // Verify the response code
        assert response.getStatusCode() == HttpStatus.UNAUTHORIZED : "Expected HttpStatus.UNAUTHORIZED but got " + response.getStatusCodeValue();
    }



}


