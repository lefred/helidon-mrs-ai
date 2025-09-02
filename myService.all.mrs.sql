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
    ADD AUTH APP `MRS` IF EXISTS
    ADD AUTH APP `MySQL` IF EXISTS;

CREATE OR REPLACE REST SCHEMA /sakila ON SERVICE /myService
    FROM `sakila`
    AUTHENTICATION NOT REQUIRED;

CREATE OR REPLACE REST VIEW /actor
    ON SERVICE /myService SCHEMA /sakila
    AS sakila.actor CLASS MyServiceSakilaActor @INSERT @UPDATE @DELETE {
        actorId: actor_id @KEY @SORTABLE,
        firstName: first_name,
        lastName: last_name,
        lastUpdate: last_update,
        filmActor: sakila.film_actor @INSERT @UPDATE @DELETE {
            actorId: actor_id @KEY @SORTABLE,
            filmId: film_id @KEY @SORTABLE,
            lastUpdate: last_update,
            film: sakila.film @INSERT @UPDATE @DELETE {
                filmId: film_id @KEY @SORTABLE,
                title: title,
                description: description,
                releaseYear: release_year,
                languageId: language_id,
                originalLanguageId: original_language_id,
                rentalDuration: rental_duration,
                rentalRate: rental_rate,
                length: length,
                replacementCost: replacement_cost,
                rating: rating,
                specialFeatures: special_features,
                lastUpdate: last_update,
                language: sakila.language @INSERT @UPDATE @DELETE {
                    languageId: language_id @KEY @SORTABLE,
                    name: name,
                    lastUpdate: last_update
                },
                filmCategory: sakila.film_category @INSERT @UPDATE @DELETE {
                    filmId: film_id @KEY @SORTABLE,
                    categoryId: category_id @KEY @SORTABLE,
                    lastUpdate: last_update,
                    category: sakila.category @INSERT @UPDATE @DELETE {
                        categoryId: category_id @KEY @SORTABLE,
                        name: name,
                        lastUpdate: last_update
                    }
                }
            }
        }
    }
    AUTHENTICATION REQUIRED;

CREATE OR REPLACE REST VIEW /category
    ON SERVICE /myService SCHEMA /sakila
    AS sakila.category CLASS MyServiceSakilaCategory {
        name: name,
        lastUpdate: last_update,
        categoryId: category_id @KEY @SORTABLE,
        filmCategory: sakila.film_category {
            categoryId: category_id @KEY @SORTABLE,
            filmId: film_id @KEY @SORTABLE,
            film: sakila.film {
                languageId: language_id,
                rating: rating,
                releaseYear: release_year,
                specialFeatures: special_features,
                language: sakila.language {
                    lastUpdate: last_update,
                    languageId: language_id @KEY @SORTABLE,
                    name: name
                },
                replacementCost: replacement_cost,
                rentalRate: rental_rate,
                languageOriginalLanguageId: sakila.language {
                    languageId: language_id @KEY @SORTABLE,
                    lastUpdate: last_update,
                    name: name
                },
                description: description,
                lastUpdate: last_update,
                title: title,
                filmActor: sakila.film_actor {
                    lastUpdate: last_update,
                    filmId: film_id @KEY @SORTABLE,
                    actorId: actor_id @KEY @SORTABLE,
                    actor: sakila.actor {
                        actorId: actor_id @KEY @SORTABLE,
                        lastUpdate: last_update,
                        firstName: first_name,
                        lastName: last_name
                    }
                },
                inventory: sakila.inventory {
                    lastUpdate: last_update,
                    rental: sakila.rental {
                        returnDate: return_date,
                        rentalId: rental_id @KEY @SORTABLE,
                        inventoryId: inventory_id,
                        customerId: customer_id,
                        staffId: staff_id,
                        rentalDate: rental_date,
                        lastUpdate: last_update
                    },
                    inventoryId: inventory_id @KEY @SORTABLE,
                    filmId: film_id,
                    storeId: store_id,
                    store: sakila.store {
                        staff: sakila.staff {
                            storeId: store_id,
                            lastName: last_name,
                            email: email,
                            staffId: staff_id @KEY @SORTABLE,
                            password: password,
                            lastUpdate: last_update,
                            active: active,
                            username: username,
                            payment: sakila.payment {
                                paymentDate: payment_date,
                                rentalId: rental_id,
                                amount: amount,
                                lastUpdate: last_update,
                                customerId: customer_id,
                                staffId: staff_id,
                                paymentId: payment_id @KEY @SORTABLE
                            },
                            firstName: first_name,
                            picture: picture,
                            address: sakila.address {
                                location: location,
                                postalCode: postal_code,
                                phone: phone,
                                city: sakila.city {
                                    countryId: country_id,
                                    cityId: city_id @KEY @SORTABLE,
                                    lastUpdate: last_update,
                                    city: city
                                },
                                address: address,
                                district: district,
                                store: sakila.store {
                                    addressId: address_id,
                                    storeId: store_id @KEY @SORTABLE,
                                    lastUpdate: last_update,
                                    managerStaffId: manager_staff_id
                                },
                                customer: sakila.customer {
                                    lastName: last_name,
                                    customerId: customer_id @KEY @SORTABLE,
                                    createDate: create_date,
                                    addressId: address_id,
                                    storeId: store_id,
                                    firstName: first_name,
                                    email: email,
                                    lastUpdate: last_update,
                                    active: active
                                },
                                addressId: address_id @KEY @SORTABLE,
                                address2: address2,
                                lastUpdate: last_update,
                                cityId: city_id
                            },
                            addressId: address_id
                        },
                        storeId: store_id @KEY @SORTABLE,
                        managerStaffId: manager_staff_id,
                        lastUpdate: last_update,
                        addressId: address_id
                    }
                },
                originalLanguageId: original_language_id,
                length: length,
                rentalDuration: rental_duration,
                filmId: film_id @KEY @SORTABLE
            },
            lastUpdate: last_update
        }
    }
    AUTHENTICATION REQUIRED;

CREATE OR REPLACE REST VIEW /city
    ON SERVICE /myService SCHEMA /sakila
    AS sakila.city CLASS MyServiceSakilaCity {
        countryId: country_id,
        country: sakila.country {
            country: country,
            lastUpdate: last_update,
            countryId: country_id @KEY @SORTABLE
        },
        lastUpdate: last_update,
        cityId: city_id @KEY @SORTABLE,
        city: city
    }
    AUTHENTICATION REQUIRED;

CREATE OR REPLACE REST VIEW /film
    ON SERVICE /myService SCHEMA /sakila
    AS sakila.film CLASS MyServiceSakilaFilm {
        originalLanguageId: original_language_id,
        languageId: language_id,
        filmActor: sakila.film_actor {
            lastUpdate: last_update,
            actorId: actor_id @KEY @SORTABLE,
            filmId: film_id @KEY @SORTABLE
        },
        language: sakila.language {
            name: name,
            languageId: language_id @KEY @SORTABLE,
            lastUpdate: last_update
        },
        languageOriginalLanguageId: sakila.language {
            lastUpdate: last_update,
            name: name,
            languageId: language_id @KEY @SORTABLE
        },
        title: title,
        length: length,
        rentalDuration: rental_duration,
        description: description,
        releaseYear: release_year,
        replacementCost: replacement_cost,
        filmCategory: sakila.film_category {
            lastUpdate: last_update,
            filmId: film_id @KEY @SORTABLE,
            categoryId: category_id @KEY @SORTABLE
        },
        rating: rating,
        rentalRate: rental_rate,
        filmId: film_id @KEY @SORTABLE,
        specialFeatures: special_features,
        lastUpdate: last_update
    }
    AUTHENTICATION REQUIRED;

CREATE OR REPLACE REST VIEW /users
    ON SERVICE /myService SCHEMA /sakila
    AS sakila.users CLASS MyServiceSakilaUsers {
        email: email,
        username: username,
        lastname: lastname,
        passwordHash: password_hash,
        firstname: firstname,
        userId: user_id @KEY @SORTABLE
    }
    AUTHENTICATION REQUIRED;