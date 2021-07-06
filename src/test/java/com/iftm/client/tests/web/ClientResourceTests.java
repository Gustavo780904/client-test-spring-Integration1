package com.iftm.client.tests.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.iftm.client.dto.ClientDTO;
import com.iftm.client.services.ClientService;
import com.iftm.client.services.exceptions.DatabaseException;
import com.iftm.client.services.exceptions.ResourceNotFoundException;
import com.iftm.client.tests.factory.ClientFactory;


@SpringBootTest
@AutoConfigureMockMvc
public class ClientResourceTests {

	@Autowired
	private MockMvc mockMvc;
	
	@MockBean
	private ClientService service;
	
	private Long existingId;
	private Long nonExistingId;
	private Long dependentId;
	private ClientDTO clientDTO;
	private List<ClientDTO> list;
	private PageImpl<ClientDTO> page;
	
	@BeforeEach
	void setUp() throws Exception {
		
		existingId = 1L;
		nonExistingId = 1000L;
		dependentId = 4L;
		clientDTO = ClientFactory.createClientDTO();
		list = new ArrayList<ClientDTO>();
		page = new PageImpl<>(List.of(clientDTO));
		
		when(service.findById(existingId)).thenReturn(clientDTO);
		when(service.findById(nonExistingId)).thenThrow(ResourceNotFoundException.class);
		
		when(service.findAll()).thenReturn(list);
		when(service.findAllPaged(any())).thenReturn(page);	
		
		when(service.insert(any())).thenReturn(clientDTO);
		
		when(service.update(eq(existingId), any())).thenReturn(clientDTO);
		when(service.update(eq(nonExistingId), any())).thenThrow(ResourceNotFoundException.class);
		
		doNothing().when(service).delete(existingId);
		doThrow(ResourceNotFoundException.class).when(service).delete(nonExistingId);
		doThrow(DatabaseException.class).when(service).delete(dependentId);
	}
	
	@Test
	public void findAllShouldReturnPage() throws Exception {
		
		ResultActions result =
		mockMvc.perform(get("/clients/")
			.accept(MediaType.APPLICATION_JSON));
		
		result.andExpect(status().isOk());
		result.andExpect(jsonPath("$.content").exists());
	}
	
	@Test
	public void findAllShouldReturnList() throws Exception {
		
		ResultActions result =
		mockMvc.perform(get("/clients/findAll")
			.accept(MediaType.APPLICATION_JSON));
		
		result.andExpect(status().isOk());
	}
	
	@Test
	public void findByIdShouldReturnClientWhenIdExists() throws Exception {
		/*
		mockMvc.perform(get("/clients/{id}", existingId)
			.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk());
		*/
		
		ResultActions result = 
		mockMvc.perform(get("/clients/{id}", existingId)
			.accept(MediaType.APPLICATION_JSON));
		
		result.andExpect(status().isOk());
		result.andExpect(jsonPath("$.id").exists());
		result.andExpect(jsonPath("$.id").value(existingId));
	}
	
	@Test
	public void findByIdShouldReturnNotFoundWhenIdDoesNotExists() throws Exception {
		
		ResultActions result =
		mockMvc.perform(get("/clients/{id}", nonExistingId)
			.accept(MediaType.APPLICATION_JSON));
		
		result.andExpect(status().isNotFound());
	}

}
