package fr.drochon.christian.taaroaa.controller;

import android.support.annotation.NonNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class Sixth<F, S, T, U, V, W> {

    public final F first;
    public final S second;
    public final U fourth;
    public final V fifth;
    public final W sixth;
    public T third;


    private Sixth(F f, S s, T t, U u, V v, W w) {
        this.first = f;
        this.second = s;
        this.third = t;
        this.fourth = u;
        this.fifth = v;
        this.sixth = w;
    }

    /**
     * Convenience method for creating an appropriately typed pair.
     *
     * @param a the first object in the Pair
     * @param b the second object in the pair
     * @return a Pair that is templatized with the types of a and b
     */
    public static <A, B, C, D, E, F> Sixth<A, B, C, D, E, F> create(A a, B b, C c, D d, E e, F f) {
        return new Sixth<A, B, C, D, E, F>(a, b, c, d, e, f);
    }

    public Sixth<T, T, T, T, T, T> get(int position) {
        return null;
    }

    public int size() {
        return 0;
    }

    public boolean add(Sixth sixth) {
        return false;
    }
}
