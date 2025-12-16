
import httpClient from "../http-commons"

const create = data => {
    return httpClient.post("/api/statetools", data)

}

const getid = id => {
    return httpClient.get(`/api/statetools/${id}`)
}

const update = data => {
    return httpClient.put("/api/statetools/", data)
}

const deleteid = id => {
    return httpClient.delete(`/api/statetools/${id}`)
}

export default { create, getid, update, deleteid}