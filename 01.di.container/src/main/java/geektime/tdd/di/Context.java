package geektime.tdd.di;

import java.lang.reflect.Type;
import java.util.Optional;

public interface Context {
    // Optional get(Ref ref);
    Optional get(Type type);
}
