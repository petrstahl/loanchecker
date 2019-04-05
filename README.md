# Zonky Loanchecker

Command line demo application reading new loans and printing them to console.

## Running

Simplest way to run the application
```
mvn spring-boot:run
```

### Running tests
```
mvn test
```

### Creating codebase reports

Checkstyle, Findbugs and Jacoco reports will be generated in `target/site` directory.
```
mvn -Preports clean install
```

### Configuration

All available options can be changed in `src/main/resources/application.properties` file