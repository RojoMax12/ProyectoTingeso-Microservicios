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
        console.log("🔍 === INICIANDO VERIFICACIÓN DE AUTOCOMPLETADO ===");
        console.log("📝 Estado actual del formulario:", toolData);
        console.log("📚 Herramientas disponibles:", existingTools.length);
        
        // Mostrar todas las herramientas disponibles para debugging
        if (debugMode && existingTools.length > 0) {
            console.log("🗂️ Lista completa de herramientas:");
            existingTools.forEach((tool, index) => {
                console.log(`   ${index + 1}. Nombre: "${tool.name}" | Categoría: "${tool.category}" | Costo: ${tool.replacement_cost}`);
            });
        }

        if (toolData.name && toolData.category && existingTools.length > 0) {
            console.log(`🔎 Buscando coincidencias para: "${toolData.name}" + "${toolData.category}"`);
            
            // Buscar herramientas con el mismo nombre y categoría
            const matchingTools = existingTools.filter(tool => {
                const nameMatch = tool.name && 
                    tool.name.toLowerCase().trim() === toolData.name.toLowerCase().trim();
                const categoryMatch = tool.category && 
                    tool.category.toLowerCase().trim() === toolData.category.toLowerCase().trim();
                
                console.log(`   🔍 Comparando con "${tool.name}" + "${tool.category}":`, {
                    nameMatch,
                    categoryMatch,
                    bothMatch: nameMatch && categoryMatch
                });
                
                return nameMatch && categoryMatch;
            });

            console.log("🎯 Herramientas coincidentes encontradas:", matchingTools.length);
            
            if (matchingTools.length > 0) {
                const referenceTool = matchingTools[0];
                console.log("💡 Herramienta de referencia seleccionada:", referenceTool);
                
                setAutoCompleteInfo({
                    found: true,
                    count: matchingTools.length,
                    reference: referenceTool
                });

                // Autocompletar el costo de reemplazo si está vacío
                if (!toolData.replacement_cost && referenceTool.replacement_cost) {
                    console.log("🔄 Autocompletando costo de", referenceTool.replacement_cost);
                    setToolData(prev => ({
                        ...prev,
                        replacement_cost: referenceTool.replacement_cost.toString()
                    }));
                } else {
                    console.log("⚠️ NO autocompletando porque:", {
                        yaHayCosto: !!toolData.replacement_cost,
                        costoDeReferencia: referenceTool.replacement_cost
                    });
                }
            } else {
                console.log("❌ No se encontraron herramientas coincidentes");
                setAutoCompleteInfo(null);
            }
        } else {
            console.log("⏸️ Condiciones no cumplidas para autocompletado:", {
                tieneNombre: !!toolData.name,
                tieneCategoria: !!toolData.category,
                hayHerramientas: existingTools.length > 0
            });
            setAutoCompleteInfo(null);
        }
        
        console.log("🔍 === FIN VERIFICACIÓN DE AUTOCOMPLETADO ===");
    }, [toolData.name, toolData.category, existingTools, debugMode]);

    const handleInputChange = (field, value) => {
        console.log(`📝 Cambiando campo "${field}" a:`, value);
        
        // Si estamos cambiando el nombre o categoría, limpiar el costo para permitir autocompletado
        if ((field === 'name' || field === 'category') && value !== '') {
            setToolData(prev => ({
                ...prev,
                [field]: value,
                // Limpiar costo solo si estamos cambiando nombre/categoría para forzar autocompletado
                ...(field === 'name' || field === 'category' ? { replacement_cost: '' } : {})
            }));
        } else {
            setToolData(prev => ({
                ...prev,
                [field]: value
            }));
        }
    };

    const handleSubmit = () => {
        if (!toolData.name || !toolData.category || !toolData.replacement_cost) {
            alert("Por favor completa todos los campos requeridos.");
            return;
        }

        const newTool = {
            name: toolData.name,
            category: toolData.category,
            replacement_cost: parseFloat(toolData.replacement_cost),
            states: 1
        };

        console.log("📤 Enviando nueva herramienta:", newTool);

        ToolServices.create(newTool)
            .then(() => {
                alert(`✅ Herramienta "${toolData.name}" agregada exitosamente!`);
                
                // Limpiar formulario
                setToolData({
                    name: "",
                    category: "",
                    replacement_cost: "",
                    states: 1
                });
                setAutoCompleteInfo(null);
                
                navigate("/ToolList");
            })
            .catch(error => {
                console.error("❌ Error al agregar herramienta:", error);
                if (error.response && error.response.data) {
                    alert(`Error: ${error.response.data}`);
                } else {
                    alert("Error al agregar la herramienta. Intenta nuevamente.");
                }
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