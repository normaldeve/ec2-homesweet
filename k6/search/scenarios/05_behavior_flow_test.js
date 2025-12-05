import { check, sleep } from "k6";
import { search } from "../utils/http_client.js";
import { pickKeyword } from "../utils/random.js";

export const options = {
    vus: 300,
    duration: "5m"
};

export default function () {
    const keyword = pickKeyword();

    // 자동완성 시뮬레이션 (3~5회)
    for (let i = 0; i < Math.floor(Math.random() * 3) + 3; i++) {
        const auto = search(keyword.substring(0, i + 1), "&autocomplete=true");
        check(auto, { "auto 200": (r) => r.status === 200 });
        sleep(Math.random() * 0.2);
    }

    // 검색 실행
    const res = search(keyword, "&size=12");
    check(res, { "search 200": (r) => r.status === 200 });
    sleep(1.2);

    // 필터 적용
    const filtered = search(keyword, "&minPrice=10000&maxPrice=90000");
    check(filtered, { "filter 200": (r) => r.status === 200 });
    sleep(1);

    // 페이징
    for (let i = 0; i < 3; i++) {
        const next = search(keyword, `&page=${i + 2}`);
        check(next, { "page 200": (r) => r.status === 200 });
        sleep(0.5);
    }
}
