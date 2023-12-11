package com.example.batchprocessing.Config;

import com.example.batchprocessing.Entity.Employee;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class Processor implements ItemProcessor<Employee, Employee> {

    @Override
    public Employee process(Employee item) throws Exception {
        return item;
    }
}
