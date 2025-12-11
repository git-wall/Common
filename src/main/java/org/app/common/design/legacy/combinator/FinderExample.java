//package org.app.common.design.legacy.combinator;
//
//import java.util.Arrays;
//import java.util.List;
//import java.util.Optional;
//import java.util.stream.Collectors;
//
///**
// * Example class demonstrating how to use the Finder interface with the combinator pattern.
// */
//public class FinderExample {
//
//    /**
//     * Example Person class for demonstration.
//     */
//    public static class Person {
//        private final String name;
//        private final int age;
//        private final String city;
//        private final Optional<String> department;
//
//        public Person(String name, int age, String city, String department) {
//            this.name = name;
//            this.age = age;
//            this.city = city;
//            this.department = Optional.ofNullable(department);
//        }
//
//        public String getName() {
//            return name;
//        }
//
//        public int getAge() {
//            return age;
//        }
//
//        public String getCity() {
//            return city;
//        }
//
//        public Optional<String> getDepartment() {
//            return department;
//        }
//
//        @Override
//        public String toString() {
//            return "Person{" +
//                    "name='" + name + '\'' +
//                    ", age=" + age +
//                    ", city='" + city + '\'' +
//                    ", department=" + department.orElse("None") +
//                    '}';
//        }
//    }
//
//    /**
//     * Example of using the Finder interface to find people based on various criteria.
//     */
//    public static void main(String[] args) {
//        // Create a list of people
//        List<Person> people = Arrays.asList(
//                new Person("Alice", 30, "New York", "Engineering"),
//                new Person("Bob", 25, "San Francisco", "Marketing"),
//                new Person("Charlie", 35, "New York", "Sales"),
//                new Person("David", 40, "Boston", "Engineering"),
//                new Person("Eve", 28, "San Francisco", null)
//        );
//
//        // Create finders for different criteria
//        Finder<Person> olderThan30 = person -> person.getAge() > 30;
//        Finder<Person> livesInNewYork = person -> "New York".equals(person.getCity());
//        Finder<Person> inEngineeringDept = Finder.byOptionalProperty(Person::getDepartment, "Engineering");
//
//        // Combine finders using the combinator pattern
//        Finder<Person> olderThan30AndInNewYork = olderThan30.and(livesInNewYork);
//        Finder<Person> inEngineeringOrOlderThan30 = inEngineeringDept.or(olderThan30);
//        Finder<Person> notInNewYork = livesInNewYork.not();
//
//        // Use the finders to filter the list of people
//        System.out.println("People older than 30:");
//        printFilteredPeople(people, olderThan30);
//
//        System.out.println("\nPeople living in New York:");
//        printFilteredPeople(people, livesInNewYork);
//
//        System.out.println("\nPeople in Engineering department:");
//        printFilteredPeople(people, inEngineeringDept);
//
//        System.out.println("\nPeople older than 30 and living in New York:");
//        printFilteredPeople(people, olderThan30AndInNewYork);
//
//        System.out.println("\nPeople in Engineering or older than 30:");
//        printFilteredPeople(people, inEngineeringOrOlderThan30);
//
//        System.out.println("\nPeople not living in New York:");
//        printFilteredPeople(people, notInNewYork);
//
//        // Example of using the static factory methods
//        Finder<Person> byNameAlice = Finder.byPropertyEquals(Person::getName, "Alice");
//        System.out.println("\nPeople named Alice:");
//        printFilteredPeople(people, byNameAlice);
//
//        // Example of using composeTransform
//        Finder<String> cityFinder = city -> city.startsWith("New");
//        Finder<Person> livesInCityStartingWithNew = cityFinder.composeTransform(Person::getCity);
//        System.out.println("\nPeople living in a city starting with 'New':");
//        printFilteredPeople(people, livesInCityStartingWithNew);
//
//        // Example of using transformResult
//        List<String> namesOfPeopleInNewYork = people.stream()
//                .map(livesInNewYork.transformResult(found -> found ? "Lives in New York" : "Does not live in New York"))
//                .collect(Collectors.toList());
//        System.out.println("\nNames of people in New York: " + namesOfPeopleInNewYork);
//    }
//
//    /**
//     * Helper method to print people that match a finder.
//     */
//    private static void printFilteredPeople(List<Person> people, Finder<Person> finder) {
//        people.stream()
//                .filter(finder::find)
//                .forEach(System.out::println);
//    }
//}
