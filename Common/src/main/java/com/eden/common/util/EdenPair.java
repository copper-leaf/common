package com.eden.common.util;

import java.util.Objects;

public class EdenPair<F, S> {
    public final F first;
    public final S second;

    /**
     * Constructor for a EdenPair.
     *
     * @param first the first object in the EdenPair
     * @param second the second object in the EdenPair
     */
    public EdenPair(F first, S second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Checks the two objects for equality by delegating to their respective
     * {@link Object#equals(Object)} methods.
     *
     * @param o the {@link EdenPair} to which this one is to be checked for equality
     * @return true if the underlying objects of the EdenPair are both considered
     *         equal
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof EdenPair)) {
            return false;
        }
        EdenPair<?, ?> p = (EdenPair<?, ?>) o;
        return Objects.equals(p.first, first) && Objects.equals(p.second, second);
    }

    /**
     * Compute a hash code using the hash codes of the underlying objects
     *
     * @return a hashcode of the EdenPair
     */
    @Override
    public int hashCode() {
        return (first == null ? 0 : first.hashCode()) ^ (second == null ? 0 : second.hashCode());
    }

    @Override
    public String toString() {
        return "EdenPair{" + String.valueOf(first) + " " + String.valueOf(second) + "}";
    }

    /**
     * Convenience method for creating an appropriately typed EdenPair.
     * @param a the first object in the EdenPair
     * @param b the second object in the EdenPair
     * @param <A> the type of the first object in the EdenPair
     * @param <B> the type of the second object in the EdenPair
     * @return a EdenPair that is templatized with the types of a and b
     */
    public static <A, B> EdenPair <A, B> create(A a, B b) {
        return new EdenPair<A, B>(a, b);
    }
}
