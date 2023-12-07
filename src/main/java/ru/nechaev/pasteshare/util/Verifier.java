package ru.nechaev.pasteshare.util;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * Dозращает название null полей объекта.
 * Нужен для обновления сущностей
 */
@Component
public class Verifier {
    public <T> String[] getNullPropertyName(T source) {
        BeanWrapper src = new BeanWrapperImpl(source);
        java.beans.PropertyDescriptor[] propertyDescriptors = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<>();
        for (java.beans.PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            Object srcValue = src.getPropertyValue(propertyDescriptor.getName());
            if (srcValue == null) emptyNames.add(propertyDescriptor.getName());
        }

        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }
}
