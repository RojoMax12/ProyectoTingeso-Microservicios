import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import ToolServices from '../Services/ToolServices';
import { 
    Box, Typography, Paper, Stack, IconButton, Drawer, List, 
    ListItem, ListItemButton, ListItemIcon, ListItemText, 
    Button, TextField, MenuItem, Alert 
} from "@mui/material";
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
    
    const [toolData, setToolData] = useState({
        name: "",
        category: "",
        replacement_cost: "",
        states: 1
    });

    const [quantity, setQuantity] = useState("1");

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
            .then(response => setExistingTools(response.data || []))
            .catch(error => console.error("❌ Error al cargar herramientas:", error));
    }, []);

    useEffect(() => {
        if (toolData.name && toolData.category && existingTools.length > 0) {
            const matchingTool = existingTools.find(tool => 
                tool.name?.toLowerCase().trim() === toolData.name.toLowerCase().trim() &&
                tool.category?.toLowerCase().trim() === toolData.category.toLowerCase().trim()
            );

            if (matchingTool) {
                setAutoCompleteInfo({
                    found: true,
                    count: existingTools.filter(t => t.name.toLowerCase().trim() === toolData.name.toLowerCase().trim()).length,
                    reference: matchingTool
                });

                if (!toolData.replacement_cost) {
                    setToolData(prev => ({ ...prev, replacement_cost: matchingTool.replacement_cost.toString() }));
                }
            } else {
                setAutoCompleteInfo(null);
            }
        }
    }, [toolData.name, toolData.category, existingTools]);

    const handleInputChange = (field, value) => {
        setToolData(prev => ({ ...prev, [field]: value }));
    };

    const handleSubmit = () => {
        const numQuantity = parseInt(quantity) || 1;
        const newTool = {
            name: toolData.name,
            category: toolData.category,
            replacement_cost: parseFloat(toolData.replacement_cost),
            states: 1
        };

        ToolServices.createmultiple(newTool, numQuantity)
            .then(() => {
                alert(`✅ Herramientas agregadas exitosamente!`);
                navigate("/ToolList");
            })
            .catch(error => console.error("❌ Error:", error));
    };

    return (
        <>
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

            {/* Barra lateral */}
            <Drawer
                open={drawerOpen}
                onClose={() => setDrawerOpen(false)}
                variant="temporary"
                sx={{ [`& .MuiDrawer-paper`]: { width: 240, backgroundColor: "#FEF3E2" } }}
            >
                <List>
                    {sidebarOptions.map((option) => (
                        <ListItem key={option.text} disablePadding>
                            <ListItemButton 
                                onClick={() => { navigate(option.path); setDrawerOpen(false); }}
                                sx={{ mx: 1, borderRadius: 1, my: 0.5, '&:hover': { backgroundColor: 'rgba(255, 94, 0, 0.1)' } }}
                            >
                                <ListItemIcon sx={{ color: "#FA812F", minWidth: 40 }}>{option.icon}</ListItemIcon>
                                <ListItemText primary={option.text} />
                            </ListItemButton>
                        </ListItem>
                    ))}
                </List>
            </Drawer>

            {/* Fondo FIJO que cubre toda la pantalla (Igual que en ToolList) */}
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

            {/* Contenedor principal con scroll (Igual que en ToolList) */}
            <Box sx={{
                backgroundColor: "#FEF3E2",
                py: 4,
                alignItems: "center",
                pt: 8
            }}>
                <Paper 
                    sx={{ 
                        maxWidth: 600, 
                        mx: "auto", 
                        backgroundColor: "#fff8f0", 
                        borderRadius: 4, 
                        boxShadow: 6, 
                        p: 4, 
                        border: "2px solid rgba(255, 94, 0, 0.2)",
                        width: '90%' // Ajuste para móviles
                    }}
                >
                    <Typography 
                        variant="h4" 
                        align="center" 
                        gutterBottom 
                        sx={{ fontWeight: "bold", mb: 4, color: "rgba(255, 94, 0, 1)" }}
                    >
                        🔧 Agregar Herramienta
                    </Typography>

                    {autoCompleteInfo && (
                        <Alert severity="success" sx={{ mb: 3, borderRadius: 2 }}>
                            <Typography variant="body2">
                                <strong>🔍 Sugerencia:</strong> Ya existen {autoCompleteInfo.count} unidades similares. 
                                Costo sugerido: <strong>${autoCompleteInfo.reference.replacement_cost}</strong>.
                            </Typography>
                        </Alert>
                    )}

                    <Stack spacing={3}>
                        <TextField
                            label="Nombre de la Herramienta"
                            value={toolData.name}
                            onChange={(e) => handleInputChange('name', e.target.value)}
                            fullWidth
                            required
                        />

                        <TextField
                            select
                            label="Categoría"
                            value={toolData.category}
                            onChange={(e) => handleInputChange('category', e.target.value)}
                            fullWidth
                            required
                        >
                            {categories.map((cat) => (
                                <MenuItem key={cat} value={cat}>{cat}</MenuItem>
                            ))}
                        </TextField>

                        <TextField
                            label="Costo de Reemplazo ($)"
                            type="number"
                            value={toolData.replacement_cost} 
                            onChange={(e) => handleInputChange('replacement_cost', e.target.value)}
                            fullWidth
                            required
                        />

                        <TextField
                            label="Cantidad a ingresar"
                            type="number"
                            value={quantity}
                            onChange={(e) => setQuantity(e.target.value)}
                            fullWidth
                            required
                        />

                        <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} sx={{ pt: 2 }}>
                            <Button
                                variant="contained"
                                onClick={handleSubmit}
                                disabled={!toolData.name || !toolData.category || !toolData.replacement_cost}
                                sx={{
                                    flex: 2,
                                    backgroundColor: "rgba(255, 94, 0, 1)",
                                    py: 1.5,
                                    fontWeight: 'bold',
                                    '&:hover': { backgroundColor: "rgba(255, 94, 0, 0.8)" }
                                }}
                            >
                                ✅ Guardar
                            </Button>
                            <Button
                                variant="outlined"
                                onClick={() => navigate("/ToolList")}
                                sx={{
                                    flex: 1,
                                    borderColor: "rgba(255, 94, 0, 1)",
                                    color: "rgba(255, 94, 0, 1)",
                                    '&:hover': { backgroundColor: "rgba(255, 94, 0, 0.1)", borderColor: "rgba(255, 94, 0, 0.8)" }
                                }}
                            >
                                Cancelar
                            </Button>
                        </Stack>
                    </Stack>
                </Paper>
            </Box>
        </>
    );
};

export default AddTools;