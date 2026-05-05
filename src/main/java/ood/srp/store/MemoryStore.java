package ood.srp.store;

import ood.srp.model.Employee;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

    public class MemoryStore implements Store {

        private final List<Employee> employees = new ArrayList<>();

        @Override
        public void add(Employee employee) {
            employees.add(employee);
        }

        @Override
        public List<Employee> findBy(Predicate<Employee> filter) {
            return employees.stream()
                    .filter(filter)
                    .collect(Collectors.toList());
        }
    }

