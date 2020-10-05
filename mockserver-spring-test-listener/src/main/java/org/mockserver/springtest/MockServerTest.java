package org.mockserver.springtest;

import org.springframework.core.annotation.AliasFor;
import org.springframework.test.context.TestPropertySource;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@TestPropertySource
@Inherited
public @interface MockServerTest {

    @AliasFor(annotation = TestPropertySource.class, attribute = "properties")
    String[] value() default {""};
}
