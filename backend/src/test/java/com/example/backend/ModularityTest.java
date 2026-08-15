package com.example.backend;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

public class ModularityTest {
    @Test
    void verifiesModuleStructure() {
        ApplicationModules.of(BackendApplication.class).verify();
    }
}
