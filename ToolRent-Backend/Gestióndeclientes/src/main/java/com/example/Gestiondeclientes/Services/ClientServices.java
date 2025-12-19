package com.example.Gestiondeclientes.Services;

import com.example.Gestiondeclientes.Entity.ClientEntity;
import com.example.Gestiondeclientes.Models.LoanTools;
import com.example.Gestiondeclientes.Repository.ClientRepository;
import com.example.Gestiondeclientes.Repository.StateUsersRepository;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
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
        try {
            // 1. Cambia List.class por LoanTools[].class
            // Esto obliga a Jackson a mapear los campos correctamente
            LoanTools[] loanToolsArray = restTemplate.getForObject(
                    "http://GESTIONDEPRESTYDEVMICROSERVICES/api/LoanTools/late",
                    LoanTools[].class);

            if (loanToolsArray == null || loanToolsArray.length == 0) {
                return Collections.emptyList();
            }

            // 2. Convertir el Array a una lista de IDs de cliente
            // Importante: Asegúrate de importar java.util.Arrays
            List<Long> clientIds = Arrays.stream(loanToolsArray)
                    .map(LoanTools::getClientid) // Ahora sí reconocerá el método
                    .distinct()
                    .collect(Collectors.toList());

            // 3. Buscar los clientes en tu base de datos local
            return clientRepository.findAllById(clientIds);

        } catch (Exception e) {
            // Log para ver el error real en la consola de IntelliJ
            System.err.println("Error en la comunicación con Préstamos: " + e.getMessage());
            return Collections.emptyList();
        }
    }



}

