-- noinspection SqlNoDataSourceInspectionForFile

-- !Ups

CREATE TABLE SLATES (
    id bigint(20) NOT NULL AUTO_INCREMENT,
    title varchar(255) NOT NULL,
    creator varchar(255) NOT NULL,
    anonymous boolean NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE QUESTIONS (
    id bigint(20) NOT NULL AUTO_INCREMENT,
    slate_id bigint(20) references SLATES(id),
    text varchar(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE CANDIDATES (
    id bigint(20) NOT NULL AUTO_INCREMENT,
    name varchar(255) NOT NULL,
    description varchar(255) NULL,
    question_id bigint(20) references QUESTIONS(id),
    slate_id bigint(20) references SLATES(id),
    PRIMARY KEY (id)
);

CREATE TABLE BALLOTS (
    id bigint(20) NOT NULL AUTO_INCREMENT,
    slate_id bigint(20) references SLATES(id),
    voter varchar(255) NOT NULL,
    anonymous boolean NOT NULL
);

CREATE TABLE FPTP_CHOICES (
    ballot_id bigint(20) references BALLOTS(id),
    question_id bigint(20) references QUESTIONS(id),
    candidate_id bigint(20) references CANDIDATES(id)//,
    //PRIMARY KEY (ballot_id, question_id)
);

CREATE TABLE APPROVAL_CHOICES(
    ballot_id bigint(20) references BALLOTS(id),
    question_id bigint(20) references QUESTIONS(id),
    candidate_id bigint(20) references CANDIDATES(id)
);

CREATE TABLE RANKED_CHOICES(
    ballot_id bigint(20) references BALLOTS(id),
    question_id bigint(20) references QUESTIONS(id),
    candidate_id bigint(20) references CANDIDATES(id),
    rank bigint(20) NOT NULL//,
    //PRIMARY KEY (ballot_id, question_id, candidate_id)
);

CREATE TABLE RANGE_CHOICES(
   ballot_id bigint(20) references BALLOTS(id),
   question_id bigint(20) references QUESTIONS(id),
   candidate_id bigint(20) references CANDIDATES(id),
   score bigint(20) NOT NULL//,
   //PRIMARY KEY (ballot_id, question_id, candidate_id)
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
    id bigint(20) NOT NULL AUTO_INCREMENT,
    provider_id varchar(255) NOT NULL,
    provider_key varchar(255) NOT NULL,
    PRIMARY KEY(id)
);

CREATE TABLE USER_LOGIN_INFO (
    user_id varchar(255) NOT NULL,
    login_info_id bigint(20) NOT NULL
);

CREATE TABLE PASSWORD_INFO (
    hasher varchar(255) NOT NULL,
    password varchar(255) NOT NULL,
    salt varchar(255) NULL,
    login_info_id bigint(20) NOT NULL
);



-- !Downs

DROP TABLE SLATES;
DROP TABLE QUESTIONS;
DROP TABLE CANDIDATES;
DROP TABLE BALLOTS;
DROP TABLE FPTP_CHOICES;
DROP TABLE APPROVAL_CHOICES;
DROP TABLE RANKED_CHOICES;
DROP TABLE RANGE_CHOICES;
DROP TABLE USER;
DROP TABLE LOGIN_INFO;
DROP TABLE USER_LOGIN_INFO;
DROP TABLE PASSWORD_INFO;