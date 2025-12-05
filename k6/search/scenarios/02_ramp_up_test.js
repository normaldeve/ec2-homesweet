import { check } from "k6";
import { search } from "../utils/http_client.js";
import { pickKeyword } from "../utils/random.js";

export const options = {
    scenarios: {
        ramp_step_1: { executor: "constant-arrival-rate", rate: 300,  duration: "1m", preAllocatedVUs: 1000, maxVUs: 5000 },
        ramp_step_2: { executor: "constant-arrival-rate", rate: 700,  duration: "1m", startTime: "1m" },
        ramp_step_3: { executor: "constant-arrival-rate", rate: 1500, duration: "1m", startTime: "2m" },
        ramp_step_4: { executor: "constant-arrival-rate", rate: 3000, duration: "1m", startTime: "3m" },
        ramp_step_5: { executor: "constant-arrival-rate", rate: 5000, duration: "1m", startTime: "4m" },
        ramp_step_6: { executor: "constant-arrival-rate", rate: 10000, duration: "1m", startTime: "5m" }
    }
};

export default function () {
    const keyword = pickKeyword();
    const res = search(keyword);

    check(res, { "status 200": (r) => r.status === 200 });
}
