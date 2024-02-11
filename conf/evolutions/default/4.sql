-- create frienships

-- !Ups
CREATE TABLE friendships
(
    id bigint(20) NOT NULL AUTO_INCREMENT,
    firstUserId bigint(20) NOT NULL,
    secondUserId bigint(20) NOT NULL,
    createdDate TEXT NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (firstUserId) REFERENCES users(id),
    FOREIGN KEY (secondUserId) REFERENCES users(id),
    CONSTRAINT UQ_FirUsId_SecUsId UNIQUE(firstUserId, secondUserId)
);
-- !Downs
DROP TABLE friendships;