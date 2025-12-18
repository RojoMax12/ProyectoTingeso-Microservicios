package com.example.Gestiondeclientes.Services;

import com.example.Gestiondeclientes.Models.LoanTools;
import com.example.Gestiondeclientes.Repository.ClientRepository;
import com.example.Gestiondeclientes.Repository.StateUsersRepository;
import com.example.proyectotingeso.Entity.ClientEntity;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ClientServices {

    @Autowired
    ClientRepository clientRepository;

    @Autowired
    StateUsersRepository stateUsersRepository;

    @Autowired
    RestTemplate restTemplate;


    public ClientEntity createClient(ClientEntity clientEntity) {
        if (clientEntity.getState() == null) {
            clientEntity.setState(stateUsersRepository.findByName("Active").getId());
        }

        System.out.println("El cliente recibido: " + clientEntity);

        // FORMA SEGURA de verificar duplicados
        Optional<ClientEntity> existingClientByRut = clientRepository.findFirstByRut(clientEntity.getRut());
        Optional<ClientEntity> existingClientByEmail = clientRepository.findFirstByEmail(clientEntity.getEmail());

        // Validar RUT duplicado
        if (existingClientByRut.isPresent()) {
            throw new IllegalArgumentException("Ya existe un cliente con ese RUT: " + clientEntity.getRut());
        }

        // Validar Email duplicado
        if (existingClientByEmail.isPresent()) {
            throw new IllegalArgumentException("Ya existe un cliente con ese email: " + clientEntity.getEmail());
        }

        return clientRepository.save(clientEntity);
    }

    public List<ClientEntity> getAllClients() {
        return clientRepository.findAll();
    }

    public ClientEntity getClientByRut(String rut) {
        return clientRepository.findByRut(rut);
    }

    public ClientEntity updateClient(ClientEntity clientEntity) {
        return clientRepository.save(clientEntity);
    }

    public boolean deleteClient(Long id) {
        clientRepository.deleteById(id);
        return true;

    }

    public ClientEntity getClientById(Long id) {
        return clientRepository.findById(id).orElse(null);
    }

    public List<ClientEntity> getAllClientLoanLate() {
        List<LoanTools> loanToolsEntities = restTemplate.getForObject("http://GestióndePréstyDevMicroservices/api/LoanTools/late", List.class);

        if (loanToolsEntities.isEmpty()) {
            return Collections.emptyList(); // Retorna inmediatamente si no hay préstamos atrasados
        }

        // Sacar los IDs de cliente
        List<Long> clientIds = loanToolsEntities.stream()
                .map(LoanTools::getClientid)
                .collect(Collectors.toList());

        // Solo se llama al repositorio si hay IDs para buscar
        return clientRepository.findAllById(clientIds);
    }



}

