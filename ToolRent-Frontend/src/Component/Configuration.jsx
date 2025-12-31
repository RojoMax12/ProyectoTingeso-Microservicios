import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  ThemeProvider, CssBaseline, AppBar, Toolbar, Typography, Stack, Avatar, Button,
  Container, Drawer, List, ListItem, ListItemButton, ListItemIcon, ListItemText,
  TextField, IconButton, Box, Paper, Divider
} from "@mui/material";
import HomeIcon from "@mui/icons-material/Home";
import BuildIcon from "@mui/icons-material/Build";
import MenuIcon from "@mui/icons-material/Menu";
import { createTheme } from "@mui/material/styles";
import { useKeycloak } from "@react-keycloak/web";
import AmountsandratesServices from "../Services/AmountsandratesServices"; // <-- Importación agregada
import LibraryAddIcon from "@mui/icons-material/LibraryAdd"; // 📚➕ Para Agregar Herramienta
import AssessmentIcon from "@mui/icons-material/Assessment"; // 📊 Para Ver Kardex (reporte
import ContactsIcon from "@mui/icons-material/Contacts";     // 📇 Para Clientes)
import PersonAddAltIcon from '@mui/icons-material/PersonAddAlt';
import AdminPanelSettingsIcon from "@mui/icons-material/AdminPanelSettings"; // 🛡️⚙️ Para Configuraciones (admin)
import ReportIcon from '@mui/icons-material/Report'; // 📈 Para Reportes
import StateToolsServices from "../Services/StateToolsServices"; // <-- Importación agregada


const Configuration = () => {
  const navigate = useNavigate();
  const [drawerOpen, setDrawerOpen] = useState(false);
  const photo = "";
  const { keycloak } = useKeycloak();
  const name = keycloak.tokenParsed?.name || "Usuario";
  const isAdmin = keycloak?.tokenParsed?.realm_access?.roles?.includes("ADMIN");

  // Estados para los campos del formulario
  const [dailyrentalrate, setDailyrentalrate] = useState("");
  const [dailylatefeefine, setDailylatefeefine] = useState("");
  const [reparationcharge, setReparationcharge] = useState("");

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

  const theme = createTheme({
    palette: {
      mode: "light",
      primary: {
        main: "#FEF3E2"
      },
      secondary: {
        main: "#FA812F"
      },
      background: {
        default: "#FEF3E2"
      }
    }
  });

  const ferchAmountsandrates = async () => {
    try {
      const response = await AmountsandratesServices.getall();
      const amountsAndRates = response.data;
      if (amountsAndRates.length > 0) {
        const config = amountsAndRates[0];
        setDailyrentalrate(config.dailyrentalrate);
        setDailylatefeefine(config.dailylatefeefine);
        setReparationcharge(config.reparationcharge);
      } else {
        console.warn("No se encontraron montos y tarifas.");
      }
    } catch (error) {
      console.error("Error al obtener montos y tarifas:", error);
    }
  };

  React.useEffect(() => {
    ferchAmountsandrates();
  }, []);


  const updateAmountsAndRates = () => {
    const data = {
      dailyrentalrate: parseFloat(dailyrentalrate) || 0,
      dailylatefeefine: parseFloat(dailylatefeefine) || 0,
      reparationcharge: parseFloat(reparationcharge) || 0
    };

    AmountsandratesServices.update(data)
      .then((response) => {
        alert("Montos y tarifas actualizados");
        console.log("Configuración actualizada:", response.data);
      })
      .catch((error) => {
        console.error("Error al actualizar montos y tarifas:", error);
        alert("Error al actualizar la configuración");
      });
  };

  const initializeAmountsAndRates = () => {
    AmountsandratesServices.create()
      .then((response) => {
        alert("Montos y tarifas inicializados");
        console.log("Montos y tarifas inicializados:", response.data);
      }
      )
      .catch((error) => {
        console.error("Error al inicializar montos y tarifas:", error);
        alert("Error al inicializar montos y tarifas");
      });
  };

  const initializeToolStates = () => {
    StateToolsServices.create()
      .then((response) => {
        alert("Estados de herramientas inicializados");
        console.log("Estados de herramientas inicializados:", response.data);
      }
      )
      .catch((error) => {
        console.error("Error al inicializar estados de herramientas:", error);
        alert("Error al inicializar estados de herramientas");
      });
  };

return (
  <ThemeProvider theme={theme}>
    <CssBaseline />

    {/* BOTÓN PARA ABRIR EL DRAWER */}
    <IconButton
      onClick={() => setDrawerOpen(true)}
      sx={{
        position: "fixed",
        top: 16,
        left: 16,
        zIndex: 1100,
        backgroundColor: "#FA812F",
        color: "white",
        boxShadow: 3,
        '&:hover': { backgroundColor: "#e06a1d" }
      }}
    >
      <MenuIcon />
    </IconButton>

    {/* SIDEBAR (DRAWER) */}
    <Drawer
      open={drawerOpen}
      onClose={() => setDrawerOpen(false)}
      variant="temporary"
      sx={{
        [`& .MuiDrawer-paper`]: { width: 220, boxSizing: "border-box", backgroundColor: "#FEF3E2" }
      }}
    >
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

    {/* CONTENIDO PRINCIPAL */}
    <Container maxWidth="lg" sx={{ mt: 10, pb: 5 }}>
      <Typography 
        variant="h3" 
        align="center" 
        sx={{ fontWeight: "bold", color: "#FA812F", mb: 6 }}
      >
        ⚙️ Configuración del Sistema
      </Typography>

      <Stack 
        direction={{ xs: "column", md: "row" }} 
        spacing={4} 
        alignItems="flex-start" 
        justifyContent="center"
      >
        
        {/* PANEL DE CONFIGURACIONES ACTUALES */}
        <Paper
          sx={{
            p: 4,
            width: { xs: "100%", md: "350px" },
            borderRadius: 4,
            backgroundColor: "#fff",
            boxShadow: 2,
            border: "1px solid rgba(0,0,0,0.05)"
          }}
        >
          <Typography variant="h6" sx={{ fontWeight: "bold", mb: 3, color: "#555" }}>
            Valores Actuales
          </Typography>
          
          <Stack spacing={2}>
            {[
              { label: "Tarifa Préstamo", value: dailyrentalrate, icon: "💰" },
              { label: "Multa Atraso", value: dailylatefeefine, icon: "⚠️" },
              { label: "Cargo Reparación", value: reparationcharge, icon: "🔧" }
            ].map((item, i) => (
              <Box 
                key={i} 
                sx={{ 
                  p: 2, 
                  borderRadius: 3, 
                  backgroundColor: "#FEF3E2", 
                  display: 'flex', 
                  justifyContent: 'space-between',
                  alignItems: 'center'
                }}
              >
                <Box>
                  <Typography variant="caption" sx={{ color: "#888", display: 'block' }}>
                    {item.label}
                  </Typography>
                  <Typography variant="h6" sx={{ fontWeight: "bold", color: "#FA812F" }}>
                    ${item.value || "0"}
                  </Typography>
                </Box>
                <Typography variant="h5">{item.icon}</Typography>
              </Box>
            ))}
          </Stack>
        </Paper>

        {/* PANEL DE EDICIÓN */}
        <Paper
          sx={{
            p: 4,
            maxWidth: 600,
            width: "100%",
            borderRadius: 4,
            backgroundColor: "#fff8f0",
            boxShadow: 4,
            border: "2px solid rgba(255, 94, 0, 0.1)"
          }}
        >
          <Stack spacing={3}>
            <Typography variant="h6" sx={{ fontWeight: "bold", color: "rgba(255, 94, 0, 1)" }}>
              Actualizar Parámetros
            </Typography>

            <TextField
              label="Nueva Tarifa de Préstamo"
              variant="outlined"
              fullWidth
              type="number"
              value={dailyrentalrate}
              onChange={(e) => setDailyrentalrate(e.target.value)}
            />

            <TextField
              label="Nueva Multa por Atraso"
              variant="outlined"
              fullWidth
              type="number"
              value={dailylatefeefine}
              onChange={(e) => setDailylatefeefine(e.target.value)}
            />

            <TextField
              label="Nuevo Cargo por Reparación"
              variant="outlined"
              fullWidth
              type="number"
              value={reparationcharge}
              onChange={(e) => setReparationcharge(e.target.value)}
            />

            <Button
              variant="contained"
              fullWidth
              sx={{ 
                mt: 2, 
                py: 1.5,
                color: "white",
                fontWeight: "bold",
                backgroundColor: "#FA812F",
                '&:hover': { backgroundColor: "#e06a1d" }
              }}
              onClick={updateAmountsAndRates}
            >
              Guardar Cambios
            </Button>

            <Divider sx={{ my: 1 }}>Acciones Especiales</Divider>

            <Stack direction="row" spacing={2}>
              <Button
                variant="outlined"
                fullWidth
                size="small"
                sx={{ color: "#FA812F", borderColor: "#FA812F" }}
                onClick={initializeAmountsAndRates}
              >
                Inicializar Tarifas
              </Button>

              <Button
                variant="outlined"
                fullWidth
                size="small"
                sx={{ color: "#FA812F", borderColor: "#FA812F" }}
                onClick={initializeToolStates}
              >
                Inicializar Estados de Herramientas
              </Button>
            </Stack>
          </Stack>
        </Paper>

      </Stack>
    </Container>
  </ThemeProvider>
);
};
export default Configuration;