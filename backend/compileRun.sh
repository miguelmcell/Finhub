export spring.profiles.active=dev

./mvnw clean package
java -Dspring.profiles.active=dev  -jar target/robinhoodhub-0.0.1-SNAPSHOT.jar
