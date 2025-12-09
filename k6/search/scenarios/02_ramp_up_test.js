import { check } from "k6";
import { search } from "../utils/http_client.js";
import { pickKeyword } from "../utils/random.js";

export const options = {
    scenarios: {
        user_load: {
            executor: "ramping-vus",
            startVUs: 0,
            stages: [
                // 실제 동시 접속자가 만 명일 때 평균 3초에 한 번 검색을 한다..
                { duration: "2m", target: 100 },
                { duration: "2m", target: 500 },
                { duration: "2m", target: 1000 },
                { duration: "2m", target: 3000 },
            ]
        }
    }

};

export default function () {
    const keyword = pickKeyword();
    const res = search(keyword);

    check(res, { "status 200": (r) => r.status === 200 });
}
