package com.workintech.s17d2.rest;

import com.workintech.s17d2.model.*;
import com.workintech.s17d2.tax.Taxable;
import jakarta.annotation.PostConstruct;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/developers") // context-path: /workintech -> full: /workintech/developers
public class DeveloperController {

    public Map<Integer, Developer> developers;
    private final Taxable taxable;

    // DI: Taxable interface üzerinden DeveloperTax inject edilir
    public DeveloperController(Taxable taxable) {
        this.taxable = taxable;
    }

    @PostConstruct
    public void init() {
        developers = new HashMap<>();
    }

    // [GET] /workintech/developers
    @GetMapping
    public List<Developer> getAll() {
        return new ArrayList<>(developers.values());
    }

    // [GET] /workintech/developers/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Developer> getById(@PathVariable Integer id) {
        Developer dev = developers.get(id);
        if (dev == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(dev);
    }

    // [POST] /workintech/developers
    @PostMapping
    public ResponseEntity<Developer> create(@RequestBody DeveloperRequest req) {
        if (req == null || req.id() == null || req.name() == null || req.salary() == null || req.experience() == null) {
            return ResponseEntity.badRequest().build();
        }

        double netSalary = calculateNetSalary(req.salary(), req.experience());

        Developer created = switch (req.experience()) {
            case JUNIOR -> new JuniorDeveloper(req.id(), req.name(), netSalary);
            case MID -> new MidDeveloper(req.id(), req.name(), netSalary);
            case SENIOR -> new SeniorDeveloper(req.id(), req.name(), netSalary);
        };

        developers.put(created.getId(), created);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // [PUT] /workintech/developers/{id}
    @PutMapping("/{id}")
    public ResponseEntity<Developer> update(@PathVariable Integer id, @RequestBody DeveloperRequest req) {
        if (!developers.containsKey(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        if (req == null || req.name() == null || req.salary() == null || req.experience() == null) {
            return ResponseEntity.badRequest().build();
        }

        double netSalary = calculateNetSalary(req.salary(), req.experience());

        Developer updated = switch (req.experience()) {
            case JUNIOR -> new JuniorDeveloper(id, req.name(), netSalary);
            case MID -> new MidDeveloper(id, req.name(), netSalary);
            case SENIOR -> new SeniorDeveloper(id, req.name(), netSalary);
        };

        developers.put(id, updated);
        return ResponseEntity.ok(updated);
    }

    // [DELETE] /workintech/developers/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Integer id) {
        Developer removed = developers.remove(id);
        if (removed == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("message", "Deleted");
        result.put("deletedDeveloper", removed);

        return ResponseEntity.ok(result);
    }

    private double calculateNetSalary(double salary, Experience experience) {
        // Rates 15/25/35 geliyor ama yüzde gibi kullanacağız: /100
        return switch (experience) {
            case JUNIOR -> salary - (salary * taxable.getSimpleTaxRate() / 100.0);
            case MID -> salary - (salary * taxable.getMiddleTaxRate() / 100.0);
            case SENIOR -> salary - (salary * taxable.getUpperTaxRate() / 100.0);
        };
    }
}