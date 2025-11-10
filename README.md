# helidon-mrs-ai

This repo is a demo using Helidon 4.3.0M2 with MySQL REST Service and OCI GenAI.


You need to use the sakila sample database and add an `users` table like this: 

```
SQL> use sakila;
SQL> CREATE TABLE `users` (
  `user_id` int unsigned NOT NULL AUTO_INCREMENT,
  `firstname` varchar(30) DEFAULT NULL,
  `lastname` varchar(30) DEFAULT NULL,
  `username` varchar(30) DEFAULT NULL,
  `email` varchar(50) DEFAULT NULL,
  `password_hash` varchar(60) DEFAULT NULL,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
```

To compile the Helidon project:

```
$ mvn -q clean install -DskipTests package
```

And to run it after having created the file `src/main/resources/application.yaml`, you do:

```
$ java -Dmrs.auth.username=<USERNAME> -Dmrs.auth.password='<PASSWORD>' \
   -Dmrs.url='https://127.0.0.1:33060/myService' -Dmrs.auth.app='MySQL' \
   -Dmrs.insecureTls=true  -Dmrs.auth.sessionType=bearer -jar helidon-mrs-ai.jar
```
