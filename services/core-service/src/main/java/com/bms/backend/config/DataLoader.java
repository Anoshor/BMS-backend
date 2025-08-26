package com.bms.backend.config;

import com.bms.backend.entity.ServiceCategory;
import com.bms.backend.repository.ServiceCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private ServiceCategoryRepository serviceCategoryRepository;

    @Override
    public void run(String... args) throws Exception {
        // Only load data if there are no service categories
        if (serviceCategoryRepository.count() == 0) {
            loadServiceCategories();
        }
    }

    private void loadServiceCategories() {
        ServiceCategory plumbing = new ServiceCategory();
        plumbing.setName("Plumbing");
        plumbing.setDescription("Water, pipes, and plumbing related maintenance");
        serviceCategoryRepository.save(plumbing);

        ServiceCategory electrical = new ServiceCategory();
        electrical.setName("Electrical");
        electrical.setDescription("Electrical systems, wiring, and lighting");
        serviceCategoryRepository.save(electrical);

        ServiceCategory hvac = new ServiceCategory();
        hvac.setName("HVAC");
        hvac.setDescription("Heating, ventilation, and air conditioning");
        serviceCategoryRepository.save(hvac);

        ServiceCategory appliances = new ServiceCategory();
        appliances.setName("Appliances");
        appliances.setDescription("Kitchen and laundry appliances");
        serviceCategoryRepository.save(appliances);

        ServiceCategory general = new ServiceCategory();
        general.setName("General Maintenance");
        general.setDescription("General building and apartment maintenance");
        serviceCategoryRepository.save(general);
    }
}