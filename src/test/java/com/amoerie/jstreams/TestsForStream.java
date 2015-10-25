package com.amoerie.jstreams;

import static com.amoerie.jstreams.TestModels.makeFruitBasket;
import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import com.amoerie.jstreams.TestModels.Apple;
import com.amoerie.jstreams.TestModels.Fruit;
import com.amoerie.jstreams.TestModels.FruitBasket;
import com.amoerie.jstreams.functions.Filter;
import com.amoerie.jstreams.functions.Mapper;

@RunWith(Enclosed.class)
public class TestsForStream {
    private static Mapper<Fruit, String> getFruitName = new Mapper<Fruit, String>() {
        @Override
        public String map(Fruit element) {
            return element.getName();
        }
    };

    /* static method tests (alphabetically) */

    public static class TestsForCreate {

        @Test
        public void shouldReturnAStreamContainingAllTheProvidedElements() {
            List<String> fruits = Stream.create(new String[]{"Apple", "Pear", "Banana"}).toList();
            assertThat(fruits, hasItem("Apple"));
            assertThat(fruits, hasItem("Pear"));
            assertThat(fruits, hasItem("Banana"));
        }

        @Test(expected = IllegalArgumentException.class)
        public void shouldThrowAnIllegalArgumentExceptionIfTheArrayIsNull() {
            Stream.create((Object[]) null);
        }

        @Test
        public void shouldWorkWithAVariableAmountOfArguments() {
            Stream<String> strings = Stream.create("abc", "def");
            assertThat(strings.toList(), is(Arrays.asList("abc", "def")));
        }

    }

    public static class TestsForEmpty {

        @Test
        public void shouldCreateEmptyStream() {
            assertThat(Stream.<Apple>empty().toList(), is(Collections.<Apple>emptyList()));
        }

        @Test(expected = NoSuchElementException.class)
        public void iteratorShouldThrowNoSuchElementException() {
            Stream.<String>empty().iterator().next();
        }

        @Test
        public void iteratorShouldNotHaveElements() {
            assertFalse(Stream.<String>empty().iterator().hasNext());
        }

    }

    public static class TestsForSingleton {
        @Test
        public void shouldCreateStreamWithOneElement() {
            Apple element = new Apple();
            assertThat(Stream.singleton(element).toSet(), is(Collections.singleton(element)));
        }

        @Test
        public void iteratorShouldReturnSingleItemOnTheFirstNext() {
            Iterator<Apple> iterator = Stream.singleton(new Apple()).iterator();
            assertThat(iterator.next(), is(notNullValue()));
        }

        @Test
        public void iteratorShouldReturnTrueForTheFirstHasNext() {
            Iterator<Apple> iterator = Stream.singleton(new Apple()).iterator();
            assertTrue(iterator.hasNext());
        }

        @Test(expected = NoSuchElementException.class)
        public void iteratorShouldThrowNoSuchElementExceptionOnTheSecondNext() {
            Iterator<String> iterator = Stream.<String>empty().iterator();
            iterator.next();
            iterator.next();
        }

        @Test
        public void iteratorShouldReturnFalseOnTheSecondHasNext() {
            Iterator<Apple> iterator = Stream.singleton(new Apple()).iterator();
            assertTrue(iterator.hasNext());
            iterator.next();
            assertFalse(iterator.hasNext());
        }

    }

    /* instance method tests (alphabetically) */

    public static class TestsForAny {
        @Test
        public void shouldReturnTrueIfAPearIfPresent() {
            FruitBasket fruits = makeFruitBasket(new Fruit("apple"), new Fruit("pear"), new Fruit("pineapple"));
            assertTrue(fruits.asStream().any(new Filter<Fruit>() {
                @Override
                public boolean apply(Fruit fruit) {
                    return fruit.getName().equals("pear");
                }
            }));
        }

        @Test
        public void shouldReturnFalseIfNoPearIfPresent() {
            FruitBasket fruits = makeFruitBasket(new Fruit("apple"), new Fruit("banana"));
            assertFalse(fruits.asStream().any(new Filter<Fruit>() {
                @Override
                public boolean apply(Fruit fruit) {
                    return fruit.getName().equals("pear");
                }
            }));
        }

        @Test
        public void shouldReturnTrueIfTheListIsInfinitelyLongButThePearIsPresent() {
            Stream<Fruit> infiniteFruits // solving world hunger, one pear at a time
                    = new InfiniteStream<Fruit>(new Fruit("pear"));
            assertTrue(infiniteFruits.any(new Filter<Fruit>() {
                @Override
                public boolean apply(Fruit fruit) {
                    return fruit.getName().equals("pear");
                }
            }));
        }

        @Test
        public void shouldReturnTrueIfThePredicateChecksForNull() {
            Stream<String> strings = Stream.create("abc", null, "def");
            assertThat(strings.any(new Filter<String>() {
                @Override
                public boolean apply(String s) {
                    return s == null;
                }
            }), is(true));
        }
    }

    public static class TestsForCast {
        @Test
        public void shouldCastEveryFruitToAnApple() {
            TestModels.Fruit[] fruits = {new Apple(), new Apple()};
            FruitBasket fruitBasket = makeFruitBasket(fruits);
            Iterable<Apple> apples = fruitBasket.asStream().cast(Apple.class).toList();
            assertThat(apples, everyItem(CoreMatchers.<Apple>instanceOf(Apple.class)));
        }

        @Test
        public void shouldCastEmptyStreamToEmptyStream() {
            Stream<Fruit> fruits = Stream.empty();
            Stream<Apple> apples = fruits.cast(Apple.class);
            assertThat(apples.toList(), is(Stream.<Apple>empty().toList()));
        }
    }

    public static class TestsForConcat {

        @Test
        public void shouldReturnAStreamContainingElementsOfBothStreams() {
            Apple apple1 = new Apple();
            Apple apple2 = new Apple();
            Fruit pear = new Fruit("pear");
            Stream<Fruit> apples = makeFruitBasket(apple1, apple2).asStream();
            Stream<Fruit> otherFruits = makeFruitBasket(pear).asStream();
            Stream<Fruit> allFruits = apples.concat(otherFruits);
            List<Fruit> allFruitsList = allFruits.toList();
            assertThat(allFruitsList.size(), is(3));
            assertThat(allFruitsList, hasItem(apple1));
            assertThat(allFruitsList, hasItem(apple2));
            assertThat(allFruitsList, hasItem(pear));
        }

        @Test
        public void shouldIgnoreEmptyStreams() {
            Set<String> singleton = Stream.singleton("abc")
                    .concat(Stream.<String>empty())
                    .concat(Stream.<String>empty())
                    .toSet();
            assertThat(singleton, is(Collections.singleton("abc")));

        }

    }

    public static class TestsForDistinct {

        @Test
        public void shouldReturnAnEmptyStreamIfTheStreamIsEmpty() {
            assertThat(Stream.<String>empty().distinct().toList(), is(Collections.<String>emptyList()));
        }

        @Test
        public void shouldCorrectlyFilterOutTheDuplicateElements() {
            List<String> fruits = Arrays.asList("Pear", "Apple", "Banana", "Pear", "Banana", "Pineapple");
            List<String> distinctFruits = Stream.create(fruits).distinct().toList();
            List<String> expectedFruits = Arrays.asList("Pear", "Apple", "Banana", "Pineapple");
            assertThat(distinctFruits, is(expectedFruits));
        }

        @Test
        public void shouldCorrectlyDistinctFromAnInfiniteStream() {
            Stream<String> infiniteFruits = Stream.create(Arrays.asList("Pear", "Apple", "Banana", "Pear", "Banana", "Pineapple"))
                    .concat(new InfiniteStream<String>("Grape"));
            List<String> distinctFruits = infiniteFruits.distinct().take(4).toList();
            List<String> expectedFruits = Arrays.asList("Pear", "Apple", "Banana", "Pineapple");
            assertThat(distinctFruits, is(expectedFruits));
        }

    }

    public static class TestsForGroupBy {

        @Test
        public void shouldBeAbleToGroupEmptyStream() {
            Stream<String> emptyStream = Stream.empty();
            Stream<Group<String, String>> emptyGroupedStream = Stream.empty();
            assertThat(emptyStream.groupBy(new Mapper<String, String>() {
                @Override
                public String map(String s) {
                    return s;
                }
            }).toList(), is(emptyGroupedStream.toList()));
        }

        @Test
        public void shouldBeAbleToGroupWithNullKeys() {
            Fruit apple = new Fruit("apple");
            Fruit pear = new Fruit("pear");
            Fruit nothing = new Fruit(null);
            FruitBasket fruitBasket = makeFruitBasket(apple, pear, nothing);
            List<Group<String, Fruit>> groups = fruitBasket.asStream()
                    .groupBy(getFruitName).toList();
            assertThat(groups.size(), is(3));
            assertThat(groups.get(0).getKey(), is("apple"));
            assertThat(groups.get(1).getKey(), is("pear"));
            assertThat(groups.get(2).getKey(), is((String) null));
            assertThat(groups.get(0).toList().get(0), is(apple));
            assertThat(groups.get(1).toList().get(0), is(pear));
            assertThat(groups.get(2).toList().get(0), is(nothing));
        }

        @Test
        public void shouldCorrectlyGroupMultipleItemsWithTheSameKey() {
            Fruit apple = new Fruit("apple");
            Fruit secondApple = new Fruit("apple");
            Fruit banana = new Fruit("banana");
            FruitBasket fruitBasket = makeFruitBasket(apple, secondApple, banana);
            List<Group<String, Fruit>> groups = fruitBasket.asStream()
                    .groupBy(getFruitName).toList();

            assertThat(groups.size(), is(2));
            assertThat(groups.get(0).getKey(), is("apple"));
            assertThat(groups.get(1).getKey(), is("banana"));
            assertThat(groups.get(0).toList().get(0), is(apple));
            assertThat(groups.get(0).toList().get(1), is(secondApple));
            assertThat(groups.get(1).toList().get(0), is(banana));
        }

    }

    public static class TestsForFilter {
        private static final List<Fruit> fruitList = Arrays.asList(new Fruit("banana"),
                new Fruit("apple"),
                new Fruit("pear"),
                new Fruit("pineapple"),
                new Fruit("strawberry"),
                new Fruit("grapes"),
                new Fruit("kiwi"));

        private static final Stream<Fruit> fruitBasket = Stream.create(fruitList);

        @Test
        public void noFilterShouldReturnAllFruits() {
            List<Fruit> allFruits = fruitBasket.filter(new Filter<Fruit>() {
                @Override
                public boolean apply(Fruit fruit) {
                    return true;
                }
            }).toList();
            assertThat(allFruits, is(fruitList));
        }

        @Test
        public void onlyFruitsThatStartWithPShouldReturn2Fruits() {
            List<Fruit> pFruits = fruitBasket.filter(new Filter<Fruit>() {
                @Override
                public boolean apply(Fruit fruit) {
                    return fruit.getName().startsWith("p");
                }
            }).toList();
            assertThat(pFruits, is(Arrays.asList(new Fruit[]{
                    new Fruit("pear"),
                    new Fruit("pineapple"),
            })));
        }

        @Test
        public void filterThatAlwaysReturnsFalseShouldReturnNoFruits() {
            List<Fruit> fruits = fruitBasket.filter(new Filter<Fruit>() {
                @Override
                public boolean apply(Fruit fruit) {
                    return false;
                }
            }).toList();
            assertThat(fruits, is(Collections.<Fruit>emptyList()));
        }

        @Test
        public void callingHasNextMultipleTimesShouldNotSkipElements() {
            final Iterator<Fruit> fruitIterator = Stream.create(fruitBasket).filter(new Filter<Fruit>() {
                @Override
                public boolean apply(Fruit fruit) {
                    return fruit.getName().startsWith("p");
                }
            }).iterator();
            assertTrue(fruitIterator.hasNext());
            assertTrue(fruitIterator.hasNext());

            List<Fruit> fruitList = new IterableStream<Fruit>(new Iterable<Fruit>() {
                @Override
                public Iterator<Fruit> iterator() {
                    return fruitIterator;
                }
            }).toList();
            assertThat(fruitList, is(Arrays.asList(new Fruit[]{
                    new Fruit("pear"),
                    new Fruit("pineapple"),
            })));
        }

        @Test
        public void callingNextMultipleTimesShouldNotSkipElements() {
            final Iterator<Fruit> fruitIterator = Stream.create(fruitBasket).filter(new Filter<Fruit>() {
                @Override
                public boolean apply(Fruit fruit) {
                    return fruit.getName().startsWith("p");
                }
            }).iterator();
            assertThat(fruitIterator.next(), is(new Fruit("pear")));
            assertThat(fruitIterator.next(), is(new Fruit("pineapple")));
            assertFalse(fruitIterator.hasNext());
        }

        @Test
        public void filteringOnNullsShouldWork() {
            final Iterator<Fruit> fruitIterator = Stream.create(fruitBasket)
                    .concat(Stream.singleton((Fruit) null))
                    .filter(new Filter<Fruit>() {
                        @Override
                        public boolean apply(Fruit fruit) {
                            return fruit == null;
                        }
                    })
                    .iterator();
            assertThat(fruitIterator.next(), is((Fruit) null));
            assertFalse(fruitIterator.hasNext());
        }
    }

    public static class TestsForFirst {

        @Test
        public void shouldReturnNullOnAnEmptyStream() {
            assertThat(Stream.<String>empty().first(), is((String) null));
        }

        @Test
        public void shouldReturnTheFirstElementOnANonEmptyStream() {
            assertThat(Stream.create("abc", "def").first(), is("abc"));
        }

        @Test
        public void shouldReturnTheFirstElementOnAnInfiniteStream() {
            assertThat(new InfiniteStream<String>("abc").first(), is("abc"));
        }

    }

    public static class TestsForFlatMap {
        @Test
        public void anEmptyFruitBasketShouldFlatMapToNoFruits() {
            List<Fruit> fruits = Stream.create(Collections.singletonList(makeFruitBasket()))
                    .flatMap(new Mapper<FruitBasket, Stream<Fruit>>() {
                        @Override
                        public Stream<Fruit> map(FruitBasket fruitBasket) {
                            return fruitBasket.asStream();
                        }
                    })
                    .toList();
            assertThat(fruits, is(Collections.<Fruit>emptyList()));
        }

        @Test
        public void oneFruitBasketShouldFlatMapToAllFruitsInTheBasket() {
            List<FruitBasket> allBaskets = Collections.singletonList(makeFruitBasket(new Fruit("apple"), new Fruit("pear")));
            List<Fruit> allFruits = Stream.create(allBaskets).flatMap(new Mapper<FruitBasket, Stream<Fruit>>() {
                @Override
                public Stream<Fruit> map(FruitBasket fruitBasket) {
                    return fruitBasket.asStream();
                }
            }).toList();
            assertThat(allFruits, is(makeFruitBasket(new Fruit("apple"), new Fruit("pear")).asList()));
        }

        @Test
        public void twoFruitBasketsShouldFlatMapToTheFruitsInBothBaskets() {
            List<FruitBasket> allBaskets = Arrays.asList(
                    makeFruitBasket(new Fruit("apple"), new Fruit("pear")),
                    makeFruitBasket(new Fruit("strawberries")));
            List<Fruit> allFruits = Stream.create(allBaskets).flatMap(new Mapper<FruitBasket, Stream<Fruit>>() {
                @Override
                public Stream<Fruit> map(FruitBasket fruitBasket) {
                    return fruitBasket.asStream();
                }
            }).toList();
            assertThat(allFruits, is(makeFruitBasket(new Fruit("apple"), new Fruit("pear"), new Fruit("strawberries")).asList()));
        }

        @Test
        public void threeFruitBasketsWithOneEmptyShouldStillMapToAllTheFruits() {
            List<FruitBasket> allBaskets = Arrays.asList(
                    makeFruitBasket(new Fruit("apple"), new Fruit("pear")),
                    makeFruitBasket(),
                    makeFruitBasket(new Fruit("strawberries")));
            List<Fruit> allFruits = Stream.create(allBaskets).flatMap(new Mapper<FruitBasket, Stream<Fruit>>() {
                @Override
                public Stream<Fruit> map(FruitBasket fruitBasket) {
                    return fruitBasket.asStream();
                }
            }).toList();
            assertThat(allFruits, is(makeFruitBasket(new Fruit("apple"), new Fruit("pear"), new Fruit("strawberries")).asList()));
        }
    }

    public static class TestsForJoin {

        @Test
        public void shouldBeEmpyOnEmptyStream() {
            String joined = Stream.empty().join(",");
            assertTrue(joined.isEmpty());
        }

        @Test
        public void shouldNotInsertDelimiterOnSingletonStream() {
            String joined = Stream.singleton("xyz").join(".");
            assertThat(joined, is("xyz"));
        }

        @Test
        public void shouldSupportEmptyDelimiter() {
            String joined = Stream.of(new String[] {"ab", "cd", "efg" }).join("");
            assertThat(joined, is("abcdefg"));
        }

        @Test
        public void shouldInsertDelimiter() {
            String joined = new InfiniteStream<Integer>(1).take(5).join(",");
            assertThat(joined, is("1,1,1,1,1"));
        }

		@Test
		public void shouldSupportEmptyStrings() {
			String joined = Stream.of("", "", "A", "").join(":");
			assertThat(joined, is("::A:"));
		}
    }

    public static class TestsForLast {

        @Test
        public void shouldReturnNullForEmptyStreams() {
            assertThat(Stream.<String>empty().last(), is((String) null));
        }

        @Test
        public void shouldTakeTheLastElementIfTheStreamIsNotEmpty() {
            assertThat(Stream.create(new String[]{"Johnny", "Freddy", "Ringo"}).last(), is("Ringo"));
        }

    }

    public static class TestsForLength {
        @Test
        public void shouldReturn0ForEmptyStreams() {
            assertThat(Stream.<String>empty().length(), is(0));
        }

        @Test
        public void shouldReturnActualLengthForNonEmptyStreams() {
            assertThat(Stream.create(Arrays.asList("one", "two", "three", "four", "five")).length(), is(5));
        }
    }

    public static class TestsForLimit {
        @Test
        public void shouldReturnEmptyStreamIfNumberIs0() {
            Set<String> strings = Stream.singleton("abc").limit(0).toSet();
            assertThat(strings, is(Collections.<String>emptySet()));
        }

        @Test
        public void shouldTake3ElementsWhenTheNumberIs3() {
            List<String> actualStrings = Stream.create(Arrays.asList("one", "two", "three", "four", "five")).limit(3).toList();
            List<String> expectedStrings = Arrays.asList("one", "two", "three");
            assertThat(actualStrings, is(expectedStrings));
        }
    }

    public static class TestsForMap {
        @Test
        public void anEmptyFruitBasketShouldMapToNoFruitNames() {
            List<String> fruitNames = Stream.create(new ArrayList<Fruit>())
                    .map(getFruitName)
                    .toList();
            assertThat(fruitNames, is(Collections.<String>emptyList()));
        }

        @Test
        public void aFilledFruitBasketShouldMapToTheirNames() {
            List<String> fruitNames = makeFruitBasket(new Fruit("apple"), new Fruit("pear")).asStream()
                    .map(getFruitName)
                    .toList();
            assertThat(fruitNames, is(Arrays.asList(new String[]{"apple", "pear"})));
        }
    }

    public static class TestsForOfClass {

        @Test
        public void shouldHandleEmptyStreamsCorrectly() {
            Stream<Apple> apples = Stream.<Fruit>empty().ofClass(Apple.class);
            assertThat(apples.toList(), is(Stream.<Apple>empty().toList()));
        }

        @Test
        public void shouldCorrectlyFilterOutApplesFromTheFruitBasket() {
            Apple apple = new Apple();
            FruitBasket assortedFruits = makeFruitBasket(new Fruit("banana"), apple, new Fruit("pear"));
            Stream<Apple> apples = assortedFruits.asStream().ofClass(Apple.class);
            assertThat(apples.toList(), is(Stream.singleton(apple).toList()));
        }

    }

    public static class TestsForSome {
        @Test
        public void shouldReturnTrueIfAPearIfPresent() {
            FruitBasket fruits = makeFruitBasket(new Fruit("apple"), new Fruit("pear"), new Fruit("pineapple"));
            assertTrue(fruits.asStream().some(new Filter<Fruit>() {
                @Override
                public boolean apply(Fruit fruit) {
                    return fruit.getName().equals("pear");
                }
            }));
        }

        @Test
        public void shouldReturnFalseIfNoPearIfPresent() {
            FruitBasket fruits = makeFruitBasket(new Fruit("apple"), new Fruit("banana"));
            assertFalse(fruits.asStream().some(new Filter<Fruit>() {
                @Override
                public boolean apply(Fruit fruit) {
                    return fruit.getName().equals("pear");
                }
            }));
        }

        @Test
        public void shouldReturnTrueIfTheListIsInfinitelyLongButThePearIsPresent() {
            Stream<Fruit> infiniteFruits // solving world hunger, one pear at a time
                    = new InfiniteStream<Fruit>(new Fruit("pear"));
            assertTrue(infiniteFruits.some(new Filter<Fruit>() {
                @Override
                public boolean apply(Fruit fruit) {
                    return fruit.getName().equals("pear");
                }
            }));
        }

        @Test
        public void shouldReturnTrueIfThePredicateChecksForNull() {
            Stream<String> strings = Stream.create("abc", null, "def");
            assertThat(strings.some(new Filter<String>() {
                @Override
                public boolean apply(String s) {
                    return s == null;
                }
            }), is(true));
        }
    }

    public static class TestsForSortBy {

        @Test
        public void shouldSortByNameCorrectly() {
            List<Fruit> fruits = Arrays.asList(new Fruit("orange"), new Fruit("banana"), new Fruit("pear"));
            List<Fruit> expectedSortedFruits = Arrays.asList(new Fruit("banana"), new Fruit("orange"), new Fruit("pear"));
            List<Fruit> sortedFruits = Stream.create(fruits).sortBy(getFruitName).toList();
            assertThat(sortedFruits, is(expectedSortedFruits));
        }

    }

    public static class TestsForSortByDescending {

        @Test
        public void shouldSortByNameCorrectly() {
            List<Fruit> fruits = Arrays.asList(new Fruit("orange"), new Fruit("banana"), new Fruit("pear"));
            List<Fruit> expectedSortedFruits = Arrays.asList(new Fruit("pear"), new Fruit("orange"), new Fruit("banana"));
            List<Fruit> sortedFruits = Stream.create(fruits).sortByDescending(getFruitName).toList();
            assertThat(sortedFruits, is(expectedSortedFruits));
        }

    }

    public static class TestsForSort {

        @Test
        public void shouldSortByNameCorrectly() {
            List<Fruit> fruits = Arrays.asList(new Fruit("orange"), new Fruit("banana"), new Fruit("pear"));
            List<Fruit> expectedSortedFruits = Arrays.asList(new Fruit("banana"), new Fruit("orange"), new Fruit("pear"));
            List<Fruit> sortedFruits = Stream.create(fruits).sort(new Comparator<Fruit>() {
                @Override
                public int compare(Fruit left, Fruit right) {
                    return left.getName().compareTo(right.getName());
                }
            }).toList();
            assertThat(sortedFruits, is(expectedSortedFruits));
        }

    }

    public static class TestsForSkip {

        @Test(expected = IllegalArgumentException.class)
        public void shouldThrowExceptionWhenSkippingANegativeAmount() {
            Stream.singleton("abc").skip(-1);
        }

        @Test
        public void shouldNotSkipAnythingWheNumberIsZero() {
            Set<String> strings = Stream.singleton("abc").skip(0).toSet();
            assertThat(strings, is(Collections.singleton("abc")));
        }

        @Test
        public void shouldSkip3ElementsWhenTheNumberIs3() {
            List<String> actualStrings = Stream.create(Arrays.asList("one", "two", "three", "four", "five")).skip(3).toList();
            List<String> expectedStrings = Arrays.asList("four", "five");
            assertThat(actualStrings, is(expectedStrings));
        }
    }

    public static class TestsForTake {
        @Test
        public void shouldReturnEmptyStreamIfTakeIs0() {
            Set<String> strings = Stream.singleton("abc").take(0).toSet();
            assertThat(strings, is(Collections.<String>emptySet()));
        }

        @Test
        public void shouldTakeElementsWhenTheNumberIs3() {
            List<String> actualStrings = Stream.create(Arrays.asList("one", "two", "three", "four", "five")).take(3).toList();
            List<String> expectedStrings = Arrays.asList("one", "two", "three");
            assertThat(actualStrings, is(expectedStrings));
        }
    }

    public static class TestsForToMap {

        @Test
        public void shouldCreateAnEmptyMapFromAnEmptyStream() {
            Map<String, Fruit> map = Stream.<Fruit>empty().toMap(getFruitName);
            assertTrue(map.isEmpty());
        }

        @Test
        public void shouldCreateACorrectMapByFruitName() {
            Map<String, Fruit> map = makeFruitBasket(new Fruit("apple"), new Fruit("pear"), new Fruit("banana")).asStream().toMap(getFruitName);
            assertThat(map.get("apple"), is(new Fruit("apple")));
            assertThat(map.get("pear"), is(new Fruit("pear")));
            assertThat(map.get("banana"), is(new Fruit("banana")));
            assertThat(map.size(), is(3));
        }

        @Test
        public void shouldCreateACorrectMapByFruitNameAndNameLength() {
            Map<String, Integer> map = makeFruitBasket(new Fruit("apple"), new Fruit("pear"), new Fruit("banana"))
                    .asStream()
                    .toMap(getFruitName, new Mapper<Fruit, Integer>() {
                        @Override
                        public Integer map(Fruit fruit) {
                            return fruit.getName().length();
                        }
                    });
            assertThat(map.get("apple"), is(5));
            assertThat(map.get("pear"), is(4));
            assertThat(map.get("banana"), is(6));
            assertThat(map.size(), is(3));
        }

    }

    public static class TestsForWithout {

        @Test
        public void shouldNotHaveAnyImpactWhenTheStreamToFilterWithIsEmpty() {
            List<String> names = Stream.create(new String[]{"abc", "def", "xyz"}).without(Stream.<String>empty()).toList();
            assertThat(names, is(Arrays.asList("abc", "def", "xyz")));
        }

        @Test
        public void shouldCorrectlyFilterOutTheNamesFromTheOtherStream() {
            List<String> names = Stream.create(new String[]{"abc", "def", "xyz"}).without(Stream.create(Arrays.asList("def", "abc"))).toList();
            assertThat(names, is(Arrays.asList("xyz")));
        }
    }
}