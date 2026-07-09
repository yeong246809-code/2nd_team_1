package org.example.k_market.service.admin;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class VersionCodeComparatorTest {

    @Test
    void comparesNumericVersionSegments() {
        assertTrue(VersionCodeComparator.INSTANCE.compare("0.0.10-SNAPSHOT", "0.0.9-SNAPSHOT") > 0);
        assertTrue(VersionCodeComparator.INSTANCE.compare("1.10.0", "1.9.9") > 0);
    }

    @Test
    void considersReleaseNewerThanSnapshotWithSameNumbers() {
        assertTrue(VersionCodeComparator.INSTANCE.compare("1.0.0", "1.0.0-SNAPSHOT") > 0);
    }
}
