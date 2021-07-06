package com.iftm.client.tests.integration;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import com.iftm.client.dto.ClientDTO;
import com.iftm.client.services.ClientService;
import com.iftm.client.services.exceptions.ResourceNotFoundException;
import com.iftm.client.tests.factory.ClientFactory;

@SpringBootTest
@Transactional
public class ClientServiceIT {

	@Autowired
	private ClientService service;

	private long existingId;
	private long nonExistingId;
	private long countClientByIncome;
	private long countTotalClients;
	private PageRequest pageRequest;

	@BeforeEach
	void setUp() throws Exception {
		existingId = 1L;
		nonExistingId = 1000L;
		countClientByIncome = 5L;
		countTotalClients = 12L;
		pageRequest = PageRequest.of(0, 6);
	}

	@Test
	public void deleteShouldDoNothingWhenIdExists() {

		Assertions.assertDoesNotThrow(() -> {
			service.delete(existingId);
		});
	}

	@Test
	public void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExistis() {

		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			service.delete(nonExistingId);
		});
	}

	@Test
	public void findByIncomeShouldReturnClientsWhenClientIncomeIsGreaterThanOrEqualsToValue() {

		Double income = 4000.0;

		Page<ClientDTO> result = service.findByIncome(income, pageRequest);

		Assertions.assertFalse(result.isEmpty());
		Assertions.assertEquals(countClientByIncome, result.getTotalElements());
	}

	@Test
	public void findAllShouldReturnAllClients() {

		List<ClientDTO> result = service.findAll();

		Assertions.assertEquals(countTotalClients, result.size());
	}

	@Test
	public void deleteShouldDecrementTotalClients() {
		service.delete(existingId);
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			service.findById(existingId);
		});
		var result = service.findAll();
		Assertions.assertEquals(countTotalClients - 1, result.size());
	}

	@Test
	public void findByIdShouldReturnExistingClient() {
		var dto = service.findById(existingId);
		Assertions.assertEquals("ConceiÃ§Ã£o Evaristo", dto.getName());
		Assertions.assertEquals("10619244881", dto.getCpf());
	}

	@Test
	public void insertShouldIncrementTotalClients() {
		var dto = ClientFactory.createClientDTO();
		dto.setId(countTotalClients + 1);
		dto = service.insert(dto);

		var result = service.findAll();
		Assertions.assertEquals(countTotalClients + 1, result.size());
	}

	@Test
	public void updateShouldChangeExistingClient() {
		var dto = service.findById(existingId);
		dto.setName("Jymmy Hendrix");
		dto.setCpf("63663663699");
		service.update(dto.getId(), dto);

		dto = service.findById(existingId);
		Assertions.assertEquals("Jymmy Hendrix", dto.getName());
		Assertions.assertEquals("63663663699", dto.getCpf());
	}

}
