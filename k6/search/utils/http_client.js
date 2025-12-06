import http from "k6/http";

const API_HOST = "http://43.203.103.201:8080";
const SEARCH_URL = `${API_HOST}/api/v1/search/products`;
const CATEGORY_URL = `${API_HOST}/api/v1/categories`;

const TOKEN = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIyMDAwMSIsImVtYWlsIjoianVubnVraW0xMDA3QGdtYWlsLmNvbSIsIm5hbWUiOiLquYDspIDsmrAiLCJwcm92aWRlciI6Imdvb2dsZSIsInJvbGUiOiJVU0VSIiwiaWF0IjoxNzY0OTI2NjI0LCJleHAiOjE3NjQ5NDQ2MjR9.S9zSGLKubYooY_dX74VR5oJb2auDVGktfTHvHQNwOk8Fmk36A4gx41wJBeqQbhyJI13wOc33TjC85EhTl9Q_Mw"

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
