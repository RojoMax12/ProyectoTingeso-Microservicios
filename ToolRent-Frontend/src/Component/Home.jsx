import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import {
  ThemeProvider, CssBaseline, AppBar, Toolbar, Typography, Stack, Avatar, Button,
  Container, Drawer, List, ListItem, ListItemButton, ListItemIcon, ListItemText,
  TextField, IconButton, Box, Paper, Modal
} from "@mui/material";
import HomeIcon from "@mui/icons-material/Home";
import BuildIcon from "@mui/icons-material/Build";
import MenuIcon from "@mui/icons-material/Menu";
import LibraryAddIcon from "@mui/icons-material/LibraryAdd";
import AssessmentIcon from "@mui/icons-material/Assessment";
import ContactsIcon from "@mui/icons-material/Contacts";
import PersonAddAltIcon from '@mui/icons-material/PersonAddAlt';
import AdminPanelSettingsIcon from "@mui/icons-material/AdminPanelSettings";
import { createTheme } from "@mui/material/styles";
import { useKeycloak } from "@react-keycloak/web";
import ClientServices from "../Services/ClientServices";
import StateUserServices from "../Services/StateUsersServices";
import LoanToolServices from "../Services/LoanToolsServices";
import ToolServices from "../Services/ToolServices";
import KardexServices from "../Services/KardexServices";
import ReportIcon from '@mui/icons-material/Report';

const theme = createTheme({
  palette: {
    background: {
      default: "#FEF3E2",
    },
  },
});

const Home = () => {
  const navigate = useNavigate();
  const [rut, setRut] = useState("");
  const [drawerOpen, setDrawerOpen] = useState(false);
  const photo = "";
  const { keycloak } = useKeycloak();
  const name = keycloak.tokenParsed?.name || "Usuario";
  const [prestadas, setPrestadas] = useState([]);
  const [clientData, setClientData] = useState({});
  const [estadoCuenta, setEstadoCuenta] = useState("");
  const [toolDetails, setToolDetails] = useState({});
  const isAdmin = keycloak?.tokenParsed?.realm_access?.roles?.includes("ADMIN");
  const [openModal, setOpenModal] = useState(false);
  const [selectedLoan, setSelectedLoan] = useState(null);
  const [lateFeeloan, setLateFeeLoan] = useState(0);
  const [lateFeeLoading, setLateFeeLoading] = useState(false);
  const [damageFee, setDamageFee] = useState(0);
  // NUEVO ESTADO: Para controlar si ya se pagó
  const [isPaid, setIsPaid] = useState(false);

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

  useEffect(() => {
    if (clientData.state) {
      const result = StateUserServices.getid(clientData.state);
      if (result && typeof result.then === "function") {
        result.then(res => {
          setEstadoCuenta(res?.data?.name || "");
        });
      } else if (result?.data?.name) {
        setEstadoCuenta(result.data.name);
      } else {
        setEstadoCuenta(result || "");
      }
    } else {
      setEstadoCuenta("");
    }
  }, [clientData.state]);

  const handleToolrent = () => {
    navigate(`/LoanTool/${clientData.id}`);
  };

  const toolrentWithId = (id) => {
    if (!id) {
      console.warn("No hay ID de cliente para buscar herramientas prestadas.");
      setPrestadas([]);
      setToolDetails({});
      return;
    }
    LoanToolServices.getiduser(id)
      .then(response => {
        const promises = response.data.map(loanTool =>
          ToolServices.getid(loanTool.toolid)
            .then(res => ({
              ...loanTool,
              toolState: res.data.states,
              toolName: res.data.name,
              toolCategory: res.data.category
            }))
            .catch(() => ({
              ...loanTool,
              toolState: null,
              toolName: "",
              toolCategory: ""
            }))
        );

        Promise.all(promises).then(loanToolsWithState => {
          const prestadasFiltradas = loanToolsWithState.filter(
            lt => lt.toolState !== 1 && lt.toolState !== null
          );
          setPrestadas(prestadasFiltradas);
          console.log("Herramientas prestadas filtradas:", prestadasFiltradas);

          const detailsObj = {};
          prestadasFiltradas.forEach(lt => {
            detailsObj[lt.toolid] = {
              name: lt.toolName,
              states: lt.toolState,
              category: lt.toolCategory
            };
          });
          setToolDetails(detailsObj);
        });
      })
      .catch(error => {
        setPrestadas([]);
        setToolDetails({});
        console.error("Error obteniendo herramientas en préstamo:", error);
      });
  };

  const searchClient = () => {
    ClientServices.getByRut(rut)
      .then(response => {
        console.log("Respuesta de búsqueda:", response);
        if (!response.data || !response.data.id) {
          alert("Cliente no encontrado o sin ID.");
          setClientData({});
          setPrestadas([]);
          setToolDetails({});
          return;
        }
        setClientData(response.data);
        console.log("Cliente encontrado:", response.data);

        LoanToolServices.checkClients(response.data.id)
          .then(res => {
            console.log("Resultado checkClients:", res.data);
            toolrentWithId(response.data.id);
          })
          .catch(err => {
            console.error("Error verificando estado del cliente:", err);
            toolrentWithId(response.data.id);
          });
      })
      .catch(error => {
        setClientData({});
        setPrestadas([]);
        setToolDetails({});
        console.error("Error buscando cliente:", error);
      });
  };

  const handleLogout = () => {
    alert("Sesión cerrada");
    keycloak.logout();
  };

       const formatRut = (value) => {
        if (!value) return "";
        
        // Remover caracteres no numéricos excepto 'k' o 'K'
        let cleaned = value.replace(/[^0-9kK]/g, '');
        
        // Si es muy corto, devolver tal como está
        if (cleaned.length <= 1) return cleaned;
        
        // Separar cuerpo y dígito verificador
        const body = cleaned.slice(0, -1);
        const dv = cleaned.slice(-1);
        
        // Formatear según la longitud del cuerpo
        let formattedBody = body;
        
        if (body.length >= 6) {
            // Aplicar formato con puntos desde atrás hacia adelante
            const reversedBody = body.split('').reverse().join('');
            const formattedReversed = reversedBody.replace(/(\d{3})(?=\d)/g, '$1.');
            formattedBody = formattedReversed.split('').reverse().join('');
        }
        
        return formattedBody + (dv ? `-${dv}` : '');
    };

  const handleOpenModal = (loanTool) => {
    setSelectedLoan(loanTool);
    TakeDamageLoan(loanTool.id);
    setOpenModal(true);
    setLateFeeLoading(true);
    setIsPaid(false); // RESET: Al abrir modal, resetear estado de pago
    calculateLateFeeForLoan(loanTool.id);
  };

  const handleCloseModal = () => {
    setOpenModal(false);
    setSelectedLoan(null);
    setDamageFee(0);
    setLateFeeLoan(0);
    setLateFeeLoading(false);
    setIsPaid(false); // RESET: Al cerrar modal, resetear estado de pago
  };

  const calculateLateFeeForLoan = (loanId) => {
    LoanToolServices.calculateLateFee(loanId)
      .then(response => {
        setLateFeeLoan(response.data || 0);
        setLateFeeLoading(false);
        console.log("Late fee calculated:", response.data);
      })
      .catch(error => {
        setLateFeeLoan(0);
        setLateFeeLoading(false);
        console.error("Error calculating late fee:", error);
      });
  };

  const returntool = (iduser, idtool) => {
    if (!iduser || !idtool) {
      alert("Faltan datos para devolver la herramienta.");
      return;
    }
    console.log("Devolviendo herramienta:", { iduser, idtool });
    LoanToolServices.updateTool(iduser, idtool)
      .then(response => {
        alert("Herramienta devuelta con éxito");
        const emailCliente = clientData.email || "";
        KardexServices.create({
          idtool: idtool,
          username: emailCliente,
          date: new Date(),
          stateToolsId: 1,
          quantity: 1
        });
        toolrentWithId(iduser);
      })
      .catch(error => {
        console.error("Error al devolver herramienta:", error);
      });
  };

  const repairtool = (idtool) => {
    if (!idtool) {
      alert("Faltan datos para reparar la herramienta.");
      return;
    }
    console.log("Reparando herramienta:", { idtool });
    ToolServices.repairtool(idtool)
      .then(response => {
        alert("Herramienta enviada a reparación con éxito");
        const emailCliente = clientData.email || "";
        KardexServices.create({
          idtool: idtool,
          username: emailCliente,
          date: new Date(),
          stateToolsId: 3,
          quantity: 1
        });
        toolrentWithId(clientData.id);
      })
      .catch(error => {
        console.error("Error al reparar herramienta:", error);
      });
  };

  const payAllFees = (loanId) => {
    console.log("ID del préstamo para pagar:", loanId);
    
    if (!loanId || typeof loanId !== 'number') {
      alert("ID de préstamo no válido");
      console.error("ID inválido:", loanId);
      return;
    }
    
    LoanToolServices.payAllFees(loanId)
      .then(response => {
        alert("Pago registrado exitosamente. Todos los cargos han sido cancelados.");
        console.log("Pago registrado:", response.data);
        setIsPaid(true); // ACTIVAR: Marcar como pagado para habilitar botón "Devolver"
        toolrentWithId(clientData.id);
      })
      .catch(error => {
        console.error("Error al pagar multas:", error);
        alert("Error al procesar el pago");
      });
  };

  // CALCULAR: Total a pagar
  const calculateTotal = () => {
    if (!selectedLoan || lateFeeLoading) return 0;
    return (
      (parseFloat(selectedLoan.rentalFee) || 0) + 
      (parseFloat(lateFeeloan) || 0) +
      (parseFloat(selectedLoan.damageFee) || 0) +
      (parseFloat(selectedLoan.repositionFee) || 0)
    );
  };

  const TakeDamageLoan = (idloan) => {
    LoanToolServices.registerDamageandReposition(idloan)
  };

    

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      
      {/* HEADER */}
      <AppBar position="fixed" sx={{ backgroundColor: "#FA812F", left: 0, width: "100%" }}>
        <Toolbar sx={{ display: "flex", justifyContent: "space-between" }}>
          <IconButton color="inherit" edge="start" onClick={() => setDrawerOpen(true)} sx={{ mr: 2 }}>
            <MenuIcon />
          </IconButton>
          <Typography variant="h4" sx={{ fontWeight: "bold" }}>
            Tool Rent
          </Typography>
          <Stack direction="row" spacing={2} alignItems="center">
            <Avatar src={photo} alt="Usuario"  />
            <Typography variant="subtitle1">{name}</Typography>
            <Button
              variant="contained"
              onClick={handleLogout}
              sx={{
                backgroundColor: "#FEF3E2",
                color: "#000",
                "&:hover": { backgroundColor: "#b49e99" }
              }}
            >
              Cerrar sesión
            </Button>
          </Stack>
        </Toolbar>
      </AppBar>

      {/* SIDEBAR */}
      <Drawer
        open={drawerOpen}
        onClose={() => setDrawerOpen(false)}
        variant="temporary"
        sx={{
          [`& .MuiDrawer-paper`]: { width: 240, boxSizing: "border-box", backgroundColor: "#FEF3E2" }
        }}
      >
        <List>
          {sidebarOptions.map((option) => (
            <ListItem key={option.text} disablePadding>
              <ListItemButton 
                onClick={() => { navigate(option.path); setDrawerOpen(false); }}
                sx={{
                  '&:hover': {
                    backgroundColor: 'rgba(255, 94, 0, 0.1)',
                  },
                  borderRadius: 1,
                  mx: 1,
                  my: 0.5
                }}
              >
                <ListItemIcon sx={{ color: 'rgba(255, 94, 0, 1)', minWidth: 40 }}>
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

      {/* MAIN CONTENT */}
      <Container sx={{ mt: 12, display: "flex", justifyContent: "center", alignItems: "flex-start", minHeight: "80vh" }}>
        <Stack direction="row" spacing={6} sx={{ width: "100%", maxWidth: 1200 }}>
          
          {/* Panel Herramientas Prestadas */}
          <Stack spacing={2} sx={{ width: "100%" }}>
            <Typography variant="h5" align="center" sx={{ fontWeight: "bold", color: "rgba(255, 94, 0, 1)" }}>
              Herramientas Prestadas
            </Typography>
            <Paper sx={{ p: 3, boxShadow: 2, minHeight: 300, display: "flex", alignItems: "center", justifyContent: "center", width: "100%" , color: "rgba(0, 0, 0, 0.7)", backgroundColor: "#ffffff31", border: "2px solid rgba(255, 94, 0, 0.2)", borderRadius: 2 }}>
              {prestadas.length === 0 ? (
                <Typography align="center" color="text.secondary" sx={{ width: "100%", fontSize: 18 }}>
                  No hay herramientas prestadas.
                </Typography>
              ) : (
                <Stack spacing={2} sx={{ width: "150%" }}>
                  {prestadas.map((loanTool) => (
                    <Box key={loanTool.id} sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", backgroundColor: "#ffffffff", p: 2, border: "1px solid rgba(0, 0, 0, 0.2)", borderRadius: 2 }}>
                      <Box>
                        <Typography variant="subtitle1" sx={{ fontWeight: "bold" }}>
                          {loanTool.toolid && toolDetails[loanTool.toolid]?.name && `${toolDetails[loanTool.toolid].name}`}
                        </Typography>
                        <Typography variant="body2" color="text.secondary">Fecha préstamo: {loanTool.initiallenddate}</Typography>
                        <Typography variant="body2" color="text.secondary">Fecha devolución: {loanTool.finalreturndate}</Typography>
                      </Box>
                      <Button
                        variant="contained"
                        color="primary"
                        size="small"
                        onClick={() => handleOpenModal(loanTool)}
                        sx={{ backgroundColor: "rgba(255, 94, 0, 1)", '&:hover': { backgroundColor: "rgba(255, 94, 0, 0.8)" } }}
                      >
                        Detalles
                      </Button>
                    </Box>
                  ))}
                </Stack>
              )}
            </Paper>
          </Stack>

          {/* Panel Información del Cliente */}
          <Stack spacing={3} sx={{ width: "110%" }}>
            <Typography variant="h5" align="center" sx={{ fontWeight: "bold", color: "rgba(255, 94, 0, 1)" }}>
              Información del Cliente
            </Typography>
            
            <Paper sx={{ p: 3, boxShadow: 3, backgroundColor: "#fff8f0", border: "2px solid rgba(255, 94, 0, 0.2)", borderRadius: 2 }}>
              {/* Búsqueda por RUT */}
              <Box sx={{ mb: 3 }}>
                <TextField
                  label="RUT"
                  variant="outlined"
                  value={rut}
                  onChange={(e) => { setRut(formatRut(e.target.value)); }}
                  fullWidth
                  placeholder="Ej: 12.345.678-9"
                  sx={{ mb: 2 }}
                />
                <Button 
                  variant="contained" 
                  color="primary" 
                  fullWidth 
                  onClick={searchClient}
                  sx={{ 
                    backgroundColor: "rgba(255, 94, 0, 1)",
                    '&:hover': { backgroundColor: "rgba(255, 94, 0, 0.8)" }
                  }}
                >
                  Buscar Cliente
                </Button>
              </Box>

              {/* Datos del cliente */}
              {clientData.name && (
                <>
                  <Box sx={{ borderTop: '1px solid rgba(255, 94, 0, 0.3)', mb: 3, pt: 3 }}>
                    <Typography variant="h6" sx={{ fontWeight: "bold", color: "rgba(255, 94, 0, 1)", mb: 2 }}>
                      Datos del Cliente
                    </Typography>
                  </Box>

                  <Stack spacing={2}>
                    <Stack direction="row" spacing={2}>
                      <TextField
                        label="Nombre del Cliente"
                        variant="outlined"
                        value={clientData.name || ""}
                        fullWidth
                        InputProps={{ readOnly: true }}
                        sx={{ '& .MuiInputBase-input': { backgroundColor: '#f9f9f9', fontWeight: 500 } }}
                      />
                      <TextField
                        label="Correo Electrónico"
                        variant="outlined"
                        value={clientData.email || ""}
                        fullWidth
                        InputProps={{ readOnly: true }}
                        sx={{ '& .MuiInputBase-input': { backgroundColor: '#f9f9f9', fontWeight: 500 } }}
                      />
                    </Stack>

                    <Stack direction="row" spacing={2}>
                      <TextField
                        label="Número de Teléfono"
                        variant="outlined"
                        value={clientData.phone || ""}
                        fullWidth
                        InputProps={{ readOnly: true }}
                        sx={{ '& .MuiInputBase-input': { backgroundColor: '#f9f9f9', fontWeight: 500 } }}
                      />
                      <TextField
                        label="Estado de Cuenta"
                        variant="outlined"
                        value={estadoCuenta || ""}
                        fullWidth
                        InputProps={{ readOnly: true }}
                        sx={{
                          '& .MuiInputBase-input': {
                            backgroundColor: estadoCuenta === "Active" ? '#e8f5e8' : '#ffeee8',
                            fontWeight: 'bold',
                            color: estadoCuenta === "Active" ? '#2e7d32' : '#d32f2f'
                          }
                        }}
                      />
                    </Stack>

                    <Box sx={{ mt: 3 }}>
                      <Button 
                        variant="contained" 
                        color="primary" 
                        fullWidth 
                        onClick={handleToolrent} 
                        disabled={estadoCuenta !== "Active"}
                        sx={{ 
                          backgroundColor: estadoCuenta === "Active" ? "rgba(255, 94, 0, 1)" : "rgba(200, 200, 200, 1)",
                          '&:hover': {
                            backgroundColor: estadoCuenta === "Active" ? "rgba(255, 94, 0, 0.8)" : "rgba(180, 180, 180, 1)"
                          },
                          py: 1.5,
                          fontSize: '1.1rem',
                          fontWeight: 'bold'
                        }}
                      >
                        {estadoCuenta === "Active" ? "Prestar Herramienta" : "Cliente Inactivo - No Disponible"}
                      </Button>
                    </Box>

                    {estadoCuenta && (
                      <Box sx={{ mt: 2, p: 2, borderRadius: 1, backgroundColor: estadoCuenta === "Active" ? '#e8f5e8' : '#ffebee' }}>
                        <Typography 
                          variant="body2" 
                          align="center" 
                          sx={{ 
                            fontWeight: 'bold',
                            color: estadoCuenta === "Active" ? '#2e7d32' : '#d32f2f'
                          }}
                        >
                          {estadoCuenta === "Active" 
                            ? "✅ Cliente activo - Puede solicitar préstamos" 
                            : "❌ Cliente inactivo - No puede solicitar préstamos"
                          }
                        </Typography>
                      </Box>
                    )}
                  </Stack>
                </>
              )}

              {!clientData.name && (
                <Box sx={{ mt: 2, p: 3, textAlign: 'center' }}>
                  <Typography variant="body1" color="text.secondary" sx={{ fontStyle: 'italic' }}>
                    Ingrese un RUT y haga clic en "Buscar Cliente" para ver la información
                  </Typography>
                </Box>
              )}
            </Paper>
          </Stack>
        </Stack>
      </Container>

      {/* MODAL */}
      <Modal open={openModal} onClose={handleCloseModal}>
        <Box sx={{
          position: 'absolute',
          top: '50%',
          left: '50%',
          transform: 'translate(-50%, -50%)',
          bgcolor: 'background.paper',
          boxShadow: 24,
          p: 4,
          borderRadius: 2,
          minWidth: 500,
          maxWidth: 700
        }}>
          <Typography variant="h6" 
          sx={{ mb: 2, fontWeight: "bold", color: "rgba(255, 94, 0, 1)", textAlign: "center" }}>
            Detalles del Préstamo
            </Typography>
          {selectedLoan && (
            console.log("Selected Loan in Modal:", selectedLoan),
            <>
              <Stack direction="row" spacing={2} sx={{ mb: 2, alignItems: "center" }}>
                <TextField
                  label="Herramienta"
                  value={selectedLoan.toolName || "No disponible"}
                  fullWidth
                  margin="normal"
                  InputProps={{ readOnly: true }}
                />
                <TextField
                  label="Categoría"
                  value={selectedLoan.toolCategory || "No disponible"}
                  fullWidth
                  margin="normal"
                  InputProps={{ readOnly: true }}
                />
              </Stack>

              <Stack direction="row" spacing={2} sx={{ mb: 2, alignItems: "center" }}>
                <TextField
                  label="Fecha Préstamo"
                  value={selectedLoan.initiallenddate || "No disponible"}
                  fullWidth
                  margin="normal"
                  InputProps={{ readOnly: true }}
                />
                <TextField
                  label="Fecha Devolución"
                  value={selectedLoan.finalreturndate || "No disponible"}
                  fullWidth
                  margin="normal"
                  InputProps={{ readOnly: true }}
                />
              </Stack>

              <Stack direction="row" spacing={2} sx={{ mb: 2, alignItems: "center" }}>
                <TextField
                  label="Cargo por Atraso"
                  value={lateFeeLoading ? "Cargando..." : `$${lateFeeloan || 0}`}
                  fullWidth
                  margin="normal"
                  InputProps={{ readOnly: true }}
                />
                <TextField
                  label="Cargo por Préstamo"
                  value={`$${selectedLoan.rentalFee || 0}`}
                  fullWidth
                  margin="normal"
                  InputProps={{ readOnly: true }}
                />
              </Stack>

              <Stack direction="row" spacing={2} sx={{ mb: 2, alignItems: "center" }}>
                <TextField
                  label="Cargo por Daño"
                  value={`$${selectedLoan.damageFee || 0}`}
                  fullWidth
                  margin="normal"
                  InputProps={{ readOnly: true }}
                />
                <TextField
                  label="Cargo por Reparación"
                  value={`$${selectedLoan.repositionFee || 0}`}
                  fullWidth
                  margin="normal"
                  InputProps={{ readOnly: true }}
                />
                
              </Stack>

              <TextField
                label="Total a Pagar"
                value={
                  lateFeeLoading 
                    ? "Cargando..." 
                    : `$${calculateTotal()}`
                }
                fullWidth
                margin="normal"
                sx={{ 
                  '& .MuiInputBase-input': { 
                    fontWeight: 'bold', 
                    fontSize: '1.1rem',
                    color: isPaid ? '#4caf50' : 'primary.main',
                    backgroundColor: isPaid ? '#e8f5e8' : 'transparent'
                  } 
                }}
                InputProps={{ readOnly: true }}
              />

              {/* INDICADOR: Mostrar estado de pago */}
              {isPaid && (
                <Box sx={{ 
                  mt: 2, 
                  p: 2, 
                  backgroundColor: '#e8f5e8', 
                  borderRadius: 1,
                  border: '2px solid #4caf50'
                }}>
                  <Typography 
                    variant="body1" 
                    align="center" 
                    sx={{ 
                      fontWeight: 'bold',
                      color: '#2e7d32'
                    }}
                  >
                    ✅ Pago realizado exitosamente - Ahora puede devolver la herramienta
                  </Typography>
                </Box>
              )}
            </>
          )}
          
          {/* Botones en una sola fila */}
          <Stack direction="row" spacing={1} sx={{ mt: 3, justifyContent: "flex-end" }}>
            <Button 
              variant="outlined" 
              onClick={handleCloseModal}
            >
              Cerrar
            </Button>
            
            {/* MODIFICADO: Botón Devolver solo habilitado si se pagó */}
            <Button 
              variant="contained" 
              color="success"
              disabled={!isPaid} // CONDICIÓN: Solo habilitado si se pagó
              onClick={() => {
                console.log("Devolver clicked", clientData.id, selectedLoan?.toolid);
                returntool(clientData.id, selectedLoan.toolid);
                handleCloseModal();
              }}
              sx={{
                backgroundColor: isPaid ? '#4caf50' : '#bdbdbd',
                '&:hover': {
                  backgroundColor: isPaid ? '#45a049' : '#bdbdbd'
                },
                '&:disabled': {
                  backgroundColor: '#bdbdbd',
                  color: '#757575'
                }
              }}
            >
              {isPaid ? "✅ Devolver" : "🔒 Debe Pagar Primero"}
            </Button>
            
            <Button 
              variant="contained" 
              color="warning"
              onClick={() => {
                console.log("Reparar clicked", selectedLoan?.toolid);
                repairtool(selectedLoan.toolid);
                handleCloseModal();
              }}
            >
              🔧 Reparar
            </Button>
            
            {/* MODIFICADO: Botón Pagar deshabilitado si ya se pagó */}
            <Button 
              variant="contained" 
              color="primary"
              disabled={isPaid || calculateTotal() === 0} // CONDICIÓN: Disabled si ya se pagó o si no hay monto
              onClick={() => {
                console.log("Pagar clicked", selectedLoan?.id);
                payAllFees(selectedLoan?.id);
              }}
              sx={{
                backgroundColor: isPaid ? '#bdbdbd' : (calculateTotal() === 0 ? '#bdbdbd' : '#1976d2'),
                '&:hover': {
                  backgroundColor: isPaid ? '#bdbdbd' : (calculateTotal() === 0 ? '#bdbdbd' : '#1565c0')
                },
                '&:disabled': {
                  backgroundColor: '#bdbdbd',
                  color: '#757575'
                }
              }}
            >
              {isPaid ? "✅ Ya Pagado" : (calculateTotal() === 0 ? "Sin Cargos" : "💳 Pagar")}
            </Button>
          </Stack>
        </Box>
      </Modal>
    </ThemeProvider>
  );
};

export default Home;