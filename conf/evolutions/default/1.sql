-- noinspection SqlNoDataSourceInspectionForFile

-- !Ups

CREATE TABLE SLATES (
    id bigint(20) NOT NULL AUTO_INCREMENT,
    title varchar(255) NOT NULL,
    creator varchar(255) NOT NULL,
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
    voter varchar(255) NOT NULL
);

CREATE TABLE FPTP_CHOICES (
    ballot_id bigint(20) references BALLOTS(id),
    question_id bigint(20) references QUESTIONS(id),
    candidate_id bigint(20) references CANDIDATES(id)
);

CREATE TABLE USER (
    userid varchar(255) NOT NULL,
    firstname varchar(255) NULL,
    lastname varchar(255) NULL,
    fullname varchar(255) NULL,
    email varchar(255) NULL,
    avatarurl varchar(255) NULL
);

CREATE TABLE LOGIN_INFO (
    id bigint(20) NOT NULL AUTO_INCREMENT,
    providerid varchar(255) NOT NULL,
    providerkey varchar(255) NOT NULL
);

CREATE TABLE USER_LOGIN_INFO (
    userid varchar(255) NOT NULL,
    logininfoid bigint(20) NOT NULL
);

CREATE TABLE PASSWORD_INFO (
    hasher varchar(255) NOT NULL,
    password varchar(255) NOT NULL,
    salt varchar(255) NULL,
    logininfoid bigint(20) NOT NULL
);



-- !Downs

DROP TABLE SLATES;
DROP TABLE QUESTIONS;
DROP TABLE CANDIDATES;
DROP TABLE BALLOTS;
DROP TABLE FPTP_CHOICES;
DROP TABLE USER;
DROP TABLE LOGIN_INFO;
DROP TABLE USER_LOGIN_INFO;
DROP TABLE PASSWORD_INFO;