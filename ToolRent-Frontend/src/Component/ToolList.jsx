import { useState, useEffect } from "react";
import { TableContainer, Paper, Table, TableHead, TableRow, TableCell, TableBody, Button, Typography, Box, Drawer, List, ListItem, ListItemButton, ListItemIcon, ListItemText, IconButton, Chip, Collapse, Accordion, AccordionSummary, AccordionDetails } from "@mui/material";
import ToolServices from "../Services/ToolServices";
import { useNavigate } from "react-router";
import HomeIcon from "@mui/icons-material/Home";
import BuildIcon from "@mui/icons-material/Build";
import MenuIcon from "@mui/icons-material/Menu";
import StateToolServices from "../Services/StateToolsServices";
import kardexServices from "../Services/KardexServices";
import { useKeycloak } from "@react-keycloak/web";
import LibraryAddIcon from "@mui/icons-material/LibraryAdd";
import AssessmentIcon from "@mui/icons-material/Assessment";
import ContactsIcon from "@mui/icons-material/Contacts";
import PersonAddAltIcon from '@mui/icons-material/PersonAddAlt';
import AdminPanelSettingsIcon from "@mui/icons-material/AdminPanelSettings";
import ReportIcon from '@mui/icons-material/Report';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import VisibilityIcon from '@mui/icons-material/Visibility';

const ToolList = () => {
    const [tools, setTools] = useState([]);
    const [groupedTools, setGroupedTools] = useState([]);
    const [drawerOpen, setDrawerOpen] = useState(false);
    const [expandedGroups, setExpandedGroups] = useState({});
    const [viewMode, setViewMode] = useState('grouped'); // 'grouped' o 'individual'
    const navigate = useNavigate();
    const [stateNames, setStateNames] = useState({});
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

    // Función para agrupar herramientas por nombre y categoría
    const groupToolsByNameAndCategory = (tools) => {
        const grouped = {};
        
        tools.forEach(tool => {
            const key = `${tool.name}_${tool.category}`;
            
            if (!grouped[key]) {
                grouped[key] = {
                    id: key,
                    name: tool.name,
                    category: tool.category,
                    replacement_cost: tool.replacement_cost,
                    instances: [],
                    totalCount: 0,
                    availableCount: 0,
                    loanedCount: 0,
                    repairCount: 0,
                    outOfServiceCount: 0
                };
            }
            
            grouped[key].instances.push(tool);
            grouped[key].totalCount++;
            
            // Contar por estado
            switch(tool.states) {
                case 1: // Available
                    grouped[key].availableCount++;
                    break;
                case 2: // Loaned
                    grouped[key].loanedCount++;
                    break;
                case 3: // In Repair
                    grouped[key].repairCount++;
                    break;
                case 4: // Out of Service
                    grouped[key].outOfServiceCount++;
                    break;
            }
        });
        
        return Object.values(grouped);
    };

    const handleGoToAddTool = () => {
        navigate("/AddTools");
    };

    const handleEdit = (id) => {
        navigate(`/EditTools/${id}`);
    };

    const handleDelete = (id) => {
        if (window.confirm("¿Seguro que quieres eliminar esta herramienta?")) {
            ToolServices.deleteid(id)
                .then(() => {
                    fetchTools();
                })
                .catch(error => {
                    console.error("Error al eliminar herramienta:", error);
                });
        }
    };

    const handleDisarget = (idtool) => {
        if (window.confirm("¿Seguro que quieres dar de baja esta herramienta?")) {
            ToolServices.unsuscribeTools(idtool)
                .then(() => {
                    fetchTools();
                    kardexServices.create({
                        date: new Date(),
                        stateToolsId: 4,
                        username: keycloak.tokenParsed.preferred_username,
                        quantity: 1,
                        idtool: idtool
                    });
                    alert("Herramienta dada de baja");
                })
                .catch(error => {
                    console.error("Error al dar de baja herramienta:", error);
                });
        }
    };

    const toggleGroupExpansion = (groupId) => {
        setExpandedGroups(prev => ({
            ...prev,
            [groupId]: !prev[groupId]
        }));
    };

    const getStateColor = (stateId) => {
        switch(stateId) {
            case 1: return "#4caf50"; // Verde - Available
            case 2: return "#ff9800"; // Naranja - Loaned
            case 3: return "#2196f3"; // Azul - In Repair
            case 4: return "#f44336"; // Rojo - Out of Service
            default: return "#757575"; // Gris - Unknown
        }
    };

    const getStateChip = (stateId, count) => {
        if (count === 0) return null;
        
        const colors = {
            1: { bg: "#e8f5e8", color: "#2e7d32" },
            2: { bg: "#fff3e0", color: "#f57c00" },
            3: { bg: "#e3f2fd", color: "#1976d2" },
            4: { bg: "#ffebee", color: "#d32f2f" }
        };
        
        const stateTexts = {
            1: "Disponible",
            2: "Prestado", 
            3: "Reparación",
            4: "Fuera de servicio"
        };

        return (
            <Chip 
                label={`${count} ${stateTexts[stateId]}`}
                size="small"
                sx={{ 
                    backgroundColor: colors[stateId]?.bg || "#f5f5f5",
                    color: colors[stateId]?.color || "#666",
                    fontSize: "0.75rem",
                    height: "24px",
                    mr: 0.5,
                    mb: 0.5
                }}
            />
        );
    };

    const fetchTools = () => {
        ToolServices.getAll()
            .then((response) => {
                setTools(response.data);
                
                // Agrupar herramientas
                const grouped = groupToolsByNameAndCategory(response.data);
                setGroupedTools(grouped);
                
                console.log("🔧 Herramientas agrupadas:", grouped);
                
                const uniqueStateIds = [...new Set(response.data.map(tool => tool.states))].filter(id => id != null);
                Promise.all(uniqueStateIds.map(id =>
                    StateToolServices.getid(id)
                        .then(res => ({ id, name: res.data.name }))
                        .catch(() => ({ id, name: "Desconocido" }))
                )).then(results => {
                    const namesObj = {};
                    results.forEach(({ id, name }) => {
                        namesObj[id] = name;
                    });
                    setStateNames(namesObj);
                });
            })
            .catch((error) => {
                console.error("There was an error!", error);
            });
    };

    useEffect(() => {
        fetchTools();
    }, []);

    return (
        <>
            {/* Botón para abrir la barra lateral */}
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

            {/* Barra lateral temporal */}
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
                                <ListItemIcon sx={{ color: "#FA812F", minWidth: 40 }}>
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

            {/* Fondo que cubre toda la pantalla */}
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

            {/* Contenido principal */}
            <Box sx={{
                minHeight: "100vh",
                backgroundColor: "#FEF3E2",
                py: 4,
                display: "flex",
                alignItems: "center",
                pt: 8
            }}>
                <Box
                    sx={{
                        maxWidth: 1200,
                        mx: "auto",
                        mt: 4,
                        backgroundColor: "#fff8f0",
                        borderRadius: 4,
                        boxShadow: 6,
                        p: 4,
                        border: "2px solid rgba(255, 94, 0, 0.2)",
                        width: '100%'
                    }}
                >
                    <Typography variant="h4" align="center" gutterBottom sx={{ 
                        fontWeight: "bold", 
                        mb: 4, 
                        color: "rgba(255, 94, 0, 1)" 
                    }}>
                        🔧 Lista de Herramientas
                    </Typography>

                    {/* Botones de control */}
                    <Box sx={{ display: "flex", justifyContent: "space-between", mb: 3 }}>
                        <Box sx={{ display: "flex", gap: 1 }}>
                            <Button
                                variant={viewMode === 'grouped' ? 'contained' : 'outlined'}
                                onClick={() => setViewMode('grouped')}
                                sx={{
                                    backgroundColor: viewMode === 'grouped' ? "rgba(255, 94, 0, 1)" : "transparent",
                                    borderColor: "rgba(255, 94, 0, 1)",
                                    color: viewMode === 'grouped' ? "#fff" : "rgba(255, 94, 0, 1)",
                                    '&:hover': {
                                        backgroundColor: viewMode === 'grouped' ? "rgba(255, 94, 0, 0.8)" : "rgba(255, 94, 0, 0.1)"
                                    }
                                }}
                            >
                                📊 Vista Agrupada
                            </Button>
                            <Button
                                variant={viewMode === 'individual' ? 'contained' : 'outlined'}
                                onClick={() => setViewMode('individual')}
                                sx={{
                                    backgroundColor: viewMode === 'individual' ? "rgba(255, 94, 0, 1)" : "transparent",
                                    borderColor: "rgba(255, 94, 0, 1)",
                                    color: viewMode === 'individual' ? "#fff" : "rgba(255, 94, 0, 1)",
                                    '&:hover': {
                                        backgroundColor: viewMode === 'individual' ? "rgba(255, 94, 0, 0.8)" : "rgba(255, 94, 0, 0.1)"
                                    }
                                }}
                            >
                                📋 Vista Individual
                            </Button>
                        </Box>
                        <Button 
                            variant="contained" 
                            onClick={handleGoToAddTool} 
                            sx={{ 
                                backgroundColor: "rgba(255, 94, 0, 1)",
                                '&:hover': {
                                    backgroundColor: "rgba(255, 94, 0, 0.8)"
                                }
                            }}
                        >
                            ➕ Agregar Herramienta
                        </Button>
                    </Box>

                    {/* Vista Agrupada */}
                    {viewMode === 'grouped' && (
                        <Box>
                            {groupedTools.map((group) => (
                                <Accordion 
                                    key={group.id}
                                    expanded={expandedGroups[group.id] || false}
                                    onChange={() => toggleGroupExpansion(group.id)}
                                    sx={{ 
                                        mb: 2,
                                        boxShadow: 2,
                                        '&:before': { display: 'none' }
                                    }}
                                >
                                    <AccordionSummary
                                        expandIcon={<ExpandMoreIcon />}
                                        sx={{
                                            backgroundColor: 'rgba(255, 94, 0, 0.05)',
                                            '&:hover': {
                                                backgroundColor: 'rgba(255, 94, 0, 0.1)'
                                            }
                                        }}
                                    >
                                        <Box sx={{ display: 'flex', alignItems: 'center', width: '100%' }}>
                                            <Box sx={{ flex: 1 }}>
                                                <Typography variant="h6" sx={{ fontWeight: 'bold' }}>
                                                    {group.name}
                                                </Typography>
                                                <Typography variant="body2" color="text.secondary">
                                                    Categoría: {group.category} | Costo: ${group.replacement_cost}
                                                </Typography>
                                            </Box>
                                            <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                                                <Chip 
                                                    label={`Total: ${group.totalCount}`}
                                                    sx={{ 
                                                        backgroundColor: 'rgba(255, 94, 0, 0.1)',
                                                        color: 'rgba(255, 94, 0, 1)',
                                                        fontWeight: 'bold'
                                                    }}
                                                />
                                                {getStateChip(1, group.availableCount)}
                                                {getStateChip(2, group.loanedCount)}
                                                {getStateChip(3, group.repairCount)}
                                                {getStateChip(4, group.outOfServiceCount)}
                                            </Box>
                                        </Box>
                                    </AccordionSummary>
                                    <AccordionDetails>
                                        <TableContainer component={Paper} sx={{ boxShadow: 1 }}>
                                            <Table size="small">
                                                <TableHead>
                                                    <TableRow sx={{ backgroundColor: "rgba(255, 94, 0, 0.1)" }}>
                                                        <TableCell sx={{ fontWeight: 'bold' }}>ID</TableCell>
                                                        <TableCell sx={{ fontWeight: 'bold' }}>Estado</TableCell>
                                                        <TableCell sx={{ fontWeight: 'bold' }}>Acciones</TableCell>
                                                    </TableRow>
                                                </TableHead>
                                                <TableBody>
                                                    {group.instances.map((tool) => (
                                                        <TableRow key={tool.id}>
                                                            <TableCell>{tool.id}</TableCell>
                                                            <TableCell>
                                                                <Chip 
                                                                    label={stateNames[tool.states] || "Cargando..."}
                                                                    size="small"
                                                                    sx={{ 
                                                                        backgroundColor: getStateColor(tool.states) + '20',
                                                                        color: getStateColor(tool.states),
                                                                        fontWeight: 'bold'
                                                                    }}
                                                                />
                                                            </TableCell>
                                                            <TableCell>
                                                                <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
                                                                    <Button
                                                                        variant="outlined"
                                                                        color="primary"
                                                                        size="small"
                                                                        onClick={() => handleEdit(tool.id)}
                                                                    >
                                                                        ✏️ Editar
                                                                    </Button>
                                                                    <Button
                                                                        variant="outlined"
                                                                        color="error"
                                                                        size="small"
                                                                        onClick={() => handleDelete(tool.id)}
                                                                    >
                                                                        🗑️ Eliminar
                                                                    </Button>
                                                                    {isAdmin && (
                                                                        <Button
                                                                            variant="outlined"
                                                                            color="warning"
                                                                            size="small"
                                                                            onClick={() => handleDisarget(tool.id)}
                                                                        >
                                                                            ⬇️ Dar de baja
                                                                        </Button>
                                                                    )}
                                                                </Box>
                                                            </TableCell>
                                                        </TableRow>
                                                    ))}
                                                </TableBody>
                                            </Table>
                                        </TableContainer>
                                    </AccordionDetails>
                                </Accordion>
                            ))}
                        </Box>
                    )}

                    {/* Vista Individual */}
                    {viewMode === 'individual' && (
                        <TableContainer component={Paper} sx={{ boxShadow: 3 }}>
                            <Table>
                                <TableHead>
                                    <TableRow sx={{ backgroundColor: "rgba(255, 94, 0, 1)" }}>
                                        <TableCell sx={{ color: "#fff", fontWeight: 'bold' }}>ID</TableCell>
                                        <TableCell sx={{ color: "#fff", fontWeight: 'bold' }}>Nombre</TableCell>
                                        <TableCell sx={{ color: "#fff", fontWeight: 'bold' }}>Categoría</TableCell>
                                        <TableCell sx={{ color: "#fff", fontWeight: 'bold' }}>Costo Reemplazo</TableCell>
                                        <TableCell sx={{ color: "#fff", fontWeight: 'bold' }}>Estado</TableCell>
                                        <TableCell sx={{ color: "#fff", fontWeight: 'bold' }}>Acciones</TableCell>
                                    </TableRow>
                                </TableHead>
                                <TableBody>
                                    {tools.map((tool) => (
                                        <TableRow key={tool.id} sx={{ '&:hover': { backgroundColor: 'rgba(255, 94, 0, 0.05)' } }}>
                                            <TableCell>{tool.id}</TableCell>
                                            <TableCell>{tool.name}</TableCell>
                                            <TableCell>{tool.category}</TableCell>
                                            <TableCell>${tool.replacement_cost}</TableCell>
                                            <TableCell>
                                                <Chip 
                                                    label={stateNames[tool.states] || "Cargando..."}
                                                    size="small"
                                                    sx={{ 
                                                        backgroundColor: getStateColor(tool.states) + '20',
                                                        color: getStateColor(tool.states),
                                                        fontWeight: 'bold'
                                                    }}
                                                />
                                            </TableCell>
                                            <TableCell>
                                                <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
                                                    <Button
                                                        variant="outlined"
                                                        color="primary"
                                                        size="small"
                                                        onClick={() => handleEdit(tool.id)}
                                                    >
                                                        ✏️ Editar
                                                    </Button>
                                                    <Button
                                                        variant="outlined"
                                                        color="error"
                                                        size="small"
                                                        onClick={() => handleDelete(tool.id)}
                                                    >
                                                        🗑️ Eliminar
                                                    </Button>
                                                    {isAdmin && (
                                                        <Button
                                                            variant="outlined"
                                                            color="warning"
                                                            size="small"
                                                            onClick={() => handleDisarget(tool.id)}
                                                        >
                                                            ⬇️ Dar de baja
                                                        </Button>
                                                    )}
                                                </Box>
                                            </TableCell>
                                        </TableRow>
                                    ))}
                                </TableBody>
                            </Table>
                        </TableContainer>
                    )}
                </Box>
            </Box>
        </>
    );
};

export default ToolList;