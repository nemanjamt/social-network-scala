-- create users

-- !Ups
CREATE TABLE users
(
    id bigint(20)  NOT NULL AUTO_INCREMENT,
    firstName VARCHAR(30) NOT NULL,
    lastName VARCHAR(45) NOT NULL,
    username VARCHAR(30) NOT NULL,
    password VARCHAR(100) NOT NULL,
    deleted BOOLEAN NOT NULL,
    UNIQUE(username),
    PRIMARY KEY(id)
);

-- !Downs
DROP TABLE users;
