package com.bayzdelivery.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.bayzdelivery.model.Person;
import com.bayzdelivery.service.PersonService;

@RestController
public class PersonController {

  private static final Logger log = LoggerFactory.getLogger(PersonController.class);

  @Autowired
  PersonService personService;

  @PostMapping("/person")
  public ResponseEntity<Person> registerPerson(@RequestBody Person person) {
    try {
      Person savedPerson = personService.save(person);
      log.info("Successfully created person with ID: {}", savedPerson.getId());
      return ResponseEntity.ok(savedPerson);
    } catch (Exception e) {
      log.error("Failed to create person record: {}", e.getMessage(), e);
      throw e;
    }
  }

  @GetMapping("/person")
  public ResponseEntity<List<Person>> getAllPersons() {
    try{
    List<Person> persons = personService.getAll();
    log.debug("Returning {} person(s)", persons.size());
    return ResponseEntity.ok(persons);
  } catch (Exception e) {
    log.error("Failed to get all person(s): {}", e.getMessage(), e);
    throw e;
  }
  }

  @GetMapping("/person/{personId}")
  public ResponseEntity<Person> getPersonById(@PathVariable Long personId) {
    try{
    Person person = personService.findById(personId);
    if (person != null) {
      log.debug("Found person with ID: {}", personId);
      return ResponseEntity.ok(person);
    }
    log.debug("No person found with ID: {}", personId);
    return ResponseEntity.notFound().build();
  } catch (Exception e) {
    log.error("Failed to get person by ID: {}", personId, e);
    throw e;
    }
  }
  
}
