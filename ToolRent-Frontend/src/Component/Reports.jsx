import React, { useState, useEffect } from "react";
import { useNavigate } from 'react-router-dom';
import {
  ThemeProvider, CssBaseline, Typography, Box, Paper, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, Button, Modal, Container, Drawer, List, 
  ListItem, ListItemButton, ListItemIcon, ListItemText, IconButton, Stack, TextField, Checkbox
} from "@mui/material";
import { createTheme } from "@mui/material/styles";
import { useKeycloak } from "@react-keycloak/web";
import HomeIcon from "@mui/icons-material/Home";
import BuildIcon from "@mui/icons-material/Build";
import LibraryAddIcon from "@mui/icons-material/LibraryAdd";
import AssessmentIcon from "@mui/icons-material/Assessment";
import ContactsIcon from "@mui/icons-material/Contacts";
import PersonAddAltIcon from '@mui/icons-material/PersonAddAlt';
import AdminPanelSettingsIcon from "@mui/icons-material/AdminPanelSettings";
import ReportIcon from '@mui/icons-material/Report';
import ReportsService from "../Services/ReportsServices";
import DataReportServices from "../Services/DataReportServices";
import LoanToolsServices from "../Services/LoanToolsServices";
import ClientServices from "../Services/ClientServices";
import ToolServices from "../Services/ToolServices";
import MenuIcon from "@mui/icons-material/Menu";
import DeleteIcon from '@mui/icons-material/Delete'; // Importar icono

const theme = createTheme({
  palette: {
    background: { default: "#FEF3E2" }
  }
});

const Reports = () => {
  const navigate = useNavigate();
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [openModal, setOpenModal] = useState(false);
  const [selectedReport, setSelectedReport] = useState(null);
  const [dataDetails, setDataDetails] = useState(null);
  const [dateModalOpen, setDateModalOpen] = useState(false);
  const [dateFilterType, setDateFilterType] = useState("");
  const [initDate, setInitDate] = useState("");
  const [endDate, setEndDate] = useState("");
  const [activeLoans, setActiveLoans] = useState([]);
  const [clientsWithDelays, setClientsWithDelays] = useState([]);
  const [topTools, setTopTools] = useState([]);
  const [isFiltered, setIsFiltered] = useState(false);
  const [selectedIds, setSelectedIds] = useState([]); // Estado para las palomitas

  const { keycloak } = useKeycloak();
  const isAdmin = keycloak?.tokenParsed?.realm_access?.roles?.includes("ADMIN");

  const sidebarOptions = [
    { text: "Inicio", icon: <HomeIcon />, path: "/" },
    { text: "Herramientas", icon: <BuildIcon />, path: "/ToolList" },
    { text: "Agregar Herramienta", icon: <LibraryAddIcon />, path: "/AddTools" },
    { text: "Ver Kardex", icon: <AssessmentIcon />, path: "/Kardex" },
    { text: "Registrar Cliente", icon: <PersonAddAltIcon />, path: "/RegisterClient" },
    { text: "Clientes", icon: <ContactsIcon />, path: "/ClientList" },
    { text: "Reportes", icon: <ReportIcon />, path: "/Reports" },
    ...(isAdmin ? [{ text: "Configuraciones", icon: <AdminPanelSettingsIcon />, path: "/Configuration" }] : [])
  ];

  const showActiveLoansReport = () => {
    ReportsService.getallReportsLoans().then(res => setActiveLoans(res.data || [])).catch(console.error);
  };

  const showClientsLateReport = () => {
    ReportsService.getallReportsClientLate().then(res => setClientsWithDelays(res.data || [])).catch(console.error);
  };

  const showTopToolsReport = () => {
    ReportsService.getTopToolsReport().then(res => setTopTools(res.data || [])).catch(console.error);
  };

  useEffect(() => {
    console.log("IDs seleccionados actualmente:", selectedIds);
    showActiveLoansReport();
    showClientsLateReport();
    showTopToolsReport();
  }, [], [selectedIds]);

  const handleOpenModal = (report) => {
    setSelectedReport(report);
    setOpenModal(true);
    if (report.name === "ReportLoanTools") getselectedDataActiveLoan(report.id);
    else if (report.name === "ReportClientLoanLate") getselectedDataClientsLateReport(report.id);
    else if (report.name === "ReportTopTools") getselectedTopToolsReport(report.id);
  };

  // ... (Tus funciones generatereport y getselectedData se mantienen igual)
  const generatereportActiveLoan = () => ReportsService.createLoanReport().then(() => { showActiveLoansReport(); alert("Generado"); });
  const generatereportClientLate = () => ReportsService.createClientLateReport().then(() => { showClientsLateReport(); alert("Generado"); });
  const generatereportTopTools = () => ReportsService.createTopToolsReport().then(() => { showTopToolsReport(); alert("Generado"); });

  const getselectedDataActiveLoan = (id) => {
    DataReportServices.getdataReportByIdreport(id).then(async (response) => {
      const dataReport = response.data;
      if (Array.isArray(dataReport)) {
        const details = await Promise.all(dataReport.map(async (r) => {
          const loanRes = await LoanToolsServices.getid(r.idLoanTool);
          const clientRes = await ClientServices.getByid(loanRes.data.clientid);
          const toolRes = await ToolServices.getid(loanRes.data.toolid);
          return {
            loanstartdate: loanRes.data.initiallenddate,
            loanfinaldate: loanRes.data.finalreturndate,
            clientname: clientRes.data.name,
            toolname: toolRes.data.name,
          };
        }));
        setDataDetails(details);
      }
    });
  };

  const getselectedDataClientsLateReport = (id) => DataReportServices.getdataReportByIdreport(id).then(res => setDataDetails(res.data));

  const getselectedTopToolsReport = (id) => {
    DataReportServices.getdataReportByIdreport(id).then(async (response) => {
      const dataReport = response.data;
      if (Array.isArray(dataReport)) {
        const details = await Promise.all(dataReport.map(async (r) => {
          const toolRes = await ToolServices.getid(r.idTool);
          return { toolname: toolRes.data.name, number_of_times_borrowed: r.number_of_times_borrowed };
        }));
        setDataDetails(details);
      }
    });
  };

  const applyDateFilter = () => {
    if (!initDate || !endDate) return alert("Seleccione fechas");
    ReportsService.reportdate(initDate, endDate).then(res => {
      const filtered = res.data || [];
      if (dateFilterType === "loan") setActiveLoans(filtered.filter(r => r.name === "ReportLoanTools"));
      if (dateFilterType === "late") setClientsWithDelays(filtered.filter(r => r.name === "ReportClientLoanLate"));
      if (dateFilterType === "top") setTopTools(filtered.filter(r => r.name === "ReportTopTools"));
      setIsFiltered(true);
      setDateModalOpen(false);
    });
  };

  const removeFilter = () => {
    if (dateFilterType === "loan") showActiveLoansReport();
    if (dateFilterType === "late") showClientsLateReport();
    if (dateFilterType === "top") showTopToolsReport();
    setIsFiltered(false);
    setDateModalOpen(false);
  };

  const handleCloseModal = () => { setOpenModal(false); setDataDetails(null); };

const handleDeleteSelected = () => {
    if (selectedIds.length === 0) return;

    if (window.confirm(`¿Estás seguro de eliminar los ${selectedIds.length} reportes seleccionados?`)) {

      console.log("Iniciando eliminación masiva. Lista de IDs:", selectedIds);
        
        // Enviamos directamente el array de números [1, 2, 5, ...]
        ReportsService.deleteReportsById(selectedIds)
            .then(() => {
                alert("Reportes eliminados con éxito");
                
                // 1. Limpiar la selección
                setSelectedIds([]); 
                
                // 2. Refrescar las tablas llamando a tus funciones de carga
                showActiveLoansReport();
                showClientsLateReport();
                showTopToolsReport();
            })
            .catch((error) => {
                console.error("Error en la eliminación masiva:", error);
                alert("No se pudieron eliminar algunos reportes. Verifique la consola.");
            });
    }
};

  const handleSelectOne = (id) => {
    setSelectedIds(prev => 
      prev.includes(id) ? prev.filter(item => item !== id) : [...prev, id]
    );
  };

  const handleSelectAllInSection = (data) => {
    const sectionIds = data.map(r => r.id);
    const allSelected = sectionIds.every(id => selectedIds.includes(id));
    
    if (allSelected) {
      setSelectedIds(prev => prev.filter(id => !sectionIds.includes(id)));
    } else {
      setSelectedIds(prev => [...new Set([...prev, ...sectionIds])]);
    }
  };

  const renderReportDetails = () => {
    if (!dataDetails) return <Typography sx={{ p: 2 }}>Cargando...</Typography>;
    return (
      <Box sx={{ maxHeight: '60vh', overflowY: 'auto', p: 1 }}>
        {dataDetails.map((detail, index) => (
          <Paper key={index} sx={{ p: 2, mb: 2, backgroundColor: "#FEF3E2", border: '1px solid rgba(255,94,0,0.2)' }}>
            {selectedReport.name === "ReportLoanTools" ? (
              <>
                <Typography variant="body2"><strong>Cliente:</strong> {detail.clientname}</Typography>
                <Typography variant="body2"><strong>Herramienta:</strong> {detail.toolname}</Typography>
                <Typography variant="body2"><strong>Devolución:</strong> {detail.loanfinaldate}</Typography>
              </>
            ) : (
              <>
                <Typography variant="body2"><strong>Herramienta:</strong> {detail.toolname}</Typography>
                <Typography variant="body2"><strong>Préstamos:</strong> {detail.number_of_times_borrowed}</Typography>
              </>
            )}
          </Paper>
        ))}
      </Box>
    );
  };

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      {/* Fondo Fijo */}
      <Box sx={{ position: "fixed", top: 0, left: 0, width: "100vw", height: "100vh", backgroundColor: "#FEF3E2", zIndex: -1 }} />

      {/* Botón de Borrado Masivo Flotante (Solo aparece si hay seleccionados) */}
      {selectedIds.length > 0 && (
        <Box sx={{ position: 'fixed', bottom: 32, right: 32, zIndex: 2000 }}>
          <Button 
            variant="contained" 
            color="error" 
            size="large"
            startIcon={<DeleteIcon />}
            onClick={handleDeleteSelected}
            sx={{ 
              borderRadius: '50px', px: 4, py: 1.5, boxShadow: '0px 4px 20px rgba(0,0,0,0.3)',
              textTransform: 'none', fontWeight: 'bold' 
            }}
          >
            Eliminar seleccionados ({selectedIds.length})
          </Button>
        </Box>
      )}

      {/* Menu / Drawer */}
      <IconButton onClick={() => setDrawerOpen(true)} sx={{ position: "fixed", top: 16, left: 16, zIndex: 1100, backgroundColor: "#FA812F", color: "white", "&:hover": { backgroundColor: "#FA812F" } }}>
        <MenuIcon />
      </IconButton>
      <Drawer open={drawerOpen} onClose={() => setDrawerOpen(false)} sx={{ [`& .MuiDrawer-paper`]: { width: 240, backgroundColor: "#FEF3E2" } }}>
        <List>
          {sidebarOptions.map((option) => (
            <ListItem key={option.text} disablePadding>
              <ListItemButton onClick={() => { navigate(option.path); setDrawerOpen(false); }}>
                <ListItemIcon sx={{ color: "#FA812F" }}>{option.icon}</ListItemIcon>
                <ListItemText primary={option.text} />
              </ListItemButton>
            </ListItem>
          ))}
        </List>
      </Drawer>

      <Container sx={{ pt: 10, pb: 5 }}>
        <Typography variant="h3" align="center" sx={{ fontWeight: "bold", color: "#FA812F", mb: 6 }}>
          📊 Centro de Reportes
        </Typography>

        {[ 
          { title: "Préstamos Activos", data: activeLoans, type: "loan", gen: generatereportActiveLoan },
          { title: "Clientes con Atraso", data: clientsWithDelays, type: "late", gen: generatereportClientLate },
          { title: "Herramientas Más Prestadas", data: topTools, type: "top", gen: generatereportTopTools }
        ].map((section, idx) => (
          <Paper key={idx} sx={{ p: 4, mb: 6, borderRadius: 4, backgroundColor: "#fff8f0", border: "2px solid rgba(255, 94, 0, 0.1)", boxShadow: 4 }}>
            <Stack direction="row" justifyContent="space-between" alignItems="center" mb={3} flexWrap="wrap" gap={2}>
              <Typography variant="h6" sx={{ fontWeight: "bold", color: "#FA812F" }}>
                {section.title} ({section.data.length})
              </Typography>
              <Stack direction="row" spacing={2}>
                <Button variant="contained" onClick={() => { setDateFilterType(section.type); setDateModalOpen(true); }} sx={{ backgroundColor: "#FA812F", "&:hover": { backgroundColor: "#e06a1d" } }}>
                  Filtrar
                </Button>
                <Button variant="outlined" onClick={section.gen} sx={{ borderColor: "#FA812F", color: "#FA812F", "&:hover": { borderColor: "#e06a1d" } }}>
                  Generar
                </Button>
              </Stack>
            </Stack>

            <TableContainer component={Paper} sx={{ boxShadow: 2 }}>
              <Table stickyHeader>
                <TableHead>
                  <TableRow>
                    {/* Nueva celda para Seleccionar Todo en esta sección */}
                    <TableCell sx={{ backgroundColor: "#FA812F", width: 50 }}>
                      <Checkbox 
                        sx={{ color: "white", '&.Mui-checked': { color: 'white' }, '&.MuiCheckbox-indeterminate': { color: 'white' } }}
                        indeterminate={section.data.some(r => selectedIds.includes(r.id)) && !section.data.every(r => selectedIds.includes(r.id))}
                        checked={section.data.length > 0 && section.data.every(r => selectedIds.includes(r.id))}
                        onChange={() => handleSelectAllInSection(section.data)}
                      />
                    </TableCell>
                    {["Fecha Reporte", "Tipo", "Acción"].map(h => (
                      <TableCell key={h} sx={{ backgroundColor: "#FA812F", color: "white", fontWeight: "bold" }}>{h}</TableCell>
                    ))}
                  </TableRow>
                </TableHead>
                <TableBody>
                  {section.data.map((report, i) => (
                    <TableRow key={report.id || i} hover selected={selectedIds.includes(report.id)}>
                      <TableCell>
                        <Checkbox 
                          checked={selectedIds.includes(report.id)}
                          onChange={() => handleSelectOne(report.id)}
                          sx={{ color: "#FA812F", '&.Mui-checked': { color: '#FA812F' } }}
                        />
                      </TableCell>
                      <TableCell>{report.date}</TableCell>
                      <TableCell>{section.title}</TableCell>
                      <TableCell>
                        <Button size="small" variant="contained" onClick={() => handleOpenModal(report)} sx={{ backgroundColor: "#FA812F" }}>Ver Detalles</Button>
                      </TableCell>
                    </TableRow>
                  ))}
                  {section.data.length === 0 && (
                    <TableRow>
                      <TableCell colSpan={4} align="center" sx={{ py: 3 }}>No hay reportes disponibles</TableCell>
                    </TableRow>
                  )}
                </TableBody>
              </Table>
            </TableContainer>
          </Paper>
        ))}
      </Container>

      {/* Modales - Se mantienen igual */}
      <Modal open={openModal} onClose={handleCloseModal}>
        <Box sx={{ position: "absolute", top: "50%", left: "50%", transform: "translate(-50%, -50%)", width: {xs: '90%', sm: 500}, backgroundColor: "white", p: 4, borderRadius: 3, boxShadow: 24 }}>
          <Typography variant="h6" sx={{ color: "#FA812F", fontWeight: "bold", mb: 2 }}>Detalles del Reporte</Typography>
          {renderReportDetails()}
          <Button fullWidth onClick={handleCloseModal} sx={{ mt: 3, backgroundColor: "#FA812F", color: "white" }}>Cerrar</Button>
        </Box>
      </Modal>

      <Modal open={dateModalOpen} onClose={() => setDateModalOpen(false)}>
        <Box sx={{ position: "absolute", top: "50%", left: "50%", transform: "translate(-50%, -50%)", width: 350, backgroundColor: "white", p: 4, borderRadius: 3 }}>
          <Typography variant="h6" sx={{ color: "#FA812F", mb: 3, fontWeight: 'bold' }}>Filtrar por Fecha</Typography>
          <Stack spacing={3}>
            <TextField type="date" label="Inicio" InputLabelProps={{ shrink: true }} value={initDate} onChange={e => setInitDate(e.target.value)} fullWidth />
            <TextField type="date" label="Fin" InputLabelProps={{ shrink: true }} value={endDate} onChange={e => setEndDate(e.target.value)} fullWidth />
            <Button variant="contained" fullWidth onClick={applyDateFilter} sx={{ backgroundColor: "#FA812F" }}>Aplicar</Button>
            {isFiltered && <Button variant="outlined" fullWidth onClick={removeFilter} color="error">Quitar Filtro</Button>}
          </Stack>
        </Box>
      </Modal>
    </ThemeProvider>
  );
};
export default Reports;