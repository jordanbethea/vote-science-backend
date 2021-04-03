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
    voter varchar(255) NOT NULL,
    PRIMARY KEY (id)
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
    avatarurl varchar(255) NULL,
    activated boolean NOT NULL,
    PRIMARY KEY (userid)
);

CREATE TABLE LOGIN_INFO (
    id bigint(20) NOT NULL AUTO_INCREMENT,
    providerid varchar(255) NOT NULL,
    providerkey varchar(255) NOT NULL,
    PRIMARY KEY (id)
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

CREATE TABLE OAUTH1INFO (
    id bigint(20) NOT NULL AUTO_INCREMENT,
    token varchar(255) NOT NULL,
    secret varchar(255) NOT NULL,
    logininfoid bigint(20) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE OAUTH2INFO (
    id bigint(20) NOT NULL AUTO_INCREMENT,
    accesstoken varchar(255) NOT NULL,
    tokentype varchar(255) NULL,
    expiresin int(20) NULL,
    refreshtoken varchar(255) NULL,
    logininfoid bigint(20) NOT NULL,
    PRIMARY KEY(id)
);

CREATE TABLE OPENIDINFO (
    id varchar(255) NOT NULL,
    logininfoid bigint(20) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE OPENIDATTRIBUTES (
    id varchar(255) NOT NULL,
    key varchar(255) NOT NULL,
    value varchar(255) NOT NULL
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
DROP TABLE OAUTH1INFO;
DROP TABLE OAUTH2INFO;
DROP TABLE OPENIDINFO;
DROP TABLE OPENIDATTRIBUTES;