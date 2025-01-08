package com.example.chargepointlocator;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        DatabaseHelperTest.class,
        LoginActivityTest.class,
        RegisterActivityTest.class
})
public class AllTests {
    // This class remains empty, it is used only as a holder for the above annotations
}
