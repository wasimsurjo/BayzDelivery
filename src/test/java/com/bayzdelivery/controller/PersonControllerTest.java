package com.bayzdelivery.controller;

import com.bayzdelivery.model.Person;
import com.bayzdelivery.model.PersonRole;
import com.bayzdelivery.service.PersonService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PersonController.class)
public class PersonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PersonService personService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testCreateCustomer() throws Exception {
        Person customer = new Person();
        customer.setName("Abdullah Customer");
        customer.setEmail("abdullah@example.com");
        customer.setRole(PersonRole.CUSTOMER);

        Person savedCustomer = new Person();
        savedCustomer.setId(1L);
        savedCustomer.setName("Abdullah Customer");
        savedCustomer.setEmail("abdullah@example.com");
        savedCustomer.setRole(PersonRole.CUSTOMER);

        when(personService.save(any(Person.class))).thenReturn(savedCustomer);

        mockMvc.perform(post("/person")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Abdullah Customer"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    @Test
    public void testCreateDeliveryMan() throws Exception {
        Person deliveryMan = new Person();
        deliveryMan.setName("Zahed Driver");
        deliveryMan.setEmail("zahed@example.com");
        deliveryMan.setRegistrationNumber("DRV-001");
        deliveryMan.setRole(PersonRole.DELIVERY_MAN);

        Person savedDeliveryMan = new Person();
        savedDeliveryMan.setId(2L);
        savedDeliveryMan.setName("Zahed Driver");
        savedDeliveryMan.setEmail("zahed@example.com");
        savedDeliveryMan.setRegistrationNumber("DRV-001");
        savedDeliveryMan.setRole(PersonRole.DELIVERY_MAN);

        when(personService.save(any(Person.class))).thenReturn(savedDeliveryMan);

        mockMvc.perform(post("/person")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(deliveryMan)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Zahed Driver"))
                .andExpect(jsonPath("$.role").value("DELIVERY_MAN"));
    }

    @Test
    public void testGetAllPersons() throws Exception {
        Person customer = new Person();
        customer.setId(1L);
        customer.setName("Test Customer");
        customer.setRole(PersonRole.CUSTOMER);

        when(personService.getAll()).thenReturn(Arrays.asList(customer));

        mockMvc.perform(get("/person"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Customer"));
    }

    @Test
    public void testGetPersonById() throws Exception {
        Person customer = new Person();
        customer.setId(1L);
        customer.setName("Test Customer");
        customer.setRole(PersonRole.CUSTOMER);

        when(personService.findById(1L)).thenReturn(customer);

        mockMvc.perform(get("/person/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Customer"));
    }

    @Test
    public void testGetPersonByIdNotFound() throws Exception {
        when(personService.findById(999L)).thenReturn(null);

        mockMvc.perform(get("/person/999"))
                .andExpect(status().isNotFound());
    }
}
