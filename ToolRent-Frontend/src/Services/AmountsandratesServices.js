import httpClient from "../http-commons";

// Inicializar montos y tarifas
const create = () => {
    return httpClient.post("/api/AmountandRates/");
};

// Actualizar montos y tarifas
const update = (data) => {
    return httpClient.put("/api/AmountandRates/update", data);
};

const getall = () => {
    return httpClient.get("/api/AmountandRates/");
}

export default { create, update, getall };
