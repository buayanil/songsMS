package htwb.ai.commentservice.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "comment")
public class Comment {

    @Id // Mark the custom identifier field
    private String commentId; // Custom identifier field

    private String userId;
    private String commentType; // String to represent comment type (e.g., "song", "playlist")
    private int referencedItemId; // ID of the song/playlist
    private String commentText;

    public Comment(String userId, String commentType, int referencedItemId, String commentText) {
        this.userId = userId;
        this.commentType = commentType;
        this.referencedItemId = referencedItemId;
        this.commentText = commentText;
    }

    // Getters and setters


    public int getReferencedItemId() {
        return referencedItemId;
    }

    public String getCommentType() {
        return commentType;
    }

    public String getUserId() {
        return userId;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "commentId='" + commentId + '\'' +
                ", userId='" + userId + '\'' +
                ", commentText='" + commentText + '\'' +
                '}';
    }
}

