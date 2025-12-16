import httpClient from "../http-commons";

// Inicializar montos y tarifas
const create = () => {
    return httpClient.post("/api/AmountandRates/");
};

// Actualizar montos y tarifas
const update = (data) => {
    return httpClient.put("/api/AmountandRates/update", data);
};

export default { create, update };
