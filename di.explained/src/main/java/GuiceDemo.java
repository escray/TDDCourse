import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provides;

import javax.inject.Qualifier;
import javax.inject.Inject;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

public class GuiceDemo {
    @Qualifier
    @Retention(RUNTIME)
    @interface Message {}

    @Qualifier
    @Retention(RUNTIME)
    @interface Count {}

    static class DemoModule extends AbstractModule {
        @Provides
        @Count
        static Integer provideCount() {
            return 3;
        }

        @Provides
        @Message
        static String provideMessage() {
            return "hello world";
        }
    }

    static class Greeter {
        private final String message;
        private final int count;

        Greeter() {
            this.message = "111";
            this.count = 5;
        }

        @Inject
        Greeter(@Message String message, @Count int count) {
            this.message = message;
            this.count = count;
        }

        void sayHello() {
            for (int i = 0; i < count; i++) {
                System.out.println(message);
            }
        }
    }

    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new DemoModule());
        Greeter greeter = injector.getInstance(Greeter.class);
        greeter.sayHello();
    }
}

