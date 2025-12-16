import httpClient from "../http-commons";

// Obtener todos los reportes de datos
const createDataReport = data => {
    return httpClient.post("/api/DataReport/", data);
}

const  getdataReportByIdreport = idreport => {
    return httpClient.get(`/api/DataReport/${idreport}`);
}

const  getalldatareport = () =>{
    return httpClient.get(`/api/DataReport/all`);
}

export default {createDataReport, getalldatareport,  getdataReportByIdreport}