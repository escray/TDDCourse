package geektime.tdd.di;

import java.lang.reflect.Type;
import java.util.List;

import static java.util.List.of;

interface ComponentProvider<T> {
    T get(Context context);

    default List<Context.Ref> getDependencyRefs() {
        return getDependencies().stream().map(Context.Ref::of).toList();
    }
    // Reference -> Provider<Service> | Service
    // List<Ref> getDependencies()
    default List<Type> getDependencies() { return of(); };
}