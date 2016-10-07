package com.barryholroyd.productsdemo;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Run all instrumented unit tests.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses(
        {
                JsonTests.class,
                ToasterTests.class
        }
)
public class InstrumentedUnitTestSuite {
}
