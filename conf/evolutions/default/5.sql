-- create likes

-- !Ups
CREATE TABLE post_likes
(
    id bigint(20) NOT NULL AUTO_INCREMENT,
    userId bigint(20) NOT NULL,
    postId bigint(20) NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (userId) REFERENCES users(id),
    FOREIGN KEY (postId) REFERENCES posts(id),
    CONSTRAINT UQ_UsId_PoId UNIQUE(userId, postId)
);
-- !Downs
DROP TABLE likes;