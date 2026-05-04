package annotation;

import config.MockType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface WithMock {
    MockType auth() default MockType.NONE;
    MockType action() default MockType.NONE;
}
