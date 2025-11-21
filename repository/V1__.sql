CREATE TABLE note
(
    note_id            VARCHAR(255) NOT NULL,
    created_at         VARCHAR(255) NULL,
    `description`      VARCHAR(300) NULL,
    note_thumbnail     BLOB NULL,
    note_pdf_data      BLOB NULL,
    pdf_note_filename  VARCHAR(255) NULL,
    remarks            VARCHAR(300) NULL,
    status             ENUM NULL,
    thumbnail_filename VARCHAR(255) NULL,
    title              VARCHAR(60) NULL,
    updated_at         VARCHAR(255) NULL,
    user_id            VARCHAR(255) NULL,
    subject_id         VARCHAR(36) NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (note_id)
);

CREATE TABLE note_report
(
    reportid            VARCHAR(36) NOT NULL,
    additional_details  VARCHAR(120) NULL,
    reason              VARCHAR(255) NULL,
    reported_by_user_id VARCHAR(255) NULL,
    reported_note_id    VARCHAR(255) NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (reportid)
);

CREATE TABLE reset_token
(
    id          VARCHAR(36) NOT NULL,
    expires_at  datetime NULL,
    reset_token VARCHAR(255) NULL,
    user_id     VARCHAR(255) NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE subject
(
    subject_id        VARCHAR(36) NOT NULL,
    subject_code      VARCHAR(20) NULL,
    created_at        date NULL,
    department        VARCHAR(255) NULL,
    semester          INT         NOT NULL,
    short_description VARCHAR(120) NULL,
    status            ENUM NULL,
    subject_name      VARCHAR(30) NULL,
    updated_at        date NULL,
    created_by_id     VARCHAR(255) NULL,
    updated_by_id     VARCHAR(255) NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (subject_id)
);

CREATE TABLE temp_user
(
    id               VARCHAR(255) NOT NULL,
    account_status   ENUM NULL,
    department       VARCHAR(255) NULL,
    gender           VARCHAR(255) NULL,
    remarks          VARCHAR(512) NULL,
    request_at       date NULL,
    semester         INT          NOT NULL,
    university_email VARCHAR(20) NULL,
    username         VARCHAR(35) NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE user
(
    id                VARCHAR(255) NOT NULL,
    account_remarks   VARCHAR(512) NULL,
    account_status    ENUM NULL,
    department        VARCHAR(2) NULL,
    expiration_at     datetime NULL,
    gender            VARCHAR(7) NULL,
    is_email_verified BIT(1) NULL,
    is_enabled        BIT(1) NULL,
    password          VARCHAR(255) NULL,
    semester          INT          NOT NULL,
    university_email  VARCHAR(20) NULL,
    username          VARCHAR(35) NULL,
    verification_code INT          NOT NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE user_report
(
    reportid            VARCHAR(36) NOT NULL,
    additional_details  VARCHAR(120) NULL,
    reason              VARCHAR(255) NULL,
    reported_by_user_id VARCHAR(255) NULL,
    reported_user_id    VARCHAR(255) NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (reportid)
);

CREATE TABLE user_roles
(
    user_id VARCHAR(255) NOT NULL,
    roles   ENUM NULL
);

ALTER TABLE user
    ADD CONSTRAINT UK8225o602bb6ge0ufvlmj7ojae UNIQUE (university_email);

ALTER TABLE subject
    ADD CONSTRAINT UKbqn0dl9ld0wcq9na8amhhramm UNIQUE (subject_code);

ALTER TABLE reset_token
    ADD CONSTRAINT UKfbfq7c1c1wxpt21p6jd2jtvhj UNIQUE (user_id);

ALTER TABLE reset_token
    ADD CONSTRAINT UKgdygoqywuoo26j4gfbdto1621 UNIQUE (reset_token);

ALTER TABLE temp_user
    ADD CONSTRAINT UKp77yowj3lbdmtsrt620j2wdkn UNIQUE (university_email);

ALTER TABLE temp_user
    ADD CONSTRAINT UKs8deif58nr23wvqxqbhow7hw2 UNIQUE (username);

ALTER TABLE user
    ADD CONSTRAINT UKsb8bbouer5wak8vyiiy4pf2bx UNIQUE (username);

ALTER TABLE user_roles
    ADD CONSTRAINT FK55itppkw3i07do3h7qoclqd4k FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE NO ACTION;

CREATE INDEX FK55itppkw3i07do3h7qoclqd4k ON user_roles (user_id);

ALTER TABLE subject
    ADD CONSTRAINT FK5rf3xveyyebcjhtloqvvnc8f6 FOREIGN KEY (updated_by_id) REFERENCES user (id) ON DELETE NO ACTION;

CREATE INDEX FK5rf3xveyyebcjhtloqvvnc8f6 ON subject (updated_by_id);

ALTER TABLE note_report
    ADD CONSTRAINT FK6b4tp41odcmd9i3x3nv64iyf2 FOREIGN KEY (reported_by_user_id) REFERENCES user (id) ON DELETE NO ACTION;

CREATE INDEX FK6b4tp41odcmd9i3x3nv64iyf2 ON note_report (reported_by_user_id);

ALTER TABLE reset_token
    ADD CONSTRAINT FK8miiqu89hlsw4wjl2u9ax94ry FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE NO ACTION;

ALTER TABLE user_report
    ADD CONSTRAINT FKb4n1wy89pp4rgf00x710vsgh1 FOREIGN KEY (reported_by_user_id) REFERENCES user (id) ON DELETE NO ACTION;

CREATE INDEX FKb4n1wy89pp4rgf00x710vsgh1 ON user_report (reported_by_user_id);

ALTER TABLE note
    ADD CONSTRAINT FKc4d38pjk2knvmkkgfk3id0s3x FOREIGN KEY (subject_id) REFERENCES subject (subject_id) ON DELETE NO ACTION;

CREATE INDEX FKc4d38pjk2knvmkkgfk3id0s3x ON note (subject_id);

ALTER TABLE user_report
    ADD CONSTRAINT FKfu1jo7wwk0nhy4celx9viudvc FOREIGN KEY (reported_user_id) REFERENCES user (id) ON DELETE NO ACTION;

CREATE INDEX FKfu1jo7wwk0nhy4celx9viudvc ON user_report (reported_user_id);

ALTER TABLE note
    ADD CONSTRAINT FKmoddtnuw3yy6ct34xnw6u0boh FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE NO ACTION;

CREATE INDEX FKmoddtnuw3yy6ct34xnw6u0boh ON note (user_id);

ALTER TABLE note_report
    ADD CONSTRAINT FKmr3thauyxn88605jteg04sdy3 FOREIGN KEY (reported_note_id) REFERENCES note (note_id) ON DELETE NO ACTION;

CREATE INDEX FKmr3thauyxn88605jteg04sdy3 ON note_report (reported_note_id);

ALTER TABLE subject
    ADD CONSTRAINT fk_createdBy_user FOREIGN KEY (created_by_id) REFERENCES user (id) ON DELETE CASCADE;

CREATE INDEX fk_createdBy_user ON subject (created_by_id);