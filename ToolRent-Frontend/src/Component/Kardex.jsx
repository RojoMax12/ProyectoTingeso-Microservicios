import { useState, useEffect } from 'react';
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
        {/* Capa de fondo fija que cubre toda la pantalla */}
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

        {/* Botón menú hamburguesa */}
        <IconButton
            color="inherit"
            onClick={() => setDrawerOpen(true)}
            sx={{ 
                position: "fixed", 
                top: 16, 
                left: 16, 
                zIndex: 1100, 
                backgroundColor: "#FA812F", 
                boxShadow: 3, 
                '&:hover': { backgroundColor: "#FA812F" }
            }}
        >
            <MenuIcon sx={{ color: "#FEF3E2" }} />
        </IconButton>

        {/* Barra lateral (Drawer) */}
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

        {/* Contenedor externo con scroll natural */}
        <Box sx={{
            backgroundColor: "transparent",
            py: 4,
            pt: 10, 
            display: "flex",
            justifyContent: "center",
            width: "100%"
        }}>
            {/* ESTE ES EL BOX QUE ENSANCHAMOS */}
            <Box
                sx={{
                    maxWidth: 1500, // <--- Aumentado para ensanchar el contenido
                    width: "95%",    // <--- Ocupa el 95% del ancho de la pantalla
                    mx: "auto",
                    backgroundColor: "#fff8f0", 
                    borderRadius: 4,
                    boxShadow: 6,
                    p: { xs: 2, md: 4 }, // Padding responsivo (menor en móviles)
                    border: "2px solid rgba(255, 94, 0, 0.2)",
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

                {/* Filtros */}
                <Stack
                    direction="row"
                    spacing={2}
                    sx={{ mb: 3, flexWrap: "wrap", alignItems: "center", gap: 2 }}
                >
                    <TextField
                        label="Fecha inicio"
                        type="date"
                        InputLabelProps={{ shrink: true }}
                        value={fechaInicio}
                        onChange={e => setFechaInicio(e.target.value)}
                        sx={{
                            minWidth: 200,
                            // Selector para el icono del calendario
                            "& input::-webkit-calendar-picker-indicator": {
                                filter: "invert(42%) sepia(93%) saturate(3065%) hue-rotate(354deg) brightness(101%) contrast(106%)",
                                cursor: "pointer",
                                fontSize: "1.2rem" // Opcional: ajustar tamaño
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
                            minWidth: 200,
                            // Selector para el icono del calendario
                            "& input::-webkit-calendar-picker-indicator": {
                                filter: "invert(42%) sepia(93%) saturate(3065%) hue-rotate(354deg) brightness(101%) contrast(106%)",
                                cursor: "pointer",
                                fontSize: "1.2rem" // Opcional: ajustar tamaño
                            }
                        }}
                    />
                    <Button
                        variant="contained"
                        onClick={handleBuscar}
                        sx={{ 
                            height: 56, // Misma altura que los TextFields
                            minWidth: 180,
                            backgroundColor: "rgba(255, 94, 0, 1)",
                            '&:hover': { backgroundColor: "rgba(255, 94, 0, 0.8)" }
                        }}
                    >
                        Buscar por rango
                    </Button>
                </Stack>
                
                <Stack spacing={2} direction="row" sx={{ mb: 4, flexWrap: "wrap", alignItems: "center", gap: 2 }}>
                    <TextField
                        label="Buscar por nombre de herramienta"
                        variant="outlined"
                        value={nombreHerramienta}
                        onChange={e => setNombreHerramienta(e.target.value)}
                        sx={{ flexGrow: 1, minWidth: 300 }} // Ocupa espacio disponible
                    />
                    <Button
                        variant="contained"
                        onClick={handleBuscarPorNombre}
                        sx={{ 
                            height: 56,
                            minWidth: 250,
                            backgroundColor: "rgba(255, 94, 0, 1)",
                            '&:hover': { backgroundColor: "rgba(255, 94, 0, 0.8)" }
                        }}
                    >
                        Buscar por nombre
                    </Button>
                </Stack>

                {/* Tabla con scroll interno */}
                <TableContainer 
                    component={Paper} 
                    sx={{ 
                        boxShadow: 3,
                        maxHeight: 600, // Aumenté un poco el alto para aprovechar el ancho
                        overflow: 'auto',
                        '&::-webkit-scrollbar': { width: '10px' },
                        '&::-webkit-scrollbar-track': { backgroundColor: '#f1f1f1', borderRadius: '6px' },
                        '&::-webkit-scrollbar-thumb': { 
                            backgroundColor: 'rgba(255, 94, 0, 0.6)', 
                            borderRadius: '6px',
                            '&:hover': { backgroundColor: 'rgba(255, 94, 0, 0.8)' }
                        }
                    }}
                >
                    <Table stickyHeader aria-label="kardex table">
                        <TableHead>
                            <TableRow>
                                {["ID", "Fecha", "Estado", "Usuario", "Cantidad", "Nombre de la Herramienta"].map((head) => (
                                    <TableCell 
                                        key={head}
                                        sx={{ 
                                            color: "#fff", 
                                            fontWeight: "bold",
                                            backgroundColor: "rgba(255, 94, 0, 1)",
                                            position: "sticky",
                                            top: 0,
                                            zIndex: 2,
                                            fontSize: '1rem'
                                        }}
                                    >
                                        {head}
                                    </TableCell>
                                ))}
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {kardexList.length === 0 ? (
                                <TableRow>
                                    <TableCell colSpan={6} align="center"> 
                                        <Typography color="text.secondary" sx={{ py: 6, fontStyle: 'italic' }}>
                                            No hay registros en el Kardex.
                                        </Typography>
                                    </TableCell>
                                </TableRow>
                            ) : (
                                kardexList.map((kardex) => (
                                    <TableRow 
                                        key={kardex.id}
                                        sx={{
                                            '&:nth-of-type(odd)': { backgroundColor: '#fafafa' },
                                            '&:hover': { backgroundColor: 'rgba(255, 94, 0, 0.05)' }
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