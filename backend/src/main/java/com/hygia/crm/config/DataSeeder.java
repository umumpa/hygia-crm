package com.hygia.crm.config;

import com.hygia.crm.entity.Region;
import com.hygia.crm.repository.RegionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("dev")
public class DataSeeder {

    @Bean
    public CommandLineRunner seedData(RegionRepository regionRepository) {
        return args -> {
            if (regionRepository.count() == 0) {
                regionRepository.save(new Region(null, "Seattle", "WA"));
                regionRepository.save(new Region(null, "San Francisco", "CA"));
                regionRepository.save(new Region(null, "Los Angeles", "CA"));
            }
        };
    }
}

