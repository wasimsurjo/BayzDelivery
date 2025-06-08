package com.bayzdelivery.service;

import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bayzdelivery.model.Person;
import com.bayzdelivery.repositories.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PersonServiceImpl implements PersonService {
    private static final Logger log = LoggerFactory.getLogger(PersonServiceImpl.class);

    @Autowired
    PersonRepository personRepository;

    @Override
    public List<Person> getAll() {
        try{
        log.debug("Fetching all person(s)");
        List<Person> personList = ((List<Person>)personRepository.findAll());
        log.debug("Found {} person(s)", personList.size());
        return personList;
        } catch (Exception e) {
            log.error("Failed to fetch all person(s): {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public Person save(Person person) {
        log.info("Saving new person with email: {}", person.getEmail());
        try {
            Person savedPerson = personRepository.save(person);
            log.info("Successfully saved person with ID: {}", savedPerson.getId());
            return savedPerson;
        } catch (Exception e) {
            log.error("Failed to save person info: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public Person findById(Long personId) {
        try{
        log.debug("Looking up person with ID: {}", personId);
        Optional<Person> person = personRepository.findById(personId);
        if (person.isPresent()) {
            log.debug("Found person with ID: {}", personId);
            return person.get();
        }
        log.debug("No person found with ID: {}", personId);
        return null;}
        catch (Exception e) {
            log.error("Failed to find person with ID: {}", personId, e);
            throw e;
        }
    }
}
