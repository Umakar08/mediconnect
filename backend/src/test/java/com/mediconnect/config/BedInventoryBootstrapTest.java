package com.mediconnect.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mediconnect.entity.HospitalBed;
import java.util.List;
import org.junit.jupiter.api.Test;

class BedInventoryBootstrapTest {
    @Test
    void standardInventoryHasExpectedRoomAndBedCodes() {
        List<HospitalBed> beds = BedInventoryBootstrap.standardInventory();

        assertEquals(170, beds.size());
        assertEquals(30, beds.stream().filter(bed -> bed.getBedType().equals("Single")).count());
        assertEquals(60, beds.stream().filter(bed -> bed.getBedType().equals("Sharing")).count());
        assertEquals(50, beds.stream().filter(bed -> bed.getBedType().equals("General")).count());
        assertEquals(30, beds.stream().filter(bed -> bed.getBedType().equals("Deluxe")).count());
        assertTrue(beds.stream().anyMatch(bed -> bed.getBedCode().equals("S1")));
        assertTrue(beds.stream().anyMatch(bed -> bed.getBedCode().equals("S30")));
        assertTrue(beds.stream().anyMatch(bed -> bed.getBedCode().equals("S1A")));
        assertTrue(beds.stream().anyMatch(bed -> bed.getBedCode().equals("S1B")));
        assertTrue(beds.stream().anyMatch(bed -> bed.getBedCode().equals("S30A")));
        assertTrue(beds.stream().anyMatch(bed -> bed.getBedCode().equals("S30B")));
        assertTrue(beds.stream().anyMatch(bed -> bed.getBedCode().equals("G50")));
        assertTrue(beds.stream().anyMatch(bed -> bed.getBedCode().equals("D30")));
        assertEquals(170, beds.stream().map(HospitalBed::getBedCode).distinct().count());
    }
}
