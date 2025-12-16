import React, { useState, useEffect } from "react";
import { useNavigate } from 'react-router-dom';
import {
  ThemeProvider, CssBaseline, Typography, Box, Paper, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, Button, Modal, Container, Drawer, List,  ListItem, ListItemButton, ListItemIcon, ListItemText, IconButton} from "@mui/material";
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

const theme = createTheme({
  palette: {
    background: { default: "#FEF3E2" }
  }
});

const Reports = () => {

  const navigate = useNavigate();
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [openModal, setOpenModal] = useState(false); // Estado para abrir/cerrar el modal
  const [selectedReport, setSelectedReport] = useState(null); // Estado para los detalles del reporte seleccionado
  const [dataDetails, setDataDetails] = useState(null); // Estado para almacenar los detalles del reporte
    // Modal de filtro por fecha
  const [dateModalOpen, setDateModalOpen] = useState(false);
  const [dateFilterType, setDateFilterType] = useState(""); // "loan" | "late" | "top"
  const [initDate, setInitDate] = useState("");
  const [endDate, setEndDate] = useState("");

  // Estados de los reportes
  const [activeLoans, setActiveLoans] = useState([]);
  const [clientsWithDelays, setClientsWithDelays] = useState([]);
  const [topTools, setTopTools] = useState([]);
  const [isFiltered, setIsFiltered] = useState(false);


  // ADMIN CHECK
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
  

  // Mostrar los reportes de préstamos activos
  const showActiveLoansReport = () => {
    ReportsService.getallReportsLoans()
      .then((response) => {
        const reports = response.data || [];
        setActiveLoans(reports);
      })
      .catch((error) => {
        console.error("Error en la solicitud de reportes", error);
      });
  };

  // Mostrar los reportes de clientes con atraso
  const showClientsLateReport = () => {
    ReportsService.getallReportsClientLate()
      .then((response) => {
        const reports = response.data || [];
        setClientsWithDelays(reports);
      })
      .catch((error) => {
        console.error("Error al obtener los reportes de clientes con atraso", error);
      });
  };

  // Mostrar los reportes de las herramientas más prestadas
  const showTopToolsReport = () => {
    ReportsService.getTopToolsReport()
      .then((response) => {
        setTopTools(response.data);
      })
      .catch((error) => {
        console.error("Error fetching top tools report:", error);
      });
  };

const generatereportActiveLoan = () => { 
    ReportsService.createLoanReport().then(() => {
        showActiveLoansReport(); // 🔥 refresco automático
        alert("Reporte generado correctamente");
    });
}

const generatereportClientLate = () => { 
    ReportsService.createClientLateReport().then(() => {
        showClientsLateReport(); // 🔥 refresco automático
        alert("Reporte generado correctamente");
    });
}

const generatereportTopTools = () => { 
    ReportsService.createTopToolsReport().then(() => {
        showTopToolsReport(); // 🔥 refresco automático
        alert("Reporte generado correctamente");
    });
}
 
  // Cargar los reportes al montar el componente
  useEffect(() => {
    showActiveLoansReport();
    showClientsLateReport();
    showTopToolsReport();
  }, []);

  // Función para obtener los detalles de los reportes de préstamos activos
  const getselectedDataActiveLoan = (selectedid) => {
    DataReportServices.getdataReportByIdreport(selectedid)
      .then(async (response) => {
        const dataReport = response.data;
        if (Array.isArray(dataReport)) {
          const loanDetails = await Promise.all(
            dataReport.map(async (r) => {
              if (r.idLoanTool) {
                const loanResponse = await LoanToolsServices.getid(r.idLoanTool);
                const clientResponse = await ClientServices.getByid(loanResponse.data.clientid);
                const toolResponse = await ToolServices.getid(loanResponse.data.toolid);

                return {
                  loanstartdate: loanResponse.data.initiallenddate,
                  loanfinaldate: loanResponse.data.finalreturndate,
                  clientname: clientResponse.data.name,
                  toolname: toolResponse.data.name,
                  reportDate: r.date,
                };
              }
            })
          );
          setDataDetails(loanDetails.filter((loan) => loan !== null));
        }
      })
      .catch((error) => {
        console.error("Error al obtener los detalles del reporte de préstamos activos", error);
      });
  };

  // Función para obtener los detalles de los reportes de clientes con atraso
  const getselectedDataClientsLateReport = (selectedid) => {
    DataReportServices.getdataReportByIdreport(selectedid)
      .then((response) => {
        setDataDetails(response.data);
      })
      .catch((error) => {
        console.error("Error al obtener los detalles de clientes con atraso", error);
      });
  };

  // Función para obtener los detalles de los reportes de herramientas más prestadas
  const getselectedTopToolsReport = (selectedid) => {
    DataReportServices.getdataReportByIdreport(selectedid)
      .then(async (response) => {
        const datareport = response.data;
        console.log("reportes", datareport)
        if (Array.isArray(datareport)) {
          const topToolsDetails = await Promise.all(
            datareport.map(async (r) => {
              if (r.idTool) {
                const toolResponse = await ToolServices.getid(r.idTool);
                return {
                  toolname: toolResponse.data.name,
                  number_of_times_borrowed: r.number_of_times_borrowed

                };
              }
            })
          );
          setDataDetails(topToolsDetails.filter((tool) => tool !== null));
        }
      })
      .catch((error) => {
        console.error("Error al obtener los detalles de herramientas más prestadas", error);
      });
  };

  // Abrir modal con los detalles del reporte
  const handleOpenModal = (report) => {
    setSelectedReport(report); // Guardar el reporte seleccionado
    setOpenModal(true); // Abrir el modal

    // Dependiendo del tipo de reporte, obtener los detalles
    if (report.name === "ReportLoanTools") {
      getselectedDataActiveLoan(report.id);
    } else if (report.name === "ReportClientLoanLate") {
      getselectedDataClientsLateReport(report.id);
    } else if (report.name === "ReportTopTools") {
      getselectedTopToolsReport(report.id);
    }
  };

  // Cerrar modal
  const handleCloseModal = () => {
    setOpenModal(false);
    setSelectedReport(null);
    setDataDetails(null);
  };

  const applyDateFilter = () => {
  if (!initDate || !endDate) {
    alert("Debe seleccionar ambas fechas");
    return;
  }

  ReportsService.reportdate(initDate, endDate)
    .then(response => {
      const filtered = response.data || [];

      if (dateFilterType === "loan") {
        setActiveLoans(filtered.filter(r => r.name === "ReportLoanTools"));
      }

      if (dateFilterType === "late") {
        setClientsWithDelays(filtered.filter(r => r.name === "ReportClientLoanLate"));
      }

      if (dateFilterType === "top") {
        setTopTools(filtered.filter(r => r.name === "ReportTopTools"));
      }

      setIsFiltered(true);   // 🔥 activa indicador de filtro
      setDateModalOpen(false);
    })
    .catch(err => console.error(err));
};

const removeFilter = () => {
    if (dateFilterType === "loan") {
      showActiveLoansReport();
    }
    if (dateFilterType === "late") {
      showClientsLateReport();
    }
    if (dateFilterType === "top") {
      showTopToolsReport();
    }

    setInitDate("");
    setEndDate("");
    setIsFiltered(false);
    setDateModalOpen(false);
  };




  // Modal de detalles
  const renderReportDetails = () => {
  if (dataDetails) {
    if (selectedReport.name === "ReportLoanTools") {
      return (
        <Box>
          <Typography variant="h6" sx={{ fontWeight: "bold", color: "rgba(255,94,0,1)", mb: 2 }}>
            Detalles del Reporte de Préstamos Activos
          </Typography>
          {dataDetails.map((detail, index) => (
            <Paper key={index} sx={{ p: 2, mb: 2, borderRadius: 2, boxShadow: 2 }}>
              <Typography variant="body1" sx={{ mb: 1 }}>
                <strong>Cliente:</strong> {detail.clientname}
              </Typography>
              <Typography variant="body1" sx={{ mb: 1 }}>
                <strong>Herramienta:</strong> {detail.toolname}
              </Typography>
              <Typography variant="body1" sx={{ mb: 1 }}>
                <strong>Fecha de Inicio:</strong> {detail.loanstartdate}
              </Typography>
              <Typography variant="body1" sx={{ mb: 1 }}>
                <strong>Fecha de Finalización:</strong> {detail.loanfinaldate}
              </Typography>
            </Paper>
          ))}
        </Box>
      );
    } else if (selectedReport.name === "ReportTopTools") {
      return (
        <Box>
          <Typography variant="h6" sx={{ fontWeight: "bold", color: "rgba(255,94,0,1)", mb: 2 }}>
            Detalles del Reporte de Herramientas Más Prestadas
          </Typography>
          {dataDetails.map((detail, index) => (
            <Paper key={index} sx={{ p: 2, mb: 2, borderRadius: 2, boxShadow: 2 }}>
              <Typography variant="body1" sx={{ mb: 1 }}>
                <strong>Herramienta:</strong> {detail.toolname}
              </Typography>
              <Typography variant="body1" sx={{ mb: 1 }}>
                <strong>Cantidad:</strong> {detail.number_of_times_borrowed}
              </Typography>
            </Paper>
          ))}
        </Box>
      );
    }
  }
  return <Typography variant="body1" sx={{ fontStyle: "italic", color: "rgba(0,0,0,0.6)" }}>Cargando detalles...</Typography>;
};


  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />

      <Box sx={{ p: 4, minHeight: "100vh" }}>
                      {/* SlideBar*/}
                      <IconButton
                          color="inherit"
                          onClick={() => setDrawerOpen(true)}
                          sx={{ 
                              position: "fixed", 
                              top: 16, 
                              left: 16, 
                              zIndex: 10, 
                              backgroundColor: "#FA812F", 
                              boxShadow: 3,
                              '&:hover': { backgroundColor: "#FA812F" }
                          }}
                      >
                          <MenuIcon sx={{ color: "#FEF3E2" }} />
                      </IconButton>

                    <Drawer
                          open={drawerOpen}
                          onClose={() => setDrawerOpen(false)}
                          variant="temporary"
                          sx={{
                              [`& .MuiDrawer-paper`]: { 
                                  width: 240, 
                                  boxSizing: "border-box", 
                                  backgroundColor: "#FEF3E2" 
                              }
                          }}
                      >
                          <List>
                              {sidebarOptions.map((option) => (
                                  <ListItem key={option.text} disablePadding>
                                      <ListItemButton 
                                          onClick={() => { 
                                              navigate(option.path); 
                                              setDrawerOpen(false); 
                                          }}
                                          sx={{
                                              '&:hover': {
                                                  backgroundColor: 'rgba(255, 94, 0, 0.1)',
                                              },
                                              borderRadius: 1,
                                              mx: 1,
                                              my: 0.5
                                          }}
                                      >
                                          <ListItemIcon sx={{ color: "rgba(255, 94, 0, 1)", minWidth: 40 }}>
                                              {option.icon}
                                          </ListItemIcon>
                                          <ListItemText 
                                              primary={option.text}
                                              sx={{
                                                  '& .MuiListItemText-primary': {
                                                      fontWeight: 500,
                                                      fontSize: '0.95rem'
                                                  }
                                              }}
                                          />
                                      </ListItemButton>
                                  </ListItem>
                              ))}
                          </List>
                      </Drawer>


        {/*Label de todo el compenente de los reportes */}          
      <Typography variant="h3" align="center" sx={{ fontWeight: "bold", color: "rgba(255,94,0,1)", mb: 4 }}>
        📊 Centro de Reportes
      </Typography>
      <Container sx={{ mt: 10, mb: 5 }}>
        {/* Reportes: Préstamos Activos */}
        <Paper sx={{ p: 4, mb: 5, borderRadius: 4, backgroundColor: "#fff", boxShadow: "0 4px 18px rgba(255,94,0,0.15)" }}>
          <Typography variant="h6" sx={{ fontWeight: "bold", color: "rgba(255,94,0,1)", mb: 3 }}>
            📋 Préstamos Activos ({activeLoans.length})

            <Button
              onClick={() => { setDateFilterType("loan"); setDateModalOpen(true); }}
              sx={{ 
                backgroundColor: "rgba(255,94,0,1)",  // Color de fondo naranja
                color: "#fff",  // Texto blanco
                padding: "6px 12px",  // Ajusta el tamaño del botón
                fontWeight: "bold",  // Establece el peso de la fuente
                fontSize: "0.875rem",  // Ajusta el tamaño de la fuente
                borderRadius: 2,  // Borde redondeado
                "&:hover": {
                  backgroundColor: "rgba(255,94,0,0.8)",  // Color al pasar el mouse (opaco)
                },
                translate: "9px"
              }}
            >
              Filtrar por fecha
            </Button>

            <Button 
            onClick = {() => generatereportActiveLoan()} 
            sx={{
                backgroundColor: "rgba(255,94,0,1)",  // Color de fondo naranja
                color: "#fff",  // Texto blanco
                padding: "6px 12px",  // Ajusta el tamaño del botón
                fontWeight: "bold",  // Establece el peso de la fuente
                fontSize: "0.875rem",  // Ajusta el tamaño de la fuente
                borderRadius: 2,  // Borde redondeado
                "&:hover": {
                  backgroundColor: "rgba(255,94,0,0.8)",  // Color al pasar el mouse (opaco)
                },
                translate: "12px"
              }}>

            Generar reporte
            </Button>
          </Typography>
          
          <TableContainer>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Fecha Reporte</TableCell>
                  <TableCell>Tipo Reporte</TableCell>
                  <TableCell>Acción</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {activeLoans.map((loan, i) => (
                  <TableRow key={i}>
                    <TableCell>{loan.date}</TableCell>
                    <TableCell>Préstamos Activos</TableCell>
                    <TableCell>
                      <Button onClick={() => handleOpenModal(loan)}
                      sx={{
                        backgroundColor: "rgba(255,94,0,1)",  // Color de fondo naranja
                        color: "#fff",  // Texto blanco
                        padding: "6px 12px",  // Ajusta el tamaño del botón
                        fontWeight: "bold",  // Establece el peso de la fuente
                        fontSize: "0.875rem",  // Ajusta el tamaño de la fuente
                        borderRadius: 2,  // Borde redondeado
                        "&:hover": {
                          backgroundColor: "rgba(255,94,0,0.8)",  // Color al pasar el mouse (opaco)
                        }
                        
                      }}>
                      Ver detalles</Button>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </Paper>

        {/* Reportes: Clientes con Atraso */}
        <Paper sx={{ p: 4, mb: 5, borderRadius: 4, backgroundColor: "#fff", boxShadow: "0 4px 18px rgba(255,94,0,0.15)" }}>
          <Typography variant="h6" sx={{ fontWeight: "bold", color: "rgba(255,94,0,1)", mb: 3 }}>
            ⚠ Clientes con Atraso ({clientsWithDelays.length})

            <Button
              onClick={() => { setDateFilterType("late"); setDateModalOpen(true); }}
              sx={{
                backgroundColor: "rgba(255,94,0,1)",  // Color de fondo naranja
                color: "#fff",  // Texto blanco
                padding: "6px 12px",  // Ajusta el tamaño del botón
                fontWeight: "bold",  // Establece el peso de la fuente
                fontSize: "0.875rem",  // Ajusta el tamaño de la fuente
                borderRadius: 2,  // Borde redondeado
                "&:hover": {
                  backgroundColor: "rgba(255,94,0,0.8)",  // Color al pasar el mouse (opaco)
                },
                translate: "9px"
               }}
            >
              Filtrar por fecha
            </Button>

            <Button 
            onClick = {() => generatereportClientLate()} 
            sx={{
                backgroundColor: "rgba(255,94,0,1)",  // Color de fondo naranja
                color: "#fff",  // Texto blanco
                padding: "6px 12px",  // Ajusta el tamaño del botón
                fontWeight: "bold",  // Establece el peso de la fuente
                fontSize: "0.875rem",  // Ajusta el tamaño de la fuente
                borderRadius: 2,  // Borde redondeado
                "&:hover": {
                  backgroundColor: "rgba(255,94,0,0.8)",  // Color al pasar el mouse (opaco)
                },
                translate: "12px"
              }}>

            Generar reporte
            </Button>
          </Typography>
          <TableContainer>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Fecha Reporte</TableCell>
                  <TableCell>Tipo Reporte</TableCell>
                  <TableCell>Acción</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {clientsWithDelays.map((client, i) => (
                  <TableRow key={i}>
                    <TableCell>{client.date}</TableCell>
                    <TableCell>Clientes con Atraso</TableCell>
                    <TableCell>
                      <Button onClick={() => handleOpenModal(client)} 
                        sx={{
                            backgroundColor: "rgba(255,94,0,1)",  // Color de fondo naranja
                            color: "#fff",  // Texto blanco
                            padding: "6px 12px",  // Ajusta el tamaño del botón
                            fontWeight: "bold",  // Establece el peso de la fuente
                            fontSize: "0.875rem",  // Ajusta el tamaño de la fuente
                            borderRadius: 2,  // Borde redondeado
                            "&:hover": {
                              backgroundColor: "rgba(255,94,0,0.8)",  // Color al pasar el mouse (opaco)
                            },
                            translate: "12px"
                          }}
                        >Ver detalles</Button>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </Paper>

        {/* Reportes: Herramientas Más Prestadas */}
        <Paper sx={{ p: 4, mb: 5, borderRadius: 4, backgroundColor: "#fff", boxShadow: "0 4px 18px rgba(255,94,0,0.15)" }}>
          <Typography variant="h6" sx={{ fontWeight: "bold", color: "rgba(255,94,0,1)", mb: 3 }}>
            🏆 Herramientas Más Prestadas ({topTools.length})

            <Button
              onClick={() => { setDateFilterType("top"); setDateModalOpen(true); }}
              sx={{ 
                backgroundColor: "rgba(255,94,0,1)",  // Color de fondo naranja
                color: "#fff",  // Texto blanco
                padding: "6px 12px",  // Ajusta el tamaño del botón
                fontWeight: "bold",  // Establece el peso de la fuente
                fontSize: "0.875rem",  // Ajusta el tamaño de la fuente
                borderRadius: 2,  // Borde redondeado
                "&:hover": {
                  backgroundColor: "rgba(255,94,0,0.8)",  // Color al pasar el mouse (opaco)
                },
                translate: "9px"
               }}
            >
              Filtrar por fecha
            </Button>

            {/* Usando el componente Button de Material-UI */}
            <Button
              onClick={() => generatereportTopTools()}
              sx={{
                backgroundColor: "rgba(255,94,0,1)",  // Color de fondo naranja
                color: "#fff",  // Texto blanco
                padding: "6px 12px",  // Ajusta el tamaño del botón
                fontWeight: "bold",  // Establece el peso de la fuente
                fontSize: "0.875rem",  // Ajusta el tamaño de la fuente
                borderRadius: 2,  // Borde redondeado
                "&:hover": {
                  backgroundColor: "rgba(255,94,0,0.8)",  // Color al pasar el mouse (opaco)
                },
                translate: "12px"
              }}
            >
              Generar reporte
            </Button>
          </Typography>
          <TableContainer>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Fecha Reporte</TableCell>
                  <TableCell>Tipo Reporte</TableCell>
                  <TableCell>Acción</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {topTools.map((tool, i) => (
                  <TableRow key={i}>
                    <TableCell>{tool.date}</TableCell>
                    <TableCell>Herramientas Más Prestadas</TableCell>
                    <TableCell>
                      <Button onClick={() => handleOpenModal(tool)} sx={{
                          backgroundColor: "rgba(255,94,0,1)",  // Color de fondo naranja
                          color: "#fff",  // Texto blanco
                          padding: "6px 12px",  // Ajusta el tamaño del botón
                          fontWeight: "bold",  // Establece el peso de la fuente
                          fontSize: "0.875rem",  // Ajusta el tamaño de la fuente
                          borderRadius: 2,  // Borde redondeado
                          "&:hover": {
                            backgroundColor: "rgba(255,94,0,0.8)",  // Color al pasar el mouse (opaco)
                          },
                          translate: "12px"
                        }}>
                          Ver detalles
                        </Button>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </Paper>
      </Container>

      {/* Modal de detalles */}
      <Modal open={openModal} onClose={handleCloseModal}>
        <Box sx={{
          position: "absolute",
          top: "50%",
          left: "50%",
          transform: "translate(-50%, -50%)",
          width: 400,
          backgroundColor: "white",
          padding: 3,
          boxShadow: 24,
          borderRadius: 2,
        }}>
          {renderReportDetails()}
          <Button onClick={handleCloseModal} sx={{ backgroundColor: "rgba(255,94,0,1)" }}>
            Cerrar
          </Button>
        </Box>
      </Modal>
    </Box>

    <Modal open={dateModalOpen} onClose={() => setDateModalOpen(false)}>
  <Box sx={{
      position: "absolute",
      top: "50%",
      left: "50%",
      transform: "translate(-50%, -50%)",
      width: 350,
      backgroundColor: "white",
      p: 3,
      borderRadius: 2,
      boxShadow: 24,
  }}>
      <Typography variant="h6" sx={{ mb: 2, color: "rgba(255,94,0,1)", fontWeight: "bold" }}>
          Filtrar por Fecha
      </Typography>

      <Box sx={{ display: "flex", flexDirection: "column", gap: 2 }}>
          <input type="date" value={initDate} onChange={(e) => setInitDate(e.target.value)} />
          <input type="date" value={endDate} onChange={(e) => setEndDate(e.target.value)} />

          <Button
              onClick={applyDateFilter}
              sx={{ backgroundColor: "rgba(255,94,0,1)",  // Color de fondo naranja
                          color: "#fff",  // Texto blanco
                          padding: "6px 12px",  // Ajusta el tamaño del botón
                          fontWeight: "bold",  // Establece el peso de la fuente
                          fontSize: "0.875rem",  // Ajusta el tamaño de la fuente
                          borderRadius: 2,  // Borde redondeado
                          "&:hover": {
                            backgroundColor: "rgba(255,94,0,0.8)",  // Color al pasar el mouse (opaco)
                          },}}
          >
              Aplicar filtro
          </Button>

          {isFiltered && (
            <Button
                onClick={removeFilter}
                sx={{ backgroundColor: "rgba(255,94,0,1)",  // Color de fondo naranja
                          color: "#fff",  // Texto blanco
                          padding: "6px 12px",  // Ajusta el tamaño del botón
                          fontWeight: "bold",  // Establece el peso de la fuente
                          fontSize: "0.875rem",  // Ajusta el tamaño de la fuente
                          borderRadius: 2,  // Borde redondeado
                          "&:hover": {
                            backgroundColor: "rgba(255,94,0,0.8)",  // Color al pasar el mouse (opaco)
                          },}}
            >
                Quitar filtro
            </Button>
          )}

          <Button
              onClick={() => setDateModalOpen(false)}
              sx={{ mt: 1 }}
          >
              Cancelar
          </Button>
      </Box>
  </Box>
</Modal>


    </ThemeProvider>
  );
};

export default Reports;
