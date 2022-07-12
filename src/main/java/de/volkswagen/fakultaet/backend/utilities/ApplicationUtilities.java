package de.volkswagen.fakultaet.backend.utilities;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Component;

import java.beans.FeatureDescriptor;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ApplicationUtilities {
    private ApplicationUtilities() {}

    public static String[] getNullPropertyNames(Object object) {
        final BeanWrapper source = new BeanWrapperImpl(object);
        return Arrays.stream(source.getPropertyDescriptors())
                .map(FeatureDescriptor::getName)
                .filter(name -> source.getPropertyValue(name) == null)
                .collect(Collectors.toList())
                .toArray(new String[0]);
    }
    public static List<String> getNullPropertyNamesAsList(Object object) {
        final BeanWrapper source = new BeanWrapperImpl(object);
        return Arrays.stream(source.getPropertyDescriptors())
                .map(FeatureDescriptor::getName)
                .filter(name -> source.getPropertyValue(name) == null)
                .collect(Collectors.toList());
    }
}
