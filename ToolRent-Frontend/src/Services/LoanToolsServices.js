import httpClient from "../http-commons"

const create = data => {
    return httpClient.post("/api/LoanTools/", data)

}

const getid = id => {
    return httpClient.get(`/api/LoanTools/${id}`)

}

const registerDamageandReposition = idloan => {
    return httpClient.put(`/api/LoanTools/register-damage/${idloan}`);
}


const updateTool = (iduser, idtools) => {
    return httpClient.put(`/api/LoanTools/return/${iduser}/${idtools}`);
}
const updateLoanTool = data => {
    return httpClient.put("/api/LoanTools/", data)
}

const deletes = id =>{
    return httpClient.delete(`/api/LoanTools/${id}`)
}

const getiduser = iduser => {
    return httpClient.get(`/api/LoanTools/userloantool/${iduser}`)
}

const calculateLateFee = idloan => {
    return httpClient.get(`/api/LoanTools/calculate-fine/${idloan}`);
}

const checkClients = idclient => {
        return httpClient.put(`/api/LoanTools/CheckClient/${idclient}`);
}

const payAllFees = idloan => {
    return httpClient.put(`/api/LoanTools/Pay/${idloan}`);
}

export default { create, getid, updateLoanTool, updateTool, deletes, getiduser, calculateLateFee, checkClients, payAllFees, registerDamageandReposition }