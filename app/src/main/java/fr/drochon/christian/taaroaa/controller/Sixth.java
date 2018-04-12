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

    /**
     * Contructeur par defaut avec 6 params generiques
     * @param f
     * @param s
     * @param t
     * @param u
     * @param v
     * @param w
     */
    private Sixth(F f, S s, T t, U u, V v, W w) {
        this.first = f;
        this.second = s;
        this.third = t;
        this.fourth = u;
        this.fifth = v;
        this.sixth = w;
    }

    /**
     * onvenience method for creating an appropriately typed sixth.
     * @param a 1er param de la methode
     * @param b 2e param
     * @param c 3e param
     * @param d 4e apram
     * @param e 5e apram
     * @param f 6e param
     * @param <A> 1er param generique
     * @param <B> 2e param gen
     * @param <C> 3e param gen
     * @param <D> 4e param gen
     * @param <E> 5e param gen
     * @param <F> 6e param gen
     * @return Sixth
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
