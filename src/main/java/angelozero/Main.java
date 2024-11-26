package angelozero;

import angelozero.model.ClassTypeEnum;
import angelozero.model.Person;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Gatherer;

public class Main {

    private static final int LIMIT = 2;

    public static void main(String[] args) {

        var personList = Arrays.asList(
                new Person("Angelo", 34, ClassTypeEnum.MASTER),
                new Person("Jake  ", 15, ClassTypeEnum.USER),
                new Person("Test-A", 10, ClassTypeEnum.ADMIN),
                new Person("Test-B", 20, ClassTypeEnum.ADMIN),
                new Person("Test-C", 30, ClassTypeEnum.ADMIN),
                new Person("Test-D", 40, ClassTypeEnum.ADMIN),
                new Person("Test-E", 50, ClassTypeEnum.ADMIN),
                new Person("Test-F", 60, ClassTypeEnum.ADMIN),
                new Person("Test-G", 99, ClassTypeEnum.ADMIN)
        );

        var adminPersons = personList.stream()
                .filter(person -> person.classTypeEnum().equals(ClassTypeEnum.ADMIN))
                .map(Person::name)
                .toList();

        System.out.println(adminPersons);

        /* Using Gatherer */
        var gathererWithList = getGathererWithList();

        /* Using Gatherer List */
        var adminPersonsWithGathererList = personList.stream()
                .filter(person -> person.classTypeEnum().equals(ClassTypeEnum.ADMIN))
                .map(Person::name)
                .gather(gathererWithList)
                .toList();
        System.out.println(adminPersonsWithGathererList);

        /* Using Gatherer List and Map */
        var adminPersonsWithGathererMap = personList.stream()
                .filter(person -> person.classTypeEnum().equals(ClassTypeEnum.ADMIN))
                .gather(getGathererWithMap(Person::name))
                .gather(gathererWithList)
                .toList();
        System.out.println(adminPersonsWithGathererMap);
    }

    private static <T> Gatherer<T, List<T>, List<T>> getGathererWithList() {
        Supplier<List<T>> initializer = ArrayList::new;
        Gatherer.Integrator<List<T>, T, List<T>> integrator = (state, element, downstream) -> {
            state.add(element);
            if (state.size() == LIMIT) {
                var group = List.copyOf(state);
                downstream.push(group);
                state.clear();
                // At the end whithout finisher the log will be
                // [[Test-A, Test-B], [Test-C, Test-D], [Test-E, Test-F]]
                // But the expected result is
                // [[Test-A, Test-B], [Test-C, Test-D], [Test-E, Test-F], [Test-G]]
                // Because "Test-G" is an ADMIN
            }
            return true;
        };

        // The function of the finisher will tell us that if there are any remaining elements in the list, they need to be added.
        BiConsumer<List<T>, Gatherer.Downstream<? super List<T>>> finisher = (state, downStream) -> {
            if (!state.isEmpty()) {
                downStream.push(List.copyOf(state));
            }
        };

        // whithout finisher
        // return Gatherer.ofSequential(initializer, integrator);

        // with finisher
        return Gatherer.ofSequential(initializer, integrator, finisher);
    }

    private static <T, R> Gatherer<T, ?, R> getGathererWithMap(Function<T, R> mapper) {
        Gatherer.Integrator<Void, T, R> integrator = (_, element, downStream) -> {
            R newElement = mapper.apply(element);
            downStream.push(newElement);
            return true;
        };

        return Gatherer.of(integrator);
    }
}