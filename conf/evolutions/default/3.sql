-- create friendship requests

-- !Ups
CREATE TABLE friendshipRequests
(
    id bigint(20) NOT NULL AUTO_INCREMENT,
    receiverId bigint(20) NOT NULL,
    senderId bigint(20) NOT NULL,
    createdDate TEXT NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (receiverId) REFERENCES users(id),
    FOREIGN KEY (senderId) REFERENCES users(id),
    CONSTRAINT UQ_SenId_RecId UNIQUE(receiverId, senderId)
);
-- !Downs
DROP TABLE friendshipRequests;