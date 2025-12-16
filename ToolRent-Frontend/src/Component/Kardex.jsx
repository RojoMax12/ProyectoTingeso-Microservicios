import React, { useState, useEffect } from 'react';
import {
    Box, Typography, Paper, Stack, IconButton, Drawer, List, ListItem, ListItemButton,
    ListItemIcon, ListItemText, Button, Table, TableBody, TableCell, TableContainer,
    TableHead, TableRow, TextField
} from "@mui/material";
import { useNavigate } from 'react-router-dom';
import HomeIcon from "@mui/icons-material/Home";
import BuildIcon from "@mui/icons-material/Build";
import MenuIcon from "@mui/icons-material/Menu";
import KardexServices from '../Services/KardexServices';
import ToolServices from '../Services/ToolServices';
import StateToolsServices from '../Services/StateToolsServices';
import { useKeycloak } from '@react-keycloak/web';
import LibraryAddIcon from "@mui/icons-material/LibraryAdd"; // 📚➕ Para Agregar Herramienta
import AssessmentIcon from "@mui/icons-material/Assessment"; // 📊 Para Ver Kardex (reporte
import ContactsIcon from "@mui/icons-material/Contacts";     // 📇 Para Clientes)
import PersonAddAltIcon from '@mui/icons-material/PersonAddAlt';
import AdminPanelSettingsIcon from "@mui/icons-material/AdminPanelSettings"; // 🛡️⚙️ Para Configuraciones (admin)
import ReportIcon from '@mui/icons-material/Report'; // 📈 Para Reportes


const Kardex = () => {
    const [kardexList, setKardexList] = useState([]);
    const [drawerOpen, setDrawerOpen] = useState(false);
    const [fechaInicio, setFechaInicio] = useState("");
    const [fechaFin, setFechaFin] = useState("");
    const [nombreHerramienta, setNombreHerramienta] = useState("");
    const [toolNames, setToolNames] = useState({});
    const [statetool, setStatetool] = useState("");
    const navigate = useNavigate();
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
  // Solo mostrar Configuraciones si es admin
  ...(isAdmin ? [{ text: "Configuraciones", icon: <AdminPanelSettingsIcon />, path: "/Configuration" }] : [])
];

    // Obtiene todos los nombres de herramientas usados en el kardex actual
    const fetchToolNames = (kardexData) => {
        const ids = [...new Set(kardexData.map(k => k.idtool))].filter(id => id != null);
        Promise.all(ids.map(id =>
            ToolServices.getid(id)
                .then(res => ({ id, name: res.data.name }))
                .catch(() => ({ id, name: "Desconocido" }))
        )).then(results => {
            const namesObj = {};
            results.forEach(({ id, name }) => {
                namesObj[id] = name;
            });
            setToolNames(namesObj);
        });
    };

    const fetchStateTool = (kardexData) => {
        const stateIds = [...new Set(kardexData.map(k => k.stateToolsId))].filter(id => id != null);
        Promise.all(stateIds.map(id =>
            StateToolsServices.getid(id)
                .then(res => ({ id, name: res.data.name }))
                .catch(() => ({ id, name: "Desconocido" }))
        )).then(results => {
            const namesObj = {};
            results.forEach(({ id, name }) => {
                namesObj[id] = name;
            });
            setStatetool(namesObj);
        });
    };

    useEffect(() => {
        KardexServices.getAll()
            .then(response => {
                setKardexList(response.data);
                fetchToolNames(response.data);
                fetchStateTool(response.data);
            })
            .catch(error => {
                console.error("Error fetching Kardex data:", error);
            });
    }, []);


    const handleBuscar = () => {
        if (fechaInicio && fechaFin) {
            KardexServices.kardexByDateRange(fechaInicio, fechaFin)
                .then(response => {
                    setKardexList(response.data);
                    fetchToolNames(response.data);
                })
                .catch(error => {
                    console.error("Error buscando kardex por rango:", error);
                });
        } else {
            KardexServices.getAll()
                .then(response => {
                    setKardexList(response.data);
                    fetchToolNames(response.data);
                })
                .catch(error => {
                    console.error("Error fetching Kardex data:", error);
                });
        }
    };

    const handleBuscarPorNombre = () => {
        if (nombreHerramienta) {
            KardexServices.kardexBytoolname(nombreHerramienta)
                .then(response => {
                    setKardexList(response.data);
                    fetchToolNames(response.data);
                })
                .catch(error => {
                    console.error("Error buscando kardex por nombre:", error);
                });
        } else {
            KardexServices.getAll()
                .then(response => {
                    setKardexList(response.data);
                    fetchToolNames(response.data);
                })
                .catch(error => {
                    console.error("Error fetching Kardex data:", error);
                });
        }
    };


return (
    <>  
        {/* Slidebar */}
        <IconButton
            color="inherit"
            onClick={() => setDrawerOpen(true)}
            sx={{ position: "fixed", top: 16, left: 16, zIndex: 10, backgroundColor: "#FA812F", boxShadow: 3 , '&:hover': { backgroundColor: "#FA812F" }}}
        >
            <MenuIcon />
        </IconButton>

        <Drawer
            anchor="left"
            open={drawerOpen}
            onClose={() => setDrawerOpen(false)}
            variant="temporary"
            sx={{
                [`& .MuiDrawer-paper`]: { width: 220, boxSizing: "border-box", backgroundColor: "#FEF3E2" }
            }}
        >
            <List>
                {sidebarOptions.map((option, index) => (
                    <ListItem key={index} disablePadding>
                        <ListItemButton onClick={() => { navigate(option.path); setDrawerOpen(false); }}>
                            <ListItemIcon sx={{ color: "#FA812F" }}>{option.icon}</ListItemIcon>
                            <ListItemText primary={option.text} />
                        </ListItemButton>
                    </ListItem>
                ))}
            </List>
        </Drawer>
        <Box
            sx={{
                position: "fixed",
                top: 0,
                left: 0,
                width: "100vw",
                height: "100vh",
                backgroundColor: "#FEF3E2",
                zIndex: -1
            }}
        />

        <Box sx={{
            minHeight: "100vh",
            backgroundColor: "#FEF3E2",
            py: 4,
            display: "flex",
            alignItems: "flex-start", // <-- Cambio: de center a flex-start
            pt: 8 // <-- Agregar padding top para el botón del menú
        }}>
            <Box
                sx={{
                    maxWidth: 1100,
                    mx: "auto",
                    mt: 2,
                    backgroundColor: "#ffffffff",
                    borderRadius: 4,
                    boxShadow: 6,
                    p: 4,
                    border: "2px solid rgba(255, 94, 0, 0.2)",
                    width: "100%" // <-- Agregar para mejor responsividad
                }}
            >
                <Typography
                    variant="h4"
                    align="center"
                    gutterBottom
                    sx={{ fontWeight: "bold", mb: 4, color: "rgba(255, 94, 0, 1)" }}
                >
                    Kardex
                </Typography>

                {/* Filtros alineados y ordenados */}
                <Stack
                    direction="row"
                    spacing={2}
                    sx={{
                        mb: 3,
                        flexWrap: "wrap",
                        alignItems: "center",
                        justifyContent: "flex-start"
                    }}
                >
                    <TextField
                        label="Fecha inicio"
                        type="date"
                        InputLabelProps={{ shrink: true }}
                        value={fechaInicio}
                        onChange={e => setFechaInicio(e.target.value)}
                        sx={{ 
                            minWidth: 160,
                            '& input[type="date"]::-webkit-calendar-picker-indicator': {
                                filter: 'invert(45%) sepia(87%) saturate(2466%) hue-rotate(15deg) brightness(102%) contrast(104%)',
                                cursor: 'pointer'
                            }
                        }}
                    />
                    <TextField
                        label="Fecha fin"
                        type="date"
                        InputLabelProps={{ shrink: true }}
                        value={fechaFin}
                        onChange={e => setFechaFin(e.target.value)}
                        sx={{ 
                            minWidth: 160,
                            '& input[type="date"]::-webkit-calendar-picker-indicator': {
                                filter: 'invert(45%) sepia(87%) saturate(2466%) hue-rotate(15deg) brightness(102%) contrast(104%)',
                                cursor: 'pointer'
                            }
                        }}
                    />
                    <Button
                        variant="contained"
                        color="primary"
                        onClick={handleBuscar}
                        sx={{ 
                            minWidth: 180,
                            backgroundColor: "rgba(255, 94, 0, 1)",
                            '&:hover': {
                                backgroundColor: "rgba(255, 94, 0, 0.8)"
                            }
                        }}
                    >
                        Buscar por rango
                    </Button>
                </Stack>
                
                <Stack spacing={2} direction="row" sx={{
                        mb: 3,
                        flexWrap: "wrap",
                        alignItems: "center",
                        justifyContent: "flex-start"
                    }}>
                    <TextField
                        label="Buscar por nombre de herramienta"
                        variant="outlined"
                        value={nombreHerramienta}
                        onChange={e => setNombreHerramienta(e.target.value)}
                        sx={{ minWidth: 220 }}
                    />
                    <Button
                        variant="contained"
                        color="primary"
                        onClick={handleBuscarPorNombre}
                        sx={{ 
                            minWidth: 220,
                            backgroundColor: "rgba(255, 94, 0, 1)",
                            '&:hover': {
                                backgroundColor: "rgba(255, 94, 0, 0.8)"
                            }
                        }}
                    >
                        Buscar por nombre de herramienta
                    </Button>
                </Stack>

                {/* TableContainer con scroll personalizado */}
                <TableContainer 
                    component={Paper} 
                    sx={{ 
                        boxShadow: 3,
                        maxHeight: 500, // <-- Altura máxima para activar scroll
                        overflow: 'auto', // <-- Scroll automático
                        '&::-webkit-scrollbar': { // <-- Personalización del scrollbar
                            width: '10px',
                            height: '10px', // Para scroll horizontal si es necesario
                        },
                        '&::-webkit-scrollbar-track': {
                            backgroundColor: '#f1f1f1',
                            borderRadius: '6px',
                        },
                        '&::-webkit-scrollbar-thumb': {
                            backgroundColor: 'rgba(255, 94, 0, 0.6)', // <-- Color naranja matching tu tema
                            borderRadius: '6px',
                            '&:hover': {
                                backgroundColor: 'rgba(255, 94, 0, 0.8)',
                            }
                        },
                        '&::-webkit-scrollbar-corner': {
                            backgroundColor: '#f1f1f1',
                        }
                    }}
                >
                    <Table stickyHeader>
                        <TableHead>
                            <TableRow>
                                <TableCell sx={{ 
                                    color: "#fff", 
                                    fontWeight: "bold",
                                    backgroundColor: "rgba(255, 94, 0, 1)", // <-- Mantener color al hacer scroll
                                    position: "sticky",
                                    top: 0,
                                    zIndex: 2
                                }}>
                                    ID</TableCell>

                                <TableCell sx={{ 
                                    color: "#fff", 
                                    fontWeight: "bold",
                                    backgroundColor: "rgba(255, 94, 0, 1)",
                                    position: "sticky",
                                    top: 0,
                                    zIndex: 2
                                }}>
                                    Fecha</TableCell>
                                <TableCell sx={{ 
                                    color: "#fff", 
                                    fontWeight: "bold",
                                    backgroundColor: "rgba(255, 94, 0, 1)",
                                    position: "sticky",
                                    top: 0,
                                    zIndex: 2
                                }}
                                >Estado de la herramienta</TableCell>
                                <TableCell sx={{ 
                                    color: "#fff", 
                                    fontWeight: "bold",
                                    backgroundColor: "rgba(255, 94, 0, 1)",
                                    position: "sticky",
                                    top: 0,
                                    zIndex: 2
                                }}>
                                    Usuario</TableCell>
                                <TableCell sx={{ 
                                    color: "#fff", 
                                    fontWeight: "bold",
                                    backgroundColor: "rgba(255, 94, 0, 1)",
                                    position: "sticky",
                                    top: 0,
                                    zIndex: 2
                                }}>
                                    Cantidad</TableCell>
                                <TableCell sx={{ 
                                    color: "#fff", 
                                    fontWeight: "bold",
                                    backgroundColor: "rgba(255, 94, 0, 1)",
                                    position: "sticky",
                                    top: 0,
                                    zIndex: 2
                                }}>
                                    Nombre de la Herramienta</TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {kardexList.length === 0 ? (
                                <TableRow>
                                    <TableCell colSpan={6} align="center"> 
                                        <Typography 
                                            color="text.secondary"
                                            sx={{ py: 4, fontStyle: 'italic' }}
                                        >
                                            No hay registros en el Kardex.
                                        </Typography>
                                    </TableCell>
                                </TableRow>
                            ) : (
                                kardexList.map((kardex, index) => (
                                    <TableRow 
                                        key={kardex.id}
                                        sx={{
                                            '&:nth-of-type(odd)': { // <-- Filas alternas para mejor legibilidad
                                                backgroundColor: '#fafafa',
                                            },
                                            '&:hover': { // <-- Efecto hover
                                                backgroundColor: 'rgba(255, 94, 0, 0.1)',
                                            }
                                        }}
                                    >
                                        <TableCell>{kardex.id}</TableCell>
                                        <TableCell>{kardex.date}</TableCell>
                                        <TableCell>{statetool[kardex.stateToolsId] || "Cargando..."}</TableCell>
                                        <TableCell>{kardex.username}</TableCell>
                                        <TableCell>{kardex.quantity}</TableCell>
                                        <TableCell>{toolNames[kardex.idtool] || "Cargando..."}</TableCell>
                                    </TableRow>
                                ))
                            )}
                        </TableBody>
                    </Table>
                </TableContainer>
            </Box>
        </Box>
    </>
);
};

export default Kardex;