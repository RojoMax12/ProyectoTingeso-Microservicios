import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import LoanToolServices from '../Services/LoanToolsServices';
import { Box, Typography, Paper, Stack, IconButton, Drawer, List, ListItem, ListItemButton, ListItemIcon, ListItemText, Button, MenuItem, Select, InputLabel, FormControl, TextField, Chip } from "@mui/material";
import MenuIcon from "@mui/icons-material/Menu";
import HomeIcon from "@mui/icons-material/Home";
import BuildIcon from "@mui/icons-material/Build";
import AddBoxIcon from "@mui/icons-material/AddBox";
import ToolServices from '../Services/ToolServices';
import ClientServices from '../Services/ClientServices';
import KardexServices from '../Services/KardexServices';
import { useKeycloak } from '@react-keycloak/web';
import LibraryAddIcon from "@mui/icons-material/LibraryAdd";
import AssessmentIcon from "@mui/icons-material/Assessment";
import ContactsIcon from "@mui/icons-material/Contacts";
import PersonAddAltIcon from '@mui/icons-material/PersonAddAlt';
import AdminPanelSettingsIcon from "@mui/icons-material/AdminPanelSettings";
import ReportIcon from '@mui/icons-material/Report';

const LoanTool = () => {
    const navigate = useNavigate();
    const { id } = useParams(); // ID del cliente desde la URL
    const [toollist, settoollist] = useState([]);
    const [groupedTools, setGroupedTools] = useState([]);
    const [clientlist, setclientlist] = useState([]);
    const [selectedTool, setSelectedTool] = useState("");
    const [selectedClient, setSelectedClient] = useState(id || "");
    const [drawerOpen, setDrawerOpen] = useState(false);
    const [startDate, setStartDate] = useState("");
    const [returnDate, setReturnDate] = useState("");
    const [availableInstances, setAvailableInstances] = useState([]);
    const { keycloak } = useKeycloak();
    const isAdmin = keycloak.hasRealmRole('ADMIN');

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
        ToolServices.getAll()
            .then(response => {
                const availableTools = response.data.filter(tool => tool.states === 1);
                settoollist(availableTools);
                
                // Agrupar herramientas por nombre y categoría
                const grouped = groupToolsByNameAndCategory(availableTools);
                setGroupedTools(grouped);
                
                console.log("🔧 Herramientas agrupadas:", grouped);
            })
            .catch(error => {
                console.error("Error fetching tool list:", error);
            });
            
        ClientServices.getByid(id)
            .then(response => {
                setclientlist([response.data]);
            })
            .catch(error => {
                console.error("Error fetching client list:", error);
            });
    }, [id]);

    // Función para agrupar herramientas por nombre y categoría
    const groupToolsByNameAndCategory = (tools) => {
        const grouped = {};
        
        tools.forEach(tool => {
            const key = `${tool.name}_${tool.category}`;
            
            if (!grouped[key]) {
                grouped[key] = {
                    id: key, // ID único para el grupo
                    name: tool.name,
                    category: tool.category,
                    replacement_cost: tool.replacement_cost,
                    count: 0,
                    instances: []
                };
            }
            
            grouped[key].count++;
            grouped[key].instances.push(tool);
        });
        
        return Object.values(grouped);
    };

    // Cuando se selecciona una herramienta agrupada, obtener instancias disponibles
    const handleToolSelection = (toolGroupId) => {
        setSelectedTool(toolGroupId);
        
        const selectedGroup = groupedTools.find(group => group.id === toolGroupId);
        if (selectedGroup) {
            setAvailableInstances(selectedGroup.instances);
            console.log("📦 Instancias disponibles:", selectedGroup.instances);
        }
    };

    const handleLoan = () => {
        if (!selectedTool || !selectedClient || !startDate || !returnDate) {
            alert("Selecciona una herramienta, un cliente y ambas fechas.");
            return;
        }

        // Seleccionar la primera instancia disponible del grupo
        const selectedGroup = groupedTools.find(group => group.id === selectedTool);
        if (!selectedGroup || selectedGroup.instances.length === 0) {
            alert("No hay instancias disponibles de esta herramienta.");
            return;
        }

        const toolInstanceId = selectedGroup.instances[0].id; // Tomar la primera instancia
        const clienteObj = clientlist.find(c => String(c.id) === String(selectedClient));
        const emailclient = clienteObj ? clienteObj.email : "unknown";

        console.log("🏷️ Prestando herramienta ID:", toolInstanceId);

        LoanToolServices.create({
            toolid: toolInstanceId, // ID de la instancia específica
            clientid: selectedClient,
            initiallenddate: startDate,
            finalreturndate: returnDate
        })
        .then(() => {
            alert(`Herramienta "${selectedGroup.name}" prestada correctamente.`);
            
            // Registrar en kardex
            KardexServices.create({
                idtool: toolInstanceId,
                username: emailclient,
                date: new Date(),
                stateToolsId: 2,
                quantity: 1
            });
            
            navigate("/Home");
        })
        .catch(error => {
            alert("Error al prestar herramienta.");
            console.error(error);
        });
    };
    

    
    return (
        <>
            {/* Fondo pantalla completa */}
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

            {/*SiderBar*/}
            <Box sx={{ p: 4, minHeight: "100vh" }}>
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
                
                <Paper 
                    sx={{ 
                        maxWidth: 600, 
                        mx: "auto", 
                        p: 4, 
                        mt: 6, 
                        borderRadius: 4, 
                        boxShadow: "0 8px 32px rgba(255, 94, 0, 0.1)",
                        border: "2px solid rgba(255, 94, 0, 0.1)"
                    }}
                >
                    <Typography 
                        variant="h4" 
                        align="center" 
                        gutterBottom 
                        sx={{ 
                            fontWeight: "bold", 
                            color: "rgba(255, 94, 0, 1)",
                            mb: 3
                        }}
                    >
                        🔧 Prestar Herramienta
                    </Typography>
                    
                    <Stack spacing={3}>
                        {/* Selector de Herramienta Agrupada */}
                        <FormControl fullWidth>
                            <InputLabel 
                                id="tool-select-label"
                                sx={{
                                    '&.Mui-focused': {
                                        color: 'rgba(255, 94, 0, 1)',
                                    }
                                }}
                            >
                                Herramienta
                            </InputLabel>
                            <Select
                                labelId="tool-select-label"
                                value={selectedTool}
                                label="Herramienta"
                                onChange={(e) => handleToolSelection(e.target.value)}
                                sx={{
                                    '&:hover .MuiOutlinedInput-notchedOutline': {
                                        borderColor: 'rgba(255, 94, 0, 0.5)',
                                    },
                                    '&.Mui-focused .MuiOutlinedInput-notchedOutline': {
                                        borderColor: 'rgba(255, 94, 0, 1)',
                                    },
                                }}
                            >   
                                {groupedTools.map(toolGroup => (
                                    <MenuItem 
                                        key={toolGroup.id} 
                                        value={toolGroup.id}
                                        sx={{
                                            display: 'flex',
                                            justifyContent: 'space-between',
                                            alignItems: 'center'
                                        }}
                                    >
                                        <Box sx={{ display: 'flex', flexDirection: 'column', flex: 1 }}>
                                            <Typography variant="body1" sx={{ fontWeight: 500 }}>
                                                {toolGroup.name}
                                            </Typography>
                                            <Typography variant="caption" color="text.secondary">
                                                {toolGroup.category}
                                            </Typography>
                                        </Box>
                                        <Chip 
                                            label={`${toolGroup.count} disponible${toolGroup.count > 1 ? 's' : ''}`}
                                            size="small"
                                            color="primary"
                                            sx={{ 
                                                backgroundColor: 'rgba(255, 94, 0, 0.1)',
                                                color: 'rgba(255, 94, 0, 1)',
                                                fontWeight: 'bold'
                                            }}
                                        />
                                    </MenuItem>
                                ))}
                            </Select>
                        </FormControl>

                        {/* Información de la herramienta seleccionada */}
                        {selectedTool && (
                            <Paper 
                                sx={{ 
                                    p: 2, 
                                    backgroundColor: 'rgba(255, 94, 0, 0.05)',
                                    border: '1px solid rgba(255, 94, 0, 0.2)',
                                    borderRadius: 2
                                }}
                            >
                                {(() => {
                                    const selectedGroup = groupedTools.find(group => group.id === selectedTool);
                                    return selectedGroup ? (
                                        <Stack spacing={1}>
                                            <Typography variant="h6" sx={{ color: 'rgba(255, 94, 0, 1)' }}>
                                                📋 Detalles de la Herramienta
                                            </Typography>
                                            <Typography variant="body2">
                                                <strong>Nombre:</strong> {selectedGroup.name}
                                            </Typography>
                                            <Typography variant="body2">
                                                <strong>Categoría:</strong> {selectedGroup.category}
                                            </Typography>
                                            <Typography variant="body2">
                                                <strong>Costo de Reemplazo:</strong> ${selectedGroup.replacement_cost}
                                            </Typography>
                                            <Typography variant="body2">
                                                <strong>Cantidad Disponible:</strong> {selectedGroup.count} unidad{selectedGroup.count > 1 ? 'es' : ''}
                                            </Typography>
                                        </Stack>
                                    ) : null;
                                })()}
                            </Paper>
                        )}

                        {/* Selector de Cliente */}
                        <FormControl fullWidth>
                            <InputLabel 
                                id="client-select-label"
                                sx={{
                                    '&.Mui-focused': {
                                        color: 'rgba(255, 94, 0, 1)',
                                    }
                                }}
                            >
                                Cliente
                            </InputLabel>
                            <Select
                                labelId="client-select-label"
                                value={selectedClient}
                                label="Cliente"
                                onChange={(e) => setSelectedClient(e.target.value)}
                                sx={{
                                    '&:hover .MuiOutlinedInput-notchedOutline': {
                                        borderColor: 'rgba(255, 94, 0, 0.5)',
                                    },
                                    '&.Mui-focused .MuiOutlinedInput-notchedOutline': {
                                        borderColor: 'rgba(255, 94, 0, 1)',
                                    },
                                }}
                            >
                                {clientlist.map(client => (
                                    <MenuItem key={client.id} value={client.id}>
                                        {client.name} ({client.rut})
                                    </MenuItem>
                                ))}
                            </Select>
                        </FormControl>

                        {/* Fechas */}
                        <TextField
                            label="Fecha inicio préstamo"
                            type="date"
                            value={startDate}
                            onChange={(e) => setStartDate(e.target.value)}
                            InputLabelProps={{ shrink: true }}
                            fullWidth
                            required
                            inputProps={{ min: new Date().toISOString().split("T")[0] }}
                            sx={{
                                '& .MuiOutlinedInput-root': {
                                    '&:hover fieldset': {
                                        borderColor: 'rgba(255, 94, 0, 0.5)',
                                    },
                                    '&.Mui-focused fieldset': {
                                        borderColor: 'rgba(255, 94, 0, 1)',
                                    },
                                },
                                '& .MuiInputLabel-root.Mui-focused': {
                                    color: 'rgba(255, 94, 0, 1)',
                                },
                                '& input[type="date"]::-webkit-calendar-picker-indicator': {
                                    backgroundImage: `url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 20 20' fill='%23ff5e00'%3e%3cpath fill-rule='evenodd' d='M6 2a1 1 0 00-1 1v1H4a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V6a2 2 0 00-2-2h-1V3a1 1 0 10-2 0v1H7V3a1 1 0 00-1-1zm0 5a1 1 0 000 2h8a1 1 0 100-2H6z' clip-rule='evenodd'/%3e%3c/svg%3e")`,
                                    backgroundRepeat: 'no-repeat',
                                    backgroundPosition: 'center',
                                    backgroundSize: '16px',
                                    cursor: 'pointer',
                                    width: '20px',
                                    height: '20px'
                                }
                            }}
                        />

                        <TextField
                            label="Fecha retorno herramienta"
                            type="date"
                            value={returnDate}
                            onChange={(e) => setReturnDate(e.target.value)}
                            InputLabelProps={{ shrink: true }}
                            fullWidth
                            required
                            inputProps={{ 
                                min: startDate || new Date().toISOString().split("T")[0] 
                            }}
                            sx={{
                                '& .MuiOutlinedInput-root': {
                                    '&:hover fieldset': {
                                        borderColor: 'rgba(255, 94, 0, 0.5)',
                                    },
                                    '&.Mui-focused fieldset': {
                                        borderColor: 'rgba(255, 94, 0, 1)',
                                    },
                                },
                                '& .MuiInputLabel-root.Mui-focused': {
                                    color: 'rgba(255, 94, 0, 1)',
                                },
                                '& input[type="date"]::-webkit-calendar-picker-indicator': {
                                    backgroundImage: `url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 20 20' fill='%23ff5e00'%3e%3cpath fill-rule='evenodd' d='M6 2a1 1 0 00-1 1v1H4a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V6a2 2 0 00-2-2h-1V3a1 1 0 10-2 0v1H7V3a1 1 0 00-1-1zm0 5a1 1 0 000 2h8a1 1 0 100-2H6z' clip-rule='evenodd'/%3e%3c/svg%3e")`,
                                    backgroundRepeat: 'no-repeat',
                                    backgroundPosition: 'center',
                                    backgroundSize: '16px',
                                    cursor: 'pointer',
                                    width: '20px',
                                    height: '20px'
                                }
                            }}
                        />

                        {/* Botones */}
                        <Stack direction="row" spacing={2}>
                            <Button
                                variant="contained"
                                onClick={handleLoan}
                                disabled={!selectedTool || !selectedClient || !startDate || !returnDate}
                                sx={{
                                    flex: 1,
                                    backgroundColor: "rgba(255, 94, 0, 1)",
                                    borderRadius: 3,
                                    py: 1.5,
                                    '&:hover': {
                                        backgroundColor: "rgba(255, 94, 0, 0.8)"
                                    },
                                    '&:disabled': {
                                        backgroundColor: "rgba(255, 94, 0, 0.3)"
                                    }
                                }}
                            >
                                🔄 Prestar Herramienta
                            </Button>

                            <Button
                                variant="outlined"
                                onClick={() => navigate("/Home")}
                                sx={{
                                    flex: 1,
                                    borderColor: "rgba(255, 94, 0, 1)",
                                    color: "rgba(255, 94, 0, 1)",
                                    borderRadius: 3,
                                    py: 1.5,
                                    '&:hover': {
                                        backgroundColor: "rgba(255, 94, 0, 0.1)",
                                        borderColor: "rgba(255, 94, 0, 0.8)"
                                    }
                                }}
                            >
                                ❌ Cancelar
                            </Button>
                            
                        </Stack>
                    </Stack>
                </Paper>
            </Box>
        </>
    );
};

export default LoanTool;