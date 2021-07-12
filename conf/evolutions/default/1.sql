-- noinspection SqlNoDataSourceInspectionForFile

-- !Ups

CREATE TABLE SLATES (
    id bigint NOT NULL AUTO_INCREMENT,
    title varchar(255) NOT NULL,
    creator varchar(255) NOT NULL,
    anonymous boolean NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE QUESTIONS (
    id bigint NOT NULL AUTO_INCREMENT,
    slate_id bigint,
    text varchar(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT FK_QUESTION_SLATE FOREIGN KEY (slate_id) REFERENCES SLATES(id)
);

CREATE TABLE CANDIDATES (
    id bigint NOT NULL AUTO_INCREMENT,
    name varchar(255) NOT NULL,
    description varchar(255) NULL,
    question_id bigint,
    slate_id bigint,
    PRIMARY KEY (id),
    CONSTRAINT FK_CANDIDATE_SLATE FOREIGN KEY (slate_id) REFERENCES SLATES(id),
    CONSTRAINT FK_CANDIDATE_QUESTION FOREIGN KEY (question_id) REFERENCES QUESTIONS(id)
);

CREATE TABLE BALLOTS (
    id bigint NOT NULL AUTO_INCREMENT,
    slate_id bigint,
    voter varchar(255) NOT NULL,
    anonymous boolean NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT FK_BALLOT_SLATE FOREIGN KEY (slate_id) REFERENCES SLATES(id)
);

CREATE TABLE FPTP_CHOICES (
    ballot_id bigint,
    question_id bigint,
    candidate_id bigint,
    CONSTRAINT FK_FPTP_BALLOT FOREIGN KEY (ballot_id) REFERENCES BALLOTS(id),
    CONSTRAINT FK_FPTP_QUESTION FOREIGN KEY (question_id) REFERENCES QUESTIONS(id),
    CONSTRAINT FK_FPTP_CANDIDATE FOREIGN KEY (candidate_id) REFERENCES CANDIDATES(id)
);

CREATE TABLE APPROVAL_CHOICES(
    ballot_id bigint,
    question_id bigint,
    candidate_id bigint,
    CONSTRAINT FK_APPROVAL_BALLOT FOREIGN KEY (ballot_id) REFERENCES BALLOTS(id),
    CONSTRAINT FK_APPROVAL_QUESTION FOREIGN KEY (question_id) REFERENCES QUESTIONS(id),
    CONSTRAINT FK_APPROVAL_CANDIDATE FOREIGN KEY (candidate_id) REFERENCES CANDIDATES(id)
);

CREATE TABLE RANKED_CHOICES(
    ballot_id bigint,
    question_id bigint,
    candidate_id bigint,
    rank bigint NOT NULL,
    CONSTRAINT FK_RANKED_BALLOT FOREIGN KEY (ballot_id) REFERENCES BALLOTS(id),
    CONSTRAINT FK_RANKED_QUESTION FOREIGN KEY (question_id) REFERENCES QUESTIONS(id),
    CONSTRAINT FK_RANKED_CANDIDATE FOREIGN KEY (candidate_id) REFERENCES CANDIDATES(id)
);

CREATE TABLE RANGE_CHOICES(
   ballot_id bigint references BALLOTS(id),
   question_id bigint references QUESTIONS(id),
   candidate_id bigint references CANDIDATES(id),
   score bigint NOT NULL,
   CONSTRAINT FK_RANGE_BALLOT FOREIGN KEY (ballot_id) REFERENCES BALLOTS(id),
   CONSTRAINT FK_RANGE_QUESTION FOREIGN KEY (question_id) REFERENCES QUESTIONS(id),
   CONSTRAINT FK_RANGE_CANDIDATE FOREIGN KEY (candidate_id) REFERENCES CANDIDATES(id)
);

CREATE TABLE USER (
    user_id varchar(255) NOT NULL,
    first_name varchar(255) NULL,
    last_name varchar(255) NULL,
    full_name varchar(255) NULL,
    email varchar(255) NULL,
    avatar_url varchar(255) NULL,
    PRIMARY KEY(user_id)
);

CREATE TABLE LOGIN_INFO (
    id bigint NOT NULL AUTO_INCREMENT,
    provider_id varchar(255) NOT NULL,
    provider_key varchar(255) NOT NULL,
    PRIMARY KEY(id)
);

CREATE TABLE USER_LOGIN_INFO (
    user_id varchar(255) NOT NULL,
    login_info_id bigint NOT NULL
);

CREATE TABLE PASSWORD_INFO (
    hasher varchar(255) NOT NULL,
    password varchar(255) NOT NULL,
    salt varchar(255) NULL,
    login_info_id bigint NOT NULL
);



-- !Downs

DROP TABLE FPTP_CHOICES;
DROP TABLE APPROVAL_CHOICES;
DROP TABLE RANKED_CHOICES;
DROP TABLE RANGE_CHOICES;
DROP TABLE USER;
DROP TABLE LOGIN_INFO;
DROP TABLE USER_LOGIN_INFO;
DROP TABLE PASSWORD_INFO;

DROP TABLE BALLOTS;
DROP TABLE CANDIDATES;
DROP TABLE QUESTIONS;
DROP TABLE SLATES;