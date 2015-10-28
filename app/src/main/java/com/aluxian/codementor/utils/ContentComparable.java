package com.aluxian.codementor.utils;

public interface ContentComparable<T> extends Comparable<T> {

    /**
     * Compares this object to the specified object to determine whether their content is the same.
     *
     * @param another The object to compare to this instance.
     * @return True if the content of the two instances is the same.
     */
    boolean compareContentTo(T another);

}
