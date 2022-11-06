package com.hp.onecloud.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MysqlLoadConditionTest {

    private MysqlLoadCondition mysqlLoadConditionUnderTest;

    @BeforeEach
    void setUp() {
        mysqlLoadConditionUnderTest = new MysqlLoadCondition();
    }

    @Test
    void testMatches() {
        // Setup
        ConditionContext context = mock(ConditionContext.class);

        MockEnvironment environment = new MockEnvironment();
        environment.setProperty("none_mysql", "true");
        when(context.getEnvironment()).thenReturn(environment);

        // Run the test
        final boolean result = mysqlLoadConditionUnderTest.matches(context, null);

        // Verify the results
        assertThat(result).isFalse();

    }
}
