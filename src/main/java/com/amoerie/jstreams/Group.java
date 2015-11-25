package com.amoerie.jstreams;


/**
 * Represents a grouping of elements.
 * A group is essentially just another stream with an extra 'key' property.
 * @param <TKey> the type of the key
 * @param <E> the type of the elements
 */
public abstract class Group<TKey, E> extends Stream<E> {
    /**
     * Gets the key of this group
     * @return the key of this group
     */
    public abstract TKey getKey();
}
