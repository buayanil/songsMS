package htwb.ai.commentservice.repository;

import htwb.ai.commentservice.model.Comment;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface CommentRepository extends MongoRepository<Comment, String> {
    List<Comment> findByCommentTypeAndReferencedItemId(String commentType, int referencedItemId);
}

