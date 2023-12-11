package com.example.batchprocessing.Entity;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "employee")
@Data
public class Employee {

    private int empId;
    private String empName;
    private double empSalary;
    private String empRole;

}