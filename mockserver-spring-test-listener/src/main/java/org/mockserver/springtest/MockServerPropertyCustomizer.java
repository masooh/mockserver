package org.mockserver.springtest;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.mock.env.MockPropertySource;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.util.SocketUtils;

public class MockServerPropertyCustomizer implements ContextCustomizer {
    private static final int mockServerPort = SocketUtils.findAvailableTcpPort();

    @Override
    public void customizeContext(ConfigurableApplicationContext context, MergedContextConfiguration mergedConfig) {
        context.getEnvironment().getPropertySources().addLast(
            new MockPropertySource().withProperty("mockServerPort", mockServerPort)
        );
    }
}
