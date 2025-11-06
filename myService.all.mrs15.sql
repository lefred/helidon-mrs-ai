CREATE OR REPLACE REST SERVICE /myService
    PUBLISHED
    OPTIONS {
        "http": {
            "allowedOrigin": "auto"
        },
        "headers": {
            "Access-Control-Allow-Headers": "Content-Type, Authorization, X-Requested-With, Origin, X-Auth-Token",
            "Access-Control-Allow-Methods": "GET, POST, PUT, DELETE, OPTIONS",
            "Access-Control-Allow-Credentials": "true"
        },
        "logging": {
            "request": {
                "body": true,
                "headers": true
            },
            "response": {
                "body": true,
                "headers": true
            },
            "exceptions": true
        },
        "includeLinksInResults": false,
        "returnInternalErrorDetails": true
    }
    ADD AUTH APP `MySQL` IF EXISTS;

CREATE OR REPLACE REST SCHEMA /sakila ON SERVICE /myService
    FROM `sakila`
    AUTHENTICATION NOT REQUIRED;

CREATE OR REPLACE REST VIEW /actor
    ON SERVICE /myService SCHEMA /sakila
    AS `sakila`.`actor` CLASS MyServiceSakilaActor @INSERT @UPDATE @DELETE {
        firstName: first_name,
        actorId: actor_id @KEY @SORTABLE,
        lastUpdate: last_update,
        filmActorActorId: sakila.film_actor @INSERT @UPDATE @DELETE {
            filmFilmId: sakila.film @INSERT @UPDATE @DELETE {
                filmId: film_id @KEY @SORTABLE,
                languageId: language_id,
                title: title,
                rentalRate: rental_rate,
                specialFeatures: special_features,
                description: description,
                originalLanguageId: original_language_id,
                rentalDuration: rental_duration,
                replacementCost: replacement_cost,
                length: length,
                releaseYear: release_year,
                lastUpdate: last_update,
                rating: rating
            },
            filmId: film_id @KEY @SORTABLE,
            actorId: actor_id @KEY @SORTABLE,
            lastUpdate: last_update
        },
        lastName: last_name
    }
    AUTHENTICATION REQUIRED;

CREATE OR REPLACE REST VIEW /category
    ON SERVICE /myService SCHEMA /sakila
    AS `sakila`.`category` CLASS MyServiceSakilaCategory {
        name: name,
        lastUpdate: last_update,
        categoryId: category_id @KEY @SORTABLE
    }
    AUTHENTICATION REQUIRED;

CREATE OR REPLACE REST VIEW /city
    ON SERVICE /myService SCHEMA /sakila
    AS `sakila`.`city` CLASS MyServiceSakilaCity {
        lastUpdate: last_update,
        cityId: city_id @KEY @SORTABLE,
        city: city,
        countryId: country_id
    }
    AUTHENTICATION REQUIRED;

CREATE OR REPLACE REST VIEW /film
    ON SERVICE /myService SCHEMA /sakila
    AS `sakila`.`film` CLASS MyServiceSakilaFilm {
        originalLanguageId: original_language_id,
        length: length,
        filmId: film_id @KEY @SORTABLE,
        rentalDuration: rental_duration,
        description: description,
        rating: rating,
        releaseYear: release_year,
        languageId: language_id,
        lastUpdate: last_update,
        replacementCost: replacement_cost,
        specialFeatures: special_features,
        rentalRate: rental_rate,
        title: title
    }
    AUTHENTICATION REQUIRED;

CREATE OR REPLACE REST VIEW /users
    ON SERVICE /myService SCHEMA /sakila
    AS `sakila`.`users` CLASS MyServiceSakilaUsers {
        firstname: firstname,
        email: email,
        updatedAt: updated_at,
        lastname: lastname,
        username: username,
        passwordHash: password_hash,
        userId: user_id @KEY @SORTABLE
    }
    AUTHENTICATION REQUIRED;