package com.mediconnect.config;

import com.mediconnect.entity.HospitalBed;
import com.mediconnect.repository.HospitalBedRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class BedInventoryBootstrap implements ApplicationRunner {
    private final HospitalBedRepository hospitalBedRepository;

    public BedInventoryBootstrap(HospitalBedRepository hospitalBedRepository) {
        this.hospitalBedRepository = hospitalBedRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List<HospitalBed> missingBeds = standardInventory().stream()
                .filter(bed -> !hospitalBedRepository.existsByBedCodeIgnoreCase(bed.getBedCode()))
                .toList();
        hospitalBedRepository.saveAll(missingBeds);
    }

    static List<HospitalBed> standardInventory() {
        List<HospitalBed> beds = new ArrayList<>(170);
        addRooms(beds, "Single", "S", 30, false);
        addRooms(beds, "Sharing", "S", 30, true);
        addRooms(beds, "General", "G", 50, false);
        addRooms(beds, "Deluxe", "D", 30, false);
        return beds;
    }

    private static void addRooms(List<HospitalBed> beds, String roomType, String prefix, int count,
            boolean sharing) {
        for (int number = 1; number <= count; number++) {
            if (sharing) {
                beds.add(new HospitalBed(prefix + number + "A", roomType, roomType));
                beds.add(new HospitalBed(prefix + number + "B", roomType, roomType));
            } else {
                beds.add(new HospitalBed(prefix + number, roomType, roomType));
            }
        }
    }
}
