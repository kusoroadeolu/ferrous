package io.github.kusoroadeolu.ferrous;

import io.github.kusoroadeolu.ferrous.option.Option;
import io.github.kusoroadeolu.ferrous.result.Result;

public class FerrosExample {
    public static void main(String[] args) {
        // Example 1: Database lookup that might not find anything
        var user = findUserById(123)
            .map(User::email)
            .filter(email -> email.contains("@"))
            .ifSomeOrElse(
                email -> "Found email: " + email,
                () -> "No valid email found"
            );
        System.out.println(user);

        // Example 2: Chaining Result and Option together
        var config = loadConfig("app.properties")
            .flatMap(FerrosExample::parseConfig)
            .map(cfg -> cfg.get("database.url"))  // Returns Option<String>
            .ok()  // Convert Result<Option<String>, Error> -> Option<Option<String>>
            .flatMap(opt -> opt)  // Flatten Option<Option<T>> -> Option<T>
            .unwrapOr("default-database-url");
        
        System.out.println("Database URL: " + config);

        // Example 3: Converting between Result and Option
        var ageResult = parseAge("25")
            .flatMap(FerrosExample::validateAge);
        
        // Extract just the success value as Option
        Option<Integer> maybeAge = ageResult.ok();
        maybeAge.ifSomeOrElse(
            age -> System.out.println("Valid age: " + age),
                () -> System.out.println("Invalid age")
        );

        // Or extract just the error as Option
        Option<String> maybeError = parseAge("invalid").err();
        maybeError.ifSomeOrElse(
            error -> System.out.println("Error occurred: " + error),
            () -> System.out.println("No error")
        );
    }

    static Option<User> findUserById(int id) {
        if (id == 123) {
            return Option.some(new User("alice@example.com"));
        }
        return Option.none();
    }

    static Result<String, String> loadConfig(String filename) {
        // Simulate file loading
        if (filename.endsWith(".properties")) {
            return Result.ok("database.url=localhost:5432");
        }
        return Result.err("File not found: " + filename);
    }

    static Result<Config, String> parseConfig(String content) {
        try {
            var config = new Config();
            for (String line : content.split("\n")) {
                String[] parts = line.split("=");
                if (parts.length == 2) {
                    config.put(parts[0], parts[1]);
                }
            }
            return Result.ok(config);
        } catch (Exception e) {
            return Result.err("Parse error: " + e.getMessage());
        }
    }

    static Result<Integer, String> parseAge(String input) {
        try {
            return Result.ok(Integer.parseInt(input));
        } catch (NumberFormatException e) {
            return Result.err("Invalid number: " + input);
        }
    }

    static Result<Integer, String> validateAge(Integer age) {
        if (age < 0 || age > 150) {
            return Result.err("Age out of range: " + age);
        }
        return Result.ok(age);
    }

    record User(String email) {}
    
    static class Config extends java.util.HashMap<String, String> {
        public Option<String> get(String key) {
            return Option.ofNullable(super.get(key));
        }
    }
}