package io.github.lwlee2608.proto.gen;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class ClassFinder {
    private static final Logger logger = LoggerFactory.getLogger(ClassFinder.class);

    @SuppressWarnings("unchecked")
    public static synchronized <T> Class<T> findClass(Class<T> targetClass, String prefix) {
        String className = targetClass.getSimpleName();

        Reflections reflections = new Reflections(prefix);
        Set<Class<? extends T>> classes = reflections.getSubTypesOf(targetClass);
        if (classes.size() > 1) {
            logger.warn("Multiple subClasses found for {}", className);
        }

        // If not found, use default
        Class<?> clazz = classes.stream().findFirst().orElse(null);
        if (clazz == null) {
            logger.info("No subClass found for {}", className);
            clazz = targetClass;
        } else {
            logger.info("Found {}", clazz.getName());
        }

        return (Class<T>) clazz;
    }
}
