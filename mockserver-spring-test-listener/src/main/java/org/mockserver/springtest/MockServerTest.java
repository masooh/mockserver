package org.mockserver.springtest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;
import org.springframework.test.context.TestPropertySource;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@TestPropertySource
@Inherited
public @interface MockServerTest {

    @AliasFor(annotation = TestPropertySource.class, attribute = "properties")
    String[] value() default { "" };
}
