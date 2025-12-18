package com.example.proyectotingeso.Services;

import com.example.proyectotingeso.Entity.ClientEntity;
import com.example.proyectotingeso.Entity.DataReportEntity;
import com.example.proyectotingeso.Entity.LoanToolsEntity;
import com.example.proyectotingeso.Entity.ReportEntity;
import com.example.proyectotingeso.Repository.DataReportRepository;
import com.example.proyectotingeso.Repository.ReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.ArgumentMatchers;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


public class ReportServicesTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private DataReportRepository dataReportRepository;
    @Mock
    private LoanToolsServices loanToolsServices;
    @Mock
    private KardexServices kardexServices;
    @Mock
    private ClientServices clientServices;

    @InjectMocks
    private ReportServices reportServices;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // --- Pruebas de CRUD Simple ---

    @Test
    public void testCreateReport() {
        // Given
        LocalDate today = LocalDate.now();
        ReportEntity reportEntity = new ReportEntity(null, "TestReport", today);

        when(reportRepository.save(ArgumentMatchers.any(ReportEntity.class)))
                .thenAnswer(i -> {
                    ReportEntity saved = (ReportEntity) i.getArguments()[0];
                    saved.setId(1L);
                    return saved;
                });

        // When
        ReportEntity result = reportServices.createReport(reportEntity);

        // Then
        assertEquals(1L, result.getId());
        assertEquals("TestReport", result.getName());
        assertEquals(today, result.getDate());
        verify(reportRepository, times(1)).save(ArgumentMatchers.any(ReportEntity.class));
    }

    // --- Pruebas de Queries Simples ---

    @Test
    public void testReportTopToolsAll() {
        // Given
        LocalDate today = LocalDate.now();
        ReportEntity report1 = new ReportEntity(1L, "ReportTopTools", today);
        ReportEntity report2 = new ReportEntity(2L, "ReportTopTools", today);
        when(reportRepository.findByName("ReportTopTools")).thenReturn(Arrays.asList(report1, report2));

        // When
        List<ReportEntity> result = reportServices.ReportTopToolsAll();

        // Then
        assertEquals(2, result.size());
        assertEquals("ReportTopTools", result.get(0).getName());
        verify(reportRepository, times(1)).findByName("ReportTopTools");
    }

    @Test
    public void testGetAllReportClientLoanLate() {
        // Given
        LocalDate today = LocalDate.now();
        ReportEntity report1 = new ReportEntity(1L, "ReportClientLoanLate", today);
        ReportEntity report2 = new ReportEntity(2L, "ReportClientLoanLate", today);
        when(reportRepository.findByName("ReportClientLoanLate")).thenReturn(Arrays.asList(report1, report2));

        // When
        List<ReportEntity> result = reportServices.GetAllReportClientLoanLate();

        // Then
        assertEquals(2, result.size());
        assertEquals("ReportClientLoanLate", result.get(0).getName());
        verify(reportRepository, times(1)).findByName("ReportClientLoanLate");
    }

    @Test
    public void testGetAllReportLoanTools() {
        // Given
        LocalDate today = LocalDate.now();
        ReportEntity report1 = new ReportEntity(1L, "ReportLoanTools", today);
        ReportEntity report2 = new ReportEntity(2L, "ReportLoanTools", today);
        when(reportRepository.findByName("ReportLoanTools")).thenReturn(Arrays.asList(report1, report2));

        // When
        List<ReportEntity> result = reportServices.GetAllReportLoanTools();

        // Then
        assertEquals(2, result.size());
        assertEquals("ReportLoanTools", result.get(0).getName());
        verify(reportRepository, times(1)).findByName("ReportLoanTools");
    }

    @Test
    public void testReportfilterDate() {
        // Given
        LocalDate start = LocalDate.of(2025, 9, 1);
        LocalDate end = LocalDate.of(2025, 9, 30);
        ReportEntity report1 = new ReportEntity(1L, "ReportLoanTools", start);
        ReportEntity report2 = new ReportEntity(2L, "ReportLoanTools", end);
        when(reportRepository.findByDateBetweenOrderByDateDesc(start, end)).thenReturn(Arrays.asList(report1, report2));

        // When
        List<ReportEntity> result = reportServices.ReportfilterDate(start, end);

        // Then
        assertEquals(2, result.size());
        assertEquals("ReportLoanTools", result.get(0).getName());
        verify(reportRepository, times(1)).findByDateBetweenOrderByDateDesc(start, end);
    }

    // --- Pruebas de Creación de Reportes Complejos ---

    @Test
    public void testReportLoanTools() {
        // Given
        LoanToolsEntity loan1 = new LoanToolsEntity();
        loan1.setId(10L);
        LoanToolsEntity loan2 = new LoanToolsEntity();
        loan2.setId(11L);
        List<LoanToolsEntity> mockLoans = Arrays.asList(loan1, loan2);
        when(loanToolsServices.findallloanstoolstatusandRentalFee()).thenReturn(mockLoans);

        when(reportRepository.save(ArgumentMatchers.any(ReportEntity.class)))
                .thenAnswer(i -> {
                    ReportEntity saved = (ReportEntity) i.getArguments()[0];
                    saved.setId(5L);
                    return saved;
                });

        when(dataReportRepository.saveAll(ArgumentMatchers.anyList())).thenReturn(new ArrayList<>());

        // When
        List<ReportEntity> result = reportServices.ReportLoanTools();

        // Then
        assertEquals(1, result.size());
        assertEquals("ReportLoanTools", result.get(0).getName());

        verify(loanToolsServices, times(1)).findallloanstoolstatusandRentalFee();
        verify(reportRepository, times(1)).save(ArgumentMatchers.any(ReportEntity.class));

        // CORREGIDO: Casting explícito a List para usar size()
        verify(dataReportRepository, times(1)).saveAll(ArgumentMatchers.argThat(list -> ((List<?>) list).size() == 2));
    }

    @Test
    public void testReportClientLoanLate() {
        // Given
        ClientEntity client1 = new ClientEntity();
        client1.setId(20L);
        ClientEntity client2 = new ClientEntity();
        client2.setId(21L);
        List<ClientEntity> mockClients = Arrays.asList(client1, client2);
        when(clientServices.getAllClientLoanLate()).thenReturn(mockClients);

        when(reportRepository.save(ArgumentMatchers.any(ReportEntity.class)))
                .thenAnswer(i -> {
                    ReportEntity saved = (ReportEntity) i.getArguments()[0];
                    saved.setId(6L);
                    return saved;
                });

        when(dataReportRepository.saveAll(ArgumentMatchers.anyList())).thenReturn(new ArrayList<>());

        // When
        List<ReportEntity> result = reportServices.ReportClientLoanLate();

        // Then
        assertEquals(1, result.size());
        assertEquals("ReportClientLoanLate", result.get(0).getName());

        verify(clientServices, times(1)).getAllClientLoanLate();
        verify(reportRepository, times(1)).save(ArgumentMatchers.any(ReportEntity.class));

        // CORREGIDO: Casting explícito a List para usar size()
        verify(dataReportRepository, times(1)).saveAll(ArgumentMatchers.argThat(list -> ((List<?>) list).size() == 2));
    }


    @Test
    public void testCreateTopToolsReport() {
        // Given
        Object[] row1 = {100L, "Martillo", 5L};
        Object[] row2 = {101L, "Sierra", 3L};
        List<Object[]> mockRanking = Arrays.asList(row1, row2);
        when(kardexServices.TopToolKardexTool()).thenReturn(mockRanking);

        when(reportRepository.save(ArgumentMatchers.any(ReportEntity.class)))
                .thenAnswer(i -> {
                    ReportEntity saved = (ReportEntity) i.getArguments()[0];
                    saved.setId(7L);
                    return saved;
                });

        when(dataReportRepository.saveAll(ArgumentMatchers.anyList())).thenReturn(new ArrayList<>());

        // When
        List<ReportEntity> result = reportServices.createTopToolsReport();

        // Then
        assertEquals(1, result.size());
        assertEquals("ReportTopTools", result.get(0).getName());

        verify(kardexServices, times(1)).TopToolKardexTool();
        verify(reportRepository, times(1)).save(ArgumentMatchers.any(ReportEntity.class));

        // CORREGIDO: Casting explícito a List para usar size() y get()
        verify(dataReportRepository, times(1)).saveAll(ArgumentMatchers.argThat(iterable -> {
            List<DataReportEntity> list = (List<DataReportEntity>) iterable;
            if (list.size() != 2) return false;

            // list.get() ahora es válido
            DataReportEntity dr1 = list.get(0);
            DataReportEntity dr2 = list.get(1);

            return dr1.getIdTool().equals(100L) && dr1.getNumber_of_times_borrowed().equals(5L) &&
                    dr2.getIdTool().equals(101L) && dr2.getNumber_of_times_borrowed().equals(3L);
        }));
    }
}