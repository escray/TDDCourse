package geektime.tdd.di;

interface ComponentProvider<T> {
    T get(Context context);
}
