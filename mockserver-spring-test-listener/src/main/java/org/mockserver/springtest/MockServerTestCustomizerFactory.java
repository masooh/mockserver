package org.mockserver.springtest;

import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.test.context.ContextConfigurationAttributes;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.ContextCustomizerFactory;

import javax.annotation.Nullable;
import java.util.List;

public class MockServerTestCustomizerFactory implements ContextCustomizerFactory {
    @Override
    @Nullable
    public ContextCustomizer createContextCustomizer(Class<?> testClass, List<ContextConfigurationAttributes> configAttributes) {
        final MockServerTest mockServerTestAnnotation = AnnotatedElementUtils.findMergedAnnotation(testClass, MockServerTest.class);
        return mockServerTestAnnotation != null ? new MockServerPropertyCustomizer() : null;
    }
}
