-- create posts

-- !Ups
CREATE TABLE posts
(
    id bigint(20) NOT NULL AUTO_INCREMENT,
    userId bigint(20),
    content VARCHAR(100) NOT NULL,
    countLike INT NOT NULL,
    deleted BOOLEAN NOT NULL,
    createdDate TEXT NOT NULL,
    updatedDate TEXT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (userId) REFERENCES users(id)
);
-- !Downs
DROP TABLE posts;