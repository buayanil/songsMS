package htwb.ai.commentservice.repository;

import htwb.ai.commentservice.model.Comment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends MongoRepository<Comment, String> {
    List<Comment> findByCommentTypeAndReferencedItemId(String commentType, int referencedItemId);
}

