-- noinspection SqlNoDataSourceInspectionForFile

-- !Ups

-- Postgres style for deployment

CREATE TABLE SLATES (
    id uuid NOT NULL,
    title varchar(255) NOT NULL,
    creator_id uuid NULL,
    anon_creator varchar(255) NULL,
    PRIMARY KEY (id)
);

CREATE TABLE QUESTIONS (
    id uuid NOT NULL,
    slate_id uuid NOT NULL,
    text varchar(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT FK_QUESTION_SLATE FOREIGN KEY (slate_id) REFERENCES SLATES(id)
);

CREATE TABLE CANDIDATES (
    id uuid NOT NULL,
    name varchar(255) NOT NULL,
    description varchar(255) NULL,
    question_id uuid NOT NULL,
    slate_id uuid NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT FK_CANDIDATE_SLATE FOREIGN KEY (slate_id) REFERENCES SLATES(id),
    CONSTRAINT FK_CANDIDATE_QUESTION FOREIGN KEY (question_id) REFERENCES QUESTIONS(id)
);

CREATE TABLE BALLOTS (
    id uuid NOT NULL,
    slate_id uuid NOT NULL,
    voter_id uuid NULL,
    anon_voter varchar(255) NULL,
    PRIMARY KEY (id),
    CONSTRAINT FK_BALLOT_SLATE FOREIGN KEY (slate_id) REFERENCES SLATES(id)
);

CREATE TABLE FPTP_CHOICES (
    ballot_id uuid NOT NULL,
    question_id uuid NOT NULL,
    candidate_id uuid NOT NULL,
    CONSTRAINT FK_FPTP_BALLOT FOREIGN KEY (ballot_id) REFERENCES BALLOTS(id),
    CONSTRAINT FK_FPTP_QUESTION FOREIGN KEY (question_id) REFERENCES QUESTIONS(id),
    CONSTRAINT FK_FPTP_CANDIDATE FOREIGN KEY (candidate_id) REFERENCES CANDIDATES(id)
);

CREATE TABLE APPROVAL_CHOICES(
    ballot_id uuid,
    question_id uuid,
    candidate_id uuid,
    CONSTRAINT FK_APPROVAL_BALLOT FOREIGN KEY (ballot_id) REFERENCES BALLOTS(id),
    CONSTRAINT FK_APPROVAL_QUESTION FOREIGN KEY (question_id) REFERENCES QUESTIONS(id),
    CONSTRAINT FK_APPROVAL_CANDIDATE FOREIGN KEY (candidate_id) REFERENCES CANDIDATES(id)
);

CREATE TABLE RANKED_CHOICES(
    ballot_id uuid,
    question_id uuid,
    candidate_id uuid,
    rank int NOT NULL,
    CONSTRAINT FK_RANKED_BALLOT FOREIGN KEY (ballot_id) REFERENCES BALLOTS(id),
    CONSTRAINT FK_RANKED_QUESTION FOREIGN KEY (question_id) REFERENCES QUESTIONS(id),
    CONSTRAINT FK_RANKED_CANDIDATE FOREIGN KEY (candidate_id) REFERENCES CANDIDATES(id)
);

CREATE TABLE RANGE_CHOICES(
   ballot_id uuid references BALLOTS(id),
   question_id uuid references QUESTIONS(id),
   candidate_id uuid references CANDIDATES(id),
   score int NOT NULL,
   CONSTRAINT FK_RANGE_BALLOT FOREIGN KEY (ballot_id) REFERENCES BALLOTS(id),
   CONSTRAINT FK_RANGE_QUESTION FOREIGN KEY (question_id) REFERENCES QUESTIONS(id),
   CONSTRAINT FK_RANGE_CANDIDATE FOREIGN KEY (candidate_id) REFERENCES CANDIDATES(id)
);

CREATE TABLE USER_DATA (
    user_id uuid NOT NULL,
    first_name varchar(255) NULL,
    last_name varchar(255) NULL,
    full_name varchar(255) NULL,
    email varchar(255) NULL,
    avatar_url varchar(255) NULL,
    email_verified boolean NOT NULL,
    PRIMARY KEY(user_id)
);

CREATE TABLE LOGIN_INFO (
    id SERIAL,
    provider_id varchar(255) NOT NULL,
    provider_key varchar(255) NOT NULL,
    PRIMARY KEY(id)
);

CREATE TABLE USER_LOGIN_INFO (
    user_id uuid NOT NULL,
    login_info_id bigint NOT NULL
);

CREATE TABLE PASSWORD_INFO (
    hasher varchar(255) NOT NULL,
    password varchar(255) NOT NULL,
    salt varchar(255) NULL,
    login_info_id bigint NOT NULL
);

CREATE TABLE AUTH_TOKENS (
     id uuid NOT NULL,
     user_id uuid NOT NULL,
     expiry timestamp NOT NULL
);

CREATE TABLE GROUPS (
    id uuid NOT NULL PRIMARY KEY,
    name varchar(255) NOT NULL,
    description varchar(255) NULL,
    admin uuid NOT NULL references USER_DATA(user_id),
    CONSTRAINT FK_GROUP_ADMIN FOREIGN KEY (admin) REFERENCES USER_DATA(user_id)
);

CREATE TABLE GROUP_MEMBERS (
    group uuid NOT NULL REFERENCES GROUPS(id),
    user uuid NOT NULL REFERENCES USER_DATA(user_id),
    CONSTRAINT FK_GROUPMEMBER_GROUP FOREIGN KEY (group) REFERENCES GROUPS(id),
    CONSTRAINT FK_GROUPMEMBER_USER FOREIGN KEY (user) REFERENCES USER_DATA(user_id)
);


-- !Downs

DROP TABLE FPTP_CHOICES;
DROP TABLE APPROVAL_CHOICES;
DROP TABLE RANKED_CHOICES;
DROP TABLE RANGE_CHOICES;
DROP TABLE USER_DATA;
DROP TABLE LOGIN_INFO;
DROP TABLE USER_LOGIN_INFO;
DROP TABLE PASSWORD_INFO;
DROP TABLE AUTH_TOKENS;

DROP TABLE BALLOTS;
DROP TABLE CANDIDATES;
DROP TABLE QUESTIONS;
DROP TABLE SLATES;

DROP TABLE GROUPS;
DROP TABLE GROUP_MEMBERS;