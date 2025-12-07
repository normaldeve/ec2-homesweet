import http from "k6/http";

const API_HOST = "http://3.36.120.38:8080";
const SEARCH_URL = `${API_HOST}/api/v1/search/products`;
const CATEGORY_URL = `${API_HOST}/api/v1/categories`;

export function getTopCategories() {
    return http.get(`${CATEGORY_URL}/top`);
}

export function search(keyword, params = "") {
    return http.get(`${SEARCH_URL}?keyword=${encodeURIComponent(keyword)}${params}`);
}
