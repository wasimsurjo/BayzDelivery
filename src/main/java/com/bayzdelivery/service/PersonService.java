package com.bayzdelivery.service;

import java.util.List;
import com.bayzdelivery.model.Person;

public interface PersonService {
  public List<Person> getAll();

  public Person save(Person p);

  public Person findById(Long personId);

}
