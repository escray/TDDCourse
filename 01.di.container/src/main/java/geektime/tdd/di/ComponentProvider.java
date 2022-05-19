package geektime.tdd.di;

import java.lang.reflect.Type;
import java.util.List;

import static java.util.List.of;

interface ComponentProvider<T> {
    T get(Context context);

    // Reference -> Provider<Service> | Service
    // List<Ref> getDependencies()
    default List<Type> getDependencies() { return of(); };
}