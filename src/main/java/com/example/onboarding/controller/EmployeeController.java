package com.example.onboarding.controller;

import com.example.onboarding.model.Employee;
import com.example.onboarding.repository.EmployeeRepository;

import jakarta.validation.Valid;

import org.apache.commons.text.WordUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeRepository repository;

    public EmployeeController(EmployeeRepository repository) {
        this.repository = repository;
    }

    /** API 1: capture employee details. The name is stored in "Title Case". */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Employee create(@Valid @RequestBody Employee employee) {
        employee.setId(null); // always create a new record
        employee.setName(WordUtils.capitalizeFully(employee.getName()));
        return repository.save(employee);
    }

    /** API 2: fetch all captured employee details. */
    @GetMapping
    public List<Employee> getAll() {
        return repository.findAll();
    }
}
