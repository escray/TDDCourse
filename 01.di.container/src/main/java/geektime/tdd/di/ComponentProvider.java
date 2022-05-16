package geektime.tdd.di;

import java.util.List;

import static java.util.List.of;

interface ComponentProvider<T> {
    T get(Context context);

    default List<Class<?>> getDependencies() {
        return of();
    };
}