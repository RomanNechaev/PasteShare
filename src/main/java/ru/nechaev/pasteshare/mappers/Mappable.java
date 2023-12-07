package ru.nechaev.pasteshare.mappers;

import java.util.List;

public interface Mappable<T, E> {
    T toDto(E entity);

    List<T> toListDto(List<E> entities);

}
