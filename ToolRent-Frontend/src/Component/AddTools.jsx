import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import ToolServices from '../Services/ToolServices';
import { Box, Typography, Paper, Stack, IconButton, Drawer, List, ListItem, ListItemButton, ListItemIcon, ListItemText, Button, TextField, MenuItem, Alert } from "@mui/material";
import MenuIcon from "@mui/icons-material/Menu";
import HomeIcon from "@mui/icons-material/Home";
import BuildIcon from "@mui/icons-material/Build";
import LibraryAddIcon from "@mui/icons-material/LibraryAdd";
import AssessmentIcon from "@mui/icons-material/Assessment";
import ContactsIcon from "@mui/icons-material/Contacts";
import PersonAddAltIcon from '@mui/icons-material/PersonAddAlt';
import AdminPanelSettingsIcon from "@mui/icons-material/AdminPanelSettings";
import ReportIcon from '@mui/icons-material/Report';
import { useKeycloak } from '@react-keycloak/web';

const AddTools = () => {
    const navigate = useNavigate();
    const [drawerOpen, setDrawerOpen] = useState(false);
    const [existingTools, setExistingTools] = useState([]);
    const [autoCompleteInfo, setAutoCompleteInfo] = useState(null);
    const [debugMode, setDebugMode] = useState(true); // Para debugging temporal
    
    // Estados del formulario
    const [toolData, setToolData] = useState({
        name: "",
        category: "",
        replacement_cost: "",
        states: 1
    });

    const [quantity, setQuantity] = useState("");

    const { keycloak } = useKeycloak();
    const isAdmin = keycloak?.tokenParsed?.realm_access?.roles?.includes("ADMIN") || false;

    const categories = [
        "Herramientas Eléctricas",
        "Herramientas Manuales", 
        "Herramientas de Jardinería",
        "Herramientas de Construcción",
        "Herramientas de Plomería",
        "Herramientas de Carpintería",
        "Equipos de Seguridad",
        "Otros"
    ];

    // Cargar herramientas existentes al iniciar
    useEffect(() => {
        console.log("🔄 Iniciando carga de herramientas...");
        ToolServices.getAll()
            .then(response => {
                console.log("✅ Respuesta del servidor:", response);
                console.log("📦 Datos recibidos:", response.data);
                setExistingTools(response.data || []);
                
                // Mostrar estructura de la primera herramienta para verificar campos
                if (response.data && response.data.length > 0) {
                    console.log("🔧 Estructura de herramienta de ejemplo:", response.data[0]);
                    console.log("📋 Campos disponibles:", Object.keys(response.data[0]));
                }
            })
            .catch(error => {
                console.error("❌ Error al cargar herramientas:", error);
                setExistingTools([]);
            });
    }, []);

    // Función para autocompletar cuando cambia nombre o categoría
    useEffect(() => {
    if (toolData.name && toolData.category && existingTools.length > 0) {
        const matchingTool = existingTools.find(tool => 
            tool.name?.toLowerCase().trim() === toolData.name.toLowerCase().trim() &&
            tool.category?.toLowerCase().trim() === toolData.category.toLowerCase().trim()
        );

        if (matchingTool) {
            setAutoCompleteInfo({
                found: true,
                count: existingTools.filter(t => t.name === matchingTool.name).length,
                reference: matchingTool
            });

            // Solo actualizar si el campo está vacío para no sobrescribir cambios manuales
            if (!toolData.replacement_cost && matchingTool.replacement_cost) {
                setToolData(prev => ({
                    ...prev,
                    replacement_cost: matchingTool.replacement_cost.toString()
                }));
            }
        } else {
            setAutoCompleteInfo(null);
        }
    }
    }  , [toolData.name, toolData.category, existingTools]); // Quitamos toolData completo para evitar bucles 

    const handleInputChange = (field, value) => {
    setToolData(prev => ({
        ...prev,
        [field]: value
    }));
    };

    const handleSubmit = () => {
        const numQuantity = parseInt(quantity) || 1; // Si está vacío, por defecto 1

        const newTool = {
            name: toolData.name,
            category: toolData.category,
            replacement_cost: parseFloat(toolData.replacement_cost),
            states: 1
        };

        // Usamos numQuantity para asegurar que sea un entero
        ToolServices.createmultiple(newTool, numQuantity)
            .then(() => {
                alert(`✅ Herramientas agregadas exitosamente!`);
                navigate("/ToolList");
            })
            .catch(error => {
                console.error("❌ Error:", error);
                alert("Error al agregar. Revisa la consola.");
            });
    };

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

            <Box sx={{ p: 4, minHeight: "100vh" }}>
                {/* Botón menú hamburguesa */}
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

                {/* Drawer del menú lateral */}
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

                {/* Formulario principal */}
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
                        🔧 Agregar Nueva Herramienta
                    </Typography>

                    {/* INFO: Mostrar estado actual para debugging */}
                    <Alert severity="info" sx={{ mb: 2 }}>
                        <Typography variant="caption">
                            <strong>Debug:</strong> 
                            Herramientas cargadas: {existingTools.length} | 
                            Autocompletado activo: {autoCompleteInfo ? 'Sí' : 'No'}
                            {autoCompleteInfo && (
                                <span> | Costo sugerido: ${autoCompleteInfo.reference.replacement_cost}</span>
                            )}
                        </Typography>
                    </Alert>

                    {/* Alerta de autocompletado */}
                    {autoCompleteInfo && (
                        <Alert 
                            severity="success" 
                            sx={{ mb: 3, borderRadius: 2 }}
                        >
                            <Typography variant="body2">
                                <strong>🔍 Herramienta similar encontrada:</strong> 
                                {` Ya existen ${autoCompleteInfo.count} herramienta(s) con el nombre "${toolData.name}" en la categoría "${toolData.category}".`}
                                <br />
                                <strong>💡 Autocompletado:</strong> Costo de reemplazo sugerido: ${autoCompleteInfo.reference.replacement_cost || "No disponible"}
                            </Typography>
                        </Alert>
                    )}

                    <Stack spacing={3}>
                        {/* Campo Nombre */}
                        <TextField
                            label="Nombre de la Herramienta"
                            value={toolData.name}
                            onChange={(e) => handleInputChange('name', e.target.value)}
                            fullWidth
                            required
                            placeholder="Ej: Taladro Eléctrico"
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
                            }}
                        />

                        {/* Campo Categoría */}
                        <TextField
                            select
                            label="Categoría"
                            value={toolData.category}
                            onChange={(e) => handleInputChange('category', e.target.value)}
                            fullWidth
                            required
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
                            }}
                        >
                            {categories.map((category) => (
                                <MenuItem key={category} value={category}>
                                    {category}
                                </MenuItem>
                            ))}
                        </TextField>

                        {/* Campo Costo de Reemplazo */}
                        <TextField
                            label="Costo de Reemplazo"
                            type="number"
                            value={toolData.replacement_cost} 
                            onChange={(e) => handleInputChange('replacement_cost', e.target.value)}
                            fullWidth
                            required
                            placeholder="0.00"
                            inputProps={{ min: 0, step: 0.01 }}
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
                            }}
                        />

                        <TextField
                            label="Cantidad a Crear"
                            type="number"
                            value={quantity}
                            onChange={(e) => setQuantity(e.target.value)}
                            fullWidth
                            required
                            placeholder="1"
                            inputProps={{ min: 1 }}
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
                            }}
                        />

                        {/* Botones */}
                        <Stack direction="row" spacing={2}>
                            <Button
                                variant="contained"
                                onClick={handleSubmit}
                                disabled={!toolData.name || !toolData.category || !toolData.replacement_cost}
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
                                ✅ Agregar Herramienta
                            </Button>

                            <Button
                                variant="outlined"
                                onClick={() => navigate("/ToolList")}
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

export default AddTools;