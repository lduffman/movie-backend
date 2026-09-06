CREATE TABLE movies
(
    id          UUID                     NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    external_id VARCHAR(20)              NOT NULL,
    title       VARCHAR(255)             NOT NULL,
    year        INTEGER                  NOT NULL,
    genres      TEXT[] DEFAULT '{}'      NOT NULL,
    rating      NUMERIC(3,1),
    votes       INTEGER,
    runtime     INTEGER,
    directors   TEXT[] DEFAULT '{}'      NOT NULL,
    writers     TEXT[] DEFAULT '{}'      NOT NULL,
    poster_url  VARCHAR(1000),
    plot        TEXT,
    CONSTRAINT PK_movies PRIMARY KEY (id)
);

ALTER TABLE movies
    ADD CONSTRAINT UX_movies_externalId UNIQUE (external_id);
