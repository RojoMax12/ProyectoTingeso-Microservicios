package com.example.proyectotingeso.Services;

import com.example.proyectotingeso.Entity.*;
import com.example.proyectotingeso.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class LoanToolsServices {

    @Autowired
    LoanToolsRepository loanToolsRepository;

    @Autowired
    ToolRepository toolRepository;

    @Autowired
    ClientRepository clientRepository;

    @Autowired
    AmountsandratesRepository amountsandratesRepository;

    @Autowired
    StateToolsRepository stateToolsRepository;

    @Autowired
    StateUsersRepository stateUsersRepository;


    public LoanToolsEntity getLoanToolsEntityById(Long id) {
        return loanToolsRepository.findById(id).get();
    }

    public LoanToolsEntity CreateLoanToolsEntity(LoanToolsEntity loanToolsEntity) {
        System.out.println("Iniciando CreateLoanToolsEntity");
        System.out.println("Datos recibidos: " + loanToolsEntity);

        // 1. Validaciones iniciales de nulos
        if (loanToolsEntity == null) {
            System.out.println("Error: Entidad de préstamo nula");
            throw new IllegalArgumentException("La entidad de préstamo no puede ser nula");
        }

        // 2. Validar IDs
        System.out.println("ToolID recibido: " + loanToolsEntity.getToolid());
        if (loanToolsEntity.getToolid() == null) {
            System.out.println("Error: ID de herramienta nulo");
            throw new IllegalArgumentException("El ID de la herramienta no puede ser nulo");
        }

        System.out.println("ClientID recibido: " + loanToolsEntity.getClientid());
        if (loanToolsEntity.getClientid() == null) {
            System.out.println("Error: ID de cliente nulo");
            throw new IllegalArgumentException("El ID del cliente no puede ser nulo");
        }

        // 3. Validar cliente
        if (hasOverdueLoans(loanToolsEntity.getClientid())) {
            throw new IllegalStateException("Cliente bloqueado por préstamos vencidos pendientes");
        }

        if (countActiveLoans(loanToolsEntity.getClientid()) > 5) {
            throw new IllegalStateException(String.format(
                    "El cliente (ID: %d) ya tiene el máximo de 5 préstamos vigentes. " +
                            "Debe devolver alguna herramienta antes de solicitar un nuevo préstamo",
                    loanToolsEntity.getClientid()
            ));
        }

        ClientEntity client = clientRepository.findById(loanToolsEntity.getClientid())
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));

        StateUsersEntity restrictedState = stateUsersRepository.findByName("Restricted");
        if (client.getState().equals(restrictedState.getId())) {
            throw new IllegalStateException("Cliente en estado restringido");
        }


        ToolEntity currentTool = toolRepository.findById(loanToolsEntity.getToolid())
                .orElseThrow(() -> new IllegalArgumentException("Herramienta no encontrada"));

        // Obtener todos los préstamos del cliente
        List<LoanToolsEntity> clientLoans = loanToolsRepository.findAllByClientidAndStatus(loanToolsEntity.getClientid(), "Active");
        System.out.println("Prestamos activos" + clientLoans);

        boolean hasToolWithSameNameAndCategory = clientLoans.stream()
                .map(loan -> toolRepository.findById(loan.getToolid()).orElse(null))
                .filter(tool -> tool != null)
                .anyMatch(tool ->
                        tool.getName().equals(currentTool.getName()) &&
                                tool.getCategory().equals(currentTool.getCategory())
                );

        if (hasToolWithSameNameAndCategory) {
            throw new IllegalStateException(String.format(
                    "El cliente ya tiene una herramienta prestada con el nombre '%s' en la categoría '%s'",
                    currentTool.getName(), currentTool.getCategory()
            ));
        }


        // 4. Validar fechas
        System.out.println("Fecha inicial recibida: " + loanToolsEntity.getInitiallenddate());
        if (loanToolsEntity.getInitiallenddate() == null) {
            System.out.println("Error: Fecha inicial nula");
            throw new IllegalArgumentException("La fecha de inicio no puede ser nula");
        }

        System.out.println("Fecha final recibida: " + loanToolsEntity.getFinalreturndate());
        if (loanToolsEntity.getFinalreturndate() == null) {
            System.out.println("Error: Fecha final nula");
            throw new IllegalArgumentException("La fecha de devolución no puede ser nula");
        }

        // 5. Validar herramienta
        System.out.println("Buscando herramienta con ID: " + loanToolsEntity.getToolid());
        var herramienta = toolRepository.findById(loanToolsEntity.getToolid())
                .orElseThrow(() -> {
                    System.out.println("Error: Herramienta no encontrada");
                    return new IllegalArgumentException("La herramienta con ID " + loanToolsEntity.getToolid() + " no existe");
                });
        System.out.println("Herramienta encontrada: " + herramienta);

        // 6. Validar estados
        System.out.println("Obteniendo estados de herramientas");
        List<StateToolsEntity> estados = stateToolsRepository.findAll();
        System.out.println("Estados encontrados: " + estados.size());
        if (estados.isEmpty()) {
            System.out.println("Error: No hay estados configurados");
            throw new IllegalStateException("No hay estados de herramientas configurados");
        }

        // Validar si la herramienta está disponible
        System.out.println("Estado actual de la herramienta: " + herramienta.getStates());
        System.out.println("Estado disponible esperado: " + estados.get(0).getId());
        if (herramienta.getStates() != estados.get(0).getId()) {
            System.out.println("Error: Herramienta no disponible");
            throw new IllegalArgumentException("La herramienta no está disponible");
        }

        try {
            // 8. Actualizar estado de la herramienta
            System.out.println("Actualizando estado de la herramienta a prestada");
            if (estados.size() <= 1) {
                System.out.println("Error: No hay suficientes estados configurados");
                throw new IllegalStateException("Se requieren al menos dos estados (disponible y prestado)");
            }

            Long estadoPrestado = estados.get(1).getId();
            herramienta.setStates(estadoPrestado);
            toolRepository.save(herramienta);
            System.out.println("Estado de herramienta actualizado correctamente");

            // 9. Guardar el préstamo
            System.out.println("Guardando el préstamo");

            loanToolsEntity.setDamageFee(0.0);
            loanToolsEntity.setRepositionFee(0.0);
            loanToolsEntity.setStatus("Active");

            LoanToolsEntity resultado = loanToolsRepository.save(loanToolsEntity);


            calculateRentalFee(loanToolsEntity.getId());

            System.out.println("Préstamo guardado correctamente: " + resultado);
            return resultado;

        } catch (Exception e) {
            System.out.println("Error durante el guardado: " + e.getMessage());
            e.printStackTrace();

            // Rollback manual del estado de la herramienta
            System.out.println("Restaurando estado original de la herramienta");
            herramienta.setStates(estados.get(0).getId());



            toolRepository.save(herramienta);


            throw new RuntimeException("Error al crear el préstamo: " + e.getMessage());
        }
    }

    public double calculateFine(Long loantoolId) {
        // Validar parámetro de entrada
        if (loantoolId == null) {
            throw new IllegalArgumentException("El ID del préstamo no puede ser nulo.");
        }

        // Buscar LoanTool de forma segura
        LoanToolsEntity loantool = loanToolsRepository.findById(loantoolId)
                .orElseThrow(() -> new IllegalStateException("No se encontró el préstamo con ID: " + loantoolId));

        System.out.println(loantool);
        LocalDate today = LocalDate.now();
        System.out.println("Fecha actual: " + today);
        LocalDate returnDate = loantool.getFinalreturndate();
        System.out.println("Fecha Retorno: " + returnDate);

        // Validar que la fecha de devolución esté configurada
        if (returnDate == null) {
            throw new IllegalStateException("La fecha de devolución no está configurada para este préstamo.");
        }

        // Si aún no está atrasado, no hay multa
        if (!today.isAfter(returnDate)) {
            loantool.setLateFee(0.0);
            loantool.setStatus("Active");
            loanToolsRepository.save(loantool);
            return 0.0;
        }

        // Calcular días de atraso
        long diasAtraso = ChronoUnit.DAYS.between(returnDate, today);

        // Obtener tarifas configuradas
        AmountsandratesEntity rates = amountsandratesRepository.findAll()
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Tarifas no configuradas. Por favor, inicialice la configuración del sistema."
                ));

        // Calcular multa
        double lateFee = diasAtraso * rates.getDailylatefeefine();
        loantool.setLateFee(lateFee);
        loantool.setStatus("Late");

        // Bloquear cliente si corresponde
        if (lateFee > 0) {
            ClientEntity client = clientRepository.findById(loantool.getClientid())
                    .orElseThrow(() -> new IllegalStateException("Cliente no encontrado con ID: " + loantool.getClientid()));

            StateUsersEntity restrictedState = stateUsersRepository.findByName("Restricted");
            if (restrictedState == null) {
                throw new IllegalStateException("Estado 'Restricted' no encontrado.");
            }
            client.setState(restrictedState.getId());
            clientRepository.save(client);
        }

        // Guardar cambios en el préstamo
        loanToolsRepository.save(loantool);

        return lateFee;
    }

    public double calculateRentalFee(Long loantoolid) {
        LoanToolsEntity loantool = loanToolsRepository.findById(loantoolid).get();
        long diasPrestamo = ChronoUnit.DAYS.between(
                loantool.getInitiallenddate(),
                loantool.getFinalreturndate());

        // Usar la misma lógica de búsqueda consistente
        AmountsandratesEntity rates = amountsandratesRepository.findAll()
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Tarifas no configuradas. Por favor, inicialice la configuración del sistema."));

        double rentalFee = diasPrestamo * rates.getDailyrentalrate();
        loantool.setRentalFee(rentalFee);
        loanToolsRepository.save(loantool);

        return rentalFee;
    }

    public boolean hasOverdueLoans(Long clientId) {
        List<LoanToolsEntity> loans = loanToolsRepository.findAllByClientid(clientId);
        LocalDate today = LocalDate.now();

        return loans.stream()
                .anyMatch(loan ->
                        loan.getFinalreturndate().isBefore(today) &&
                                (loan.getLateFee() == null || loan.getLateFee() > 0));

    }


    public List<LoanToolsEntity> getAlluserLoanTools(Long userid) {
        return loanToolsRepository.findAllByClientid(userid) ;
    }

    //Se cambia el estado de la herramienta actual a que esta disponble, falta actualizar en el kardex
    public LoanToolsEntity returnLoanTools(Long userid, Long toolid) {
        // Buscar herramienta
        var tool = toolRepository.findById(toolid)
                .orElseThrow(() -> new IllegalArgumentException("La herramienta con id " + toolid + " no existe"));

        // Buscar préstamo asociado
        var loan = loanToolsRepository.findByClientidAndToolid(userid, toolid)
                .orElseThrow(() -> new IllegalArgumentException("No existe un préstamo para este usuario y herramienta"));

        // Cambiar estado de la herramienta a "Disponible" (por ejemplo el primero en la lista)
        List<StateToolsEntity> states = stateToolsRepository.findAll();
        if (states.isEmpty()) {
            throw new IllegalStateException("No hay estados configurados en la BD");
        }

        tool.setStates(states.get(0).getId()); // disponible
        toolRepository.save(tool);

       checkAndUpdateClientStatus(loan.getClientid());
       loan.setStatus("No active");

        return loanToolsRepository.save(loan);
    }

    public boolean checkAndUpdateClientStatus(Long clientId) {
        System.out.println("[TRACE] Iniciando checkAndUpdateClientStatus para clientId: " + clientId);

        ClientEntity client = clientRepository.findById(clientId)
                .orElseThrow(() -> {
                    System.out.println("[ERROR] Cliente no encontrado: " + clientId);
                    return new IllegalArgumentException("Cliente no encontrado");
                });

        System.out.println("[TRACE] Cliente encontrado: " + client.getId() + ", estado actual: " + client.getState());

        // Obtener estados
        Long restricted = stateUsersRepository.findByName("Restricted").getId();
        Long active = stateUsersRepository.findByName("Active").getId();

        // Verificar si el cliente está actualmente restringido
        if (!client.getState().equals(restricted)) {
            System.out.println("[TRACE] El cliente no está restringido. No se realiza ningún cambio.");
            return false;
        }

        // 1. Verificar préstamos vencidos
        if (hasOverdueLoans(clientId)) {
            System.out.println("[TRACE] El cliente tiene préstamos vencidos. No se desbloquea.");
            return false;
        }

        // 2. Verificar multas impagas
        boolean hasUnpaidFines = loanToolsRepository.findAllByClientid(clientId).stream().anyMatch(loan ->
                (loan.getLateFee() != null && loan.getLateFee() > 0) ||
                        (loan.getDamageFee() != null && loan.getDamageFee() > 0) ||
                        (loan.getRepositionFee() != null && loan.getRepositionFee() > 0)
        );

        if (hasUnpaidFines) {
            System.out.println("[TRACE] El cliente tiene multas impagas. No se desbloquea.");
            return false;
        }
        

        // Si no hay restricciones, actualizar a Activo
        client.setState(active);
        clientRepository.save(client);
        System.out.println("[TRACE] Estado del cliente actualizado a 'Active'.");
        return true; // ✅ cambio realizado
    }

    public LoanToolsEntity UpdateLoanToolsEntity(LoanToolsEntity loanToolsEntity) {
        return loanToolsRepository.save(loanToolsEntity);
    }

    public boolean DeleteLoanToolsEntity(Long id) throws Exception{
        try {
            loanToolsRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    public int countActiveLoans(Long clientId) {
        List<LoanToolsEntity> loans = loanToolsRepository.findAllByClientidAndStatus(clientId, "Active");
        // Solo filtramos los préstamos que no han sido devueltos
        return loans.size();
    }

    public void registerDamageFeeandReposition(Long loanId) {

        LoanToolsEntity loan = loanToolsRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found: " + loanId));

        ToolEntity tool = toolRepository.findById(loan.getToolid())
                .orElseThrow(() -> new RuntimeException("Tool not found: " + loan.getToolid()));

        AmountsandratesEntity rates = amountsandratesRepository.findById(3L)
                .orElseThrow(() -> new RuntimeException("Rates not found"));

        System.out.println(tool.getId());
        System.out.println(tool.getStates());

        // Si la herramienta está en reparación (3)
        if (tool.getStates() == 3) {
            loan.setRepositionFee(rates.getReparationcharge());
            System.out.println("Llegue aqui");
        }

        // Si la herramienta está dañada (4)
        if (tool.getStates() == 4) {
            loan.setDamageFee((double) tool.getReplacement_cost());
            System.out.println("Llegue aqui");
        }

        loanToolsRepository.save(loan); // ← IMPORTANTE
    }

    public Boolean registerAllFeesPayment(Long loanId) {
        LoanToolsEntity loan = loanToolsRepository.findById(loanId).get();

        if (loan != null){

            loan.setLateFee(0.0);
            loan.setDamageFee(0.0);
            loan.setRepositionFee(0.0);
            loan.setRentalFee(0.0);
            loan.setStatus("No active");
            loanToolsRepository.save(loan);

            // Verificar si el cliente puede ser desbloqueado
            checkAndUpdateClientStatus(loan.getClientid());
            return true;

        }
        return false;
    }


    public List<LoanToolsEntity> findallloanstoolstatusandRentalFee(){
        List<String> status = List.of("Late", "Active");
        List<LoanToolsEntity> loanstoolstatus = loanToolsRepository.findAllBystatusInAndRentalFeeGreaterThan(status, 0.0);
        return loanstoolstatus;
    }

    public List<LoanToolsEntity> findallloanstoolstatusLate(){
        List<LoanToolsEntity> loans = loanToolsRepository.findAllBystatus("Late");
        return loans;
    }
    
}
