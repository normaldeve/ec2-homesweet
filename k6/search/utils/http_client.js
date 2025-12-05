import http from "k6/http";

const API_HOST = "http://43.203.103.201:8080";
const SEARCH_URL = `${API_HOST}/api/v1/search/products`;
const CATEGORY_URL = `${API_HOST}/api/v1/categories`;

const TOKEN = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIyMDAwMSIsImVtYWlsIjoianVubnVraW0xMDA3QGdtYWlsLmNvbSIsIm5hbWUiOiLquYDspIDsmrAiLCJwcm92aWRlciI6Imdvb2dsZSIsInJvbGUiOiJVU0VSIiwiaWF0IjoxNzY0OTA2MjEyLCJleHAiOjE3NjQ5MjQyMTJ9.6IEWY58Uz5xlJVu8OuFzsLiY2AiwDyAgwGnDyvzneea6-HhD8wIbXB4o9WMCEjcn3ms4JRWzd9bud0CMbM52Og";

export function getTopCategories() {
    return http.get(`${CATEGORY_URL}/top`);
}

export function search(keyword, params = "") {
    return http.get(
        `${SEARCH_URL}?keyword=${encodeURIComponent(keyword)}${params}`,
        {
            headers: {
                "Authorization": `Bearer ${TOKEN}`
            }
        }
    );
}
