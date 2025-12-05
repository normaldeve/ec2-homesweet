import { check, sleep } from "k6";
import { search } from "../utils/http_client.js";
import { pickKeyword } from "../utils/random.js";

export const options = {
    scenarios: {
        soak: {
            executor: "constant-arrival-rate",
            rate: 600,
            duration: "1h",
            preAllocatedVUs: 1200,
            maxVUs: 3000
        }
    }
};

export default function () {
    const keyword = pickKeyword();
    const res = search(keyword);

    check(res, { "status 200": (r) => r.status === 200 });

    sleep(1); // 장기 테스트에서는 think time 필수
}
