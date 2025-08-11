package org.app.common.support;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

class EnrichTest {

    // Test class for demonstration
    static class Person {
        private String name;
        private int age;
        private List<String> hobbies;

        public Person(String name, int age) {
            this.name = name;
            this.age = age;
            this.hobbies = new ArrayList<>();
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public List<String> getHobbies() {
            return hobbies;
        }

        public void setHobbies(List<String> hobbies) {
            this.hobbies = hobbies;
        }

        public void addHobby(String hobby) {
            if (this.hobbies == null) {
                this.hobbies = new ArrayList<>();
            }
            this.hobbies.add(hobby);
        }
    }

    @Test
    void testObjectEnrichment() {
        // Create a person
        Person person = new Person("John", 30);

        // Define an enricher function
        Function<Person, Person> enricher = p -> {
            p.addHobby("Reading");
            return p;
        };

        // Enrich the person
        Person enrichedPerson = Enrich.object(person, enricher);

        // Verify enrichment
        assertEquals("John", enrichedPerson.getName());
        assertEquals(30, enrichedPerson.getAge());
        assertTrue(enrichedPerson.getHobbies().contains("Reading"));
    }

    @Test
    void testObjectWithContextEnrichment() {
        // Create a person
        Person person = new Person("John", 30);

        // Define a context
        Map<String, List<String>> hobbiesByName = new HashMap<>();
        hobbiesByName.put("John", Arrays.asList("Reading", "Swimming"));

        // Define an enricher function with context
        BiFunction<Person, Map<String, List<String>>, Person> enricher = (p, ctx) -> {
            List<String> hobbies = ctx.get(p.getName());
            if (hobbies != null) {
                hobbies.forEach(p::addHobby);
            }
            return p;
        };

        // Enrich the person with context
        Person enrichedPerson = Enrich.objectWithContext(person, hobbiesByName, enricher);

        // Verify enrichment
        assertEquals(2, enrichedPerson.getHobbies().size());
        assertTrue(enrichedPerson.getHobbies().contains("Reading"));
        assertTrue(enrichedPerson.getHobbies().contains("Swimming"));
    }

    @Test
    void testObjectInPlaceEnrichment() {
        // Create a person
        Person person = new Person("John", 30);

        // Define an in-place enricher
        BiConsumer<Person, Person> enricher = (p1, p2) -> {
            p1.addHobby("Cooking");
            p1.setAge(p1.getAge() + 1);
        };

        // Enrich the person in-place
        Person enrichedPerson = Enrich.objectInPlace(person, enricher);

        // Verify enrichment
        assertEquals("John", enrichedPerson.getName());
        assertEquals(31, enrichedPerson.getAge());
        assertTrue(enrichedPerson.getHobbies().contains("Cooking"));
        
        // Verify it's the same object
        assertSame(person, enrichedPerson);
    }

    @Test
    void testListEnrichment() {
        // Create a list of persons
        List<Person> persons = Arrays.asList(
            new Person("John", 30),
            new Person("Jane", 25),
            new Person("Bob", 40)
        );

        // Define an enricher function
        Function<Person, Person> enricher = p -> {
            p.addHobby("Reading");
            return p;
        };

        // Enrich the list
        List<Person> enrichedPersons = Enrich.list(persons, enricher);

        // Verify enrichment
        assertEquals(3, enrichedPersons.size());
        for (Person p : enrichedPersons) {
            assertTrue(p.getHobbies().contains("Reading"));
        }
    }

    @Test
    void testListWithContextEnrichment() {
        // Create a list of persons
        List<Person> persons = Arrays.asList(
            new Person("John", 30),
            new Person("Jane", 25),
            new Person("Bob", 40)
        );

        // Define a context
        Map<String, List<String>> hobbiesByName = new HashMap<>();
        hobbiesByName.put("John", Arrays.asList("Reading", "Swimming"));
        hobbiesByName.put("Jane", Arrays.asList("Dancing", "Painting"));
        hobbiesByName.put("Bob", Arrays.asList("Hiking", "Cooking"));

        // Define an enricher function with context
        BiFunction<Person, Map<String, List<String>>, Person> enricher = (p, ctx) -> {
            List<String> hobbies = ctx.get(p.getName());
            if (hobbies != null) {
                hobbies.forEach(p::addHobby);
            }
            return p;
        };

        // Enrich the list with context
        List<Person> enrichedPersons = Enrich.listWithContext(persons, hobbiesByName, enricher);

        // Verify enrichment
        assertEquals(3, enrichedPersons.size());
        assertEquals(2, enrichedPersons.get(0).getHobbies().size());
        assertEquals(2, enrichedPersons.get(1).getHobbies().size());
        assertEquals(2, enrichedPersons.get(2).getHobbies().size());
        
        assertTrue(enrichedPersons.get(0).getHobbies().contains("Reading"));
        assertTrue(enrichedPersons.get(0).getHobbies().contains("Swimming"));
        
        assertTrue(enrichedPersons.get(1).getHobbies().contains("Dancing"));
        assertTrue(enrichedPersons.get(1).getHobbies().contains("Painting"));
        
        assertTrue(enrichedPersons.get(2).getHobbies().contains("Hiking"));
        assertTrue(enrichedPersons.get(2).getHobbies().contains("Cooking"));
    }

    @Test
    void testListInPlaceEnrichment() {
        // Create a list of persons
        List<Person> persons = new ArrayList<>(Arrays.asList(
            new Person("John", 30),
            new Person("Jane", 25),
            new Person("Bob", 40)
        ));

        // Define an in-place enricher
        BiConsumer<Person, Person> enricher = (p1, p2) -> {
            p1.addHobby("Cooking");
            p1.setAge(p1.getAge() + 1);
        };

        // Enrich the list in-place
        List<Person> enrichedPersons = Enrich.listInPlace(persons, enricher);

        // Verify enrichment
        assertEquals(3, enrichedPersons.size());
        for (Person p : enrichedPersons) {
            assertTrue(p.getHobbies().contains("Cooking"));
            // Age should be incremented
            assertTrue(p.getAge() > 25);
        }
        
        // Verify it's the same list
        assertSame(persons, enrichedPersons);
    }

    @Test
    void testCollectionEnrichment() {
        // Create a set of persons
        Set<Person> persons = new HashSet<>(Arrays.asList(
            new Person("John", 30),
            new Person("Jane", 25),
            new Person("Bob", 40)
        ));

        // Define an enricher function
        Function<Person, Person> enricher = p -> {
            p.addHobby("Reading");
            return p;
        };

        // Enrich the collection
        Set<Person> enrichedPersons = Enrich.collection(persons, enricher);

        // Verify enrichment
        assertEquals(3, enrichedPersons.size());
        for (Person p : enrichedPersons) {
            assertTrue(p.getHobbies().contains("Reading"));
        }
        
        // Verify it's the same collection
        assertSame(persons, enrichedPersons);
    }

    @Test
    void testMapValuesEnrichment() {
        // Create a map of persons
        Map<String, Person> personMap = new HashMap<>();
        personMap.put("person1", new Person("John", 30));
        personMap.put("person2", new Person("Jane", 25));
        personMap.put("person3", new Person("Bob", 40));

        // Define an enricher function
        Function<Person, Person> enricher = p -> {
            p.addHobby("Reading");
            return p;
        };

        // Enrich the map values
        Map<String, Person> enrichedMap = Enrich.mapValues(personMap, enricher);

        // Verify enrichment
        assertEquals(3, enrichedMap.size());
        for (Person p : enrichedMap.values()) {
            assertTrue(p.getHobbies().contains("Reading"));
        }
        
        // Verify it's the same map
        assertSame(personMap, enrichedMap);
    }
}