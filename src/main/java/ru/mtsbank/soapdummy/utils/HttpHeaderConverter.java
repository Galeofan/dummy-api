package ru.mtsbank.soapdummy.utils;

import java.util.Enumeration;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Converter for http headers
 */
public class HttpHeaderConverter {

    private HttpHeaderConverter() {
        throw new UnsupportedOperationException("Class for util only");
    }

    /**
     * Convert http headers to {@link Stream}
     * @param enumeration headers in enumeration object
     * @param <T> type of header
     * @return {@link Stream} of headers
     */
    public static <T> Stream<T> convert(Enumeration<T> enumeration) {
        EnumerationSpliterator<T> spliterator
            = new EnumerationSpliterator<>(Long.MAX_VALUE, Spliterator.ORDERED, enumeration);
        return StreamSupport.stream(spliterator, false);
    }

    /**
     * Spliterator for {@link Enumeration}
     * @param <T> type of content
     */
    public static class EnumerationSpliterator<T> extends Spliterators.AbstractSpliterator<T> {

        private final Enumeration<T> enumeration;

        public EnumerationSpliterator(long est,
                                      int additionalCharacteristics,
                                      Enumeration<T> enumeration) {
            super(est, additionalCharacteristics);
            this.enumeration = enumeration;
        }

        @Override
        public boolean tryAdvance(Consumer<? super T> action) {
            if (enumeration.hasMoreElements()) {
                action.accept(enumeration.nextElement());
                return true;
            }
            return false;
        }

        @Override
        public void forEachRemaining(Consumer<? super T> action) {
            while (enumeration.hasMoreElements())
                action.accept(enumeration.nextElement());
        }
    }
}
