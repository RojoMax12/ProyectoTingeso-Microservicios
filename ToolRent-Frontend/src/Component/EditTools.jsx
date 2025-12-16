import React, { useState } from "react";
import {
  Box, Typography, Paper, TextField, Button, Stack,
  IconButton, Drawer, List, ListItem, ListItemButton,
  ListItemIcon, ListItemText, CssBaseline
} from "@mui/material";
import { useNavigate, useParams } from "react-router-dom";
import MenuIcon from "@mui/icons-material/Menu";

import HomeIcon from "@mui/icons-material/Home";
import BuildIcon from "@mui/icons-material/Build";
import LibraryAddIcon from "@mui/icons-material/LibraryAdd";
import AssessmentIcon from "@mui/icons-material/Assessment";
import ContactsIcon from "@mui/icons-material/Contacts";
import PersonAddAltIcon from '@mui/icons-material/PersonAddAlt';
import AdminPanelSettingsIcon from "@mui/icons-material/AdminPanelSettings";
import ReportIcon from '@mui/icons-material/Report';

import ToolServices from "../Services/ToolServices";
import { ThemeProvider, createTheme } from "@mui/material/styles";
import { useKeycloak } from "@react-keycloak/web";


/* ---------------------- TEMA GLOBAL ---------------------- */
const theme = createTheme({
  palette: {
    background: { default: "#FEF3E2" },
    primary: { main: "rgba(255, 94, 0, 1)" },
  }
});


/* ---------------------- COMPONENTE PRINCIPAL ---------------------- */
const EditTools = () => {
  const [toolName, setToolName] = useState("");
  const [toolCategory, setToolCategory] = useState("");
  const [toolRemplacementCost, setToolRemplacementCost] = useState("");

  const { keycloak } = useKeycloak();
  const navigate = useNavigate();
  const { id } = useParams();
  const [drawerOpen, setDrawerOpen] = useState(false);

  const isAdmin = keycloak?.tokenParsed?.realm_access?.roles?.includes("ADMIN");


  /* ---------------------- SIDEBAR ---------------------- */
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


  /* ---------------------- GUARDAR CAMBIOS ---------------------- */
  const handleSave = () => {
    ToolServices.update({
      id: id,
      name: toolName,
      category: toolCategory,
      replacement_cost: toolRemplacementCost
    })
      .then(() => {
        alert("Herramienta actualizada correctamente");
        navigate("/ToolList");
      })
      .catch((error) => {
        console.error(error);
        alert("Error al actualizar herramienta");
      });
  };


  return (
    <ThemeProvider theme={theme}>
      <CssBaseline /> {/* ACTIVA BACKGROUND DEL TEMA */}

      {/* BOTÓN MENU */}
      <IconButton
        onClick={() => setDrawerOpen(true)}
        sx={{
          position: "fixed",
          top: 16,
          left: 16,
          zIndex: 10,
          backgroundColor: "rgba(255, 94, 0, 1)",
          '&:hover': { backgroundColor: "rgba(255, 94, 0, 1)" }
        }}
      >
        <MenuIcon sx={{ color: "#fff" }} />
      </IconButton>


      {/* SIDEBAR */}
      <Drawer
        open={drawerOpen}
        onClose={() => setDrawerOpen(false)}
        variant="temporary"
        sx={{
          [`& .MuiDrawer-paper`]: {
            width: 240,
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
                  borderRadius: 1,
                  mx: 1,
                  my: 0.5,
                  '&:hover': {
                    backgroundColor: 'rgba(255, 94, 0, 0.1)',
                  }
                }}
              >
                <ListItemIcon sx={{ color: "rgba(255, 94, 0, 1)" }}>
                  {option.icon}
                </ListItemIcon>
                <ListItemText
                  primary={option.text}
                  sx={{ '& .MuiListItemText-primary': { fontWeight: 500 } }}
                />
              </ListItemButton>
            </ListItem>
          ))}
        </List>
      </Drawer>


      {/* CONTENIDO PRINCIPAL */}
      <Box sx={{ minHeight: "100vh", py: 6 }}>
        <Box sx={{ maxWidth: 520, mx: "auto" }}>
          <Typography
            variant="h4"
            align="center"
            sx={{
              mb: 3,
              fontWeight: "bold",
              color: "rgba(255, 94, 0, 1)"
            }}
          >
            Editar Herramienta
          </Typography>

          <Paper
            sx={{
              p: 4,
              borderRadius: 3,
              boxShadow: "0 4px 16px rgba(255,94,0,0.25)"
            }}
          > 
            {/*Formulario */}
            <Stack spacing={3}>
              <TextField
                label="Nombre de la Herramienta"
                value={toolName}
                onChange={(e) => setToolName(e.target.value)}
                fullWidth
              />

              <TextField
                label="Categoría"
                value={toolCategory}
                onChange={(e) => setToolCategory(e.target.value)}
                fullWidth
              />

              <TextField
                label="Costo de Reemplazo"
                type="number"
                value={toolRemplacementCost}
                onChange={(e) => setToolRemplacementCost(e.target.value)}
                fullWidth
              />

              <Button
                variant="contained"
                sx={{
                  backgroundColor: "rgba(255, 94, 0, 1)",
                  py: 1.5,
                  fontSize: "1rem",
                  fontWeight: "bold"
                }}
                onClick={handleSave}
              >
                Guardar Cambios
              </Button>

              <Button
                variant="outlined"
                sx={{
                  borderColor: "rgba(255, 94, 0, 1)",
                  color: "rgba(255, 94, 0, 1)"
                }}
                onClick={() => navigate("/ToolList")}
              >
                Cancelar
              </Button>
            </Stack>
          </Paper>
        </Box>
      </Box>

    </ThemeProvider>
  );
};


export default EditTools;
