package com.bayzdelivery.integration;

import com.bayzdelivery.model.Delivery;
import com.bayzdelivery.model.Person;
import com.bayzdelivery.model.PersonRole;
import com.bayzdelivery.service.DeliveryService;
import com.bayzdelivery.service.PersonService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
public class DeliveryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DeliveryService deliveryService;

    @MockBean 
    private PersonService personService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testCreateOrder() throws Exception {
        Person customer = new Person();
        customer.setId(1L);
        customer.setName("Test Customer");
        customer.setRole(PersonRole.CUSTOMER);

        Person deliveryMan = new Person();
        deliveryMan.setId(2L);
        deliveryMan.setName("Test Driver");
        deliveryMan.setRole(PersonRole.DELIVERY_MAN);

        Delivery order = new Delivery();
        order.setOrderId("ORD-123");
        order.setPrice(100L);
        order.setCustomer(customer);
        order.setDeliveryMan(deliveryMan);
        order.setStartTime(Instant.now().plus(1, ChronoUnit.HOURS));

        Delivery savedOrder = new Delivery();
        savedOrder.setId(1L);
        savedOrder.setOrderId("ORD-123");
        savedOrder.setPrice(100L);

        when(deliveryService.createOrder(any(Delivery.class))).thenReturn(savedOrder);

        mockMvc.perform(post("/delivery/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value("ORD-123"))
                .andExpect(jsonPath("$.price").value(100));
    }

    @Test
    public void testCompleteDelivery() throws Exception {
        Delivery completion = new Delivery();
        completion.setOrderId("ORD-123");
        completion.setDistance(10L);
        completion.setPrice(100L);

        Delivery completedDelivery = new Delivery();
        completedDelivery.setId(1L);
        completedDelivery.setOrderId("ORD-123");
        completedDelivery.setDistance(10L);
        completedDelivery.setPrice(100L);
        completedDelivery.setCommission(10L); // 100 * 0.05 + 10 * 0.5 = 10

        when(deliveryService.completeDelivery(any(Delivery.class))).thenReturn(completedDelivery);

        mockMvc.perform(post("/delivery/complete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(completion)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value("ORD-123"))
                .andExpect(jsonPath("$.commission").value(10));
    }

    @Test
    public void testGetDeliveryById() throws Exception {
        Delivery delivery = new Delivery();
        delivery.setId(1L);
        delivery.setOrderId("ORD-123");
        delivery.setPrice(100L);

        when(deliveryService.findById(1L)).thenReturn(delivery);

        mockMvc.perform(get("/delivery/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value("ORD-123"));
    }

    @Test
    public void testGetDeliveryByIdNotFound() throws Exception {
        when(deliveryService.findById(999L)).thenReturn(null);

        mockMvc.perform(get("/delivery/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetTopDeliveryMen() throws Exception {
        when(deliveryService.getTopDeliveryMen(any(Instant.class), any(Instant.class)))
                .thenReturn(Arrays.asList());

        mockMvc.perform(get("/delivery/top")
                .param("startTime", "2025-06-01T00:00:00Z")
                .param("endTime", "2025-07-01T23:59:59Z"))
                .andExpect(status().isOk());
    }
} 