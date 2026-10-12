// =====================================================================
// GymErp API 부하 테스트 (k6)
//
// 사용법:
//   k6 run -e SCENARIO=member  -e LABEL=before perf/k6/api.js
//   k6 run -e SCENARIO=ptlog_deep -e VUS=20 -e DURATION=30s perf/k6/api.js
//
// SCENARIO
//   member        GET /v1/member                       회원 목록(+통계)
//   member_detail GET /v1/member/{memNum}              회원 상세(+통계)
//   dashboard     GET /v1/home                         대시보드 KPI
//   ptlog_first   GET /v1/log/pt/paged (첫 페이지)
//   ptlog_deep    GET /v1/log/pt/paged (뒤쪽 페이지, offset 900,000)
//   ptlog_deep_cursor GET /v1/log/pt/paged?cursor=… (같은 위치를 키셋 페이징으로)
//   voucher_first GET /v1/log/voucher/paged (첫 페이지)
//   voucher_deep  GET /v1/log/voucher/paged (뒤쪽 페이지, offset 250,000)
//
// 결과는 perf/results/{SCENARIO}-{LABEL}.json 에 요약 저장
// =====================================================================
import http from 'k6/http';
import { check } from 'k6';

const BASE = __ENV.BASE_URL || 'http://localhost:9000';
const SCENARIO = __ENV.SCENARIO || 'member';
const LABEL = __ENV.LABEL || 'run';

export const options = {
  vus: Number(__ENV.VUS || 10),
  duration: __ENV.DURATION || '30s',
  summaryTrendStats: ['avg', 'min', 'med', 'p(90)', 'p(95)', 'p(99)', 'max'],
  // 개선 전 API 는 수 초 이상 걸릴 수 있어 타임아웃을 넉넉히
  thresholds: { http_req_failed: ['rate<0.01'] },
};

const PATHS = {
  member: () => '/v1/member',
  member_detail: () => `/v1/member/${1 + Math.floor(Math.random() * 70000)}`,
  dashboard: () => '/v1/home',
  ptlog_first: () => '/v1/log/pt/paged?page=1&limit=20',
  ptlog_deep: () => '/v1/log/pt/paged?page=45001&limit=20',
  // 위 ptlog_deep 과 같은 위치(offset 900,000 = usageId 100,000 부터)를 키셋으로 조회
  ptlog_deep_cursor: () => '/v1/log/pt/paged?cursor=100001&limit=20',
  voucher_first: () => '/v1/log/voucher/paged?page=1&limit=20',
  voucher_deep: () => '/v1/log/voucher/paged?page=12501&limit=20',
};

export function setup() {
  const res = http.post(`${BASE}/v1/emp/login`,
    JSON.stringify({ empEmail: 'admin@gym.local', password: 'perf1234' }),
    { headers: { 'Content-Type': 'application/json' } });
  check(res, { 'login 200': (r) => r.status === 200 });
  const cookie = res.cookies.JSESSIONID && res.cookies.JSESSIONID[0];
  if (!cookie) throw new Error(`로그인 실패: ${res.status} ${res.body}`);
  return { session: cookie.value };
}

export default function (data) {
  const path = PATHS[SCENARIO]();
  const res = http.get(`${BASE}${path}`, {
    cookies: { JSESSIONID: data.session },
    timeout: '120s',
    tags: { scenario: SCENARIO },
  });
  check(res, { 'status 200': (r) => r.status === 200 });
}

export function handleSummary(data) {
  const d = data.metrics.http_req_duration.values;
  const summary = {
    scenario: SCENARIO,
    label: LABEL,
    vus: options.vus,
    duration: options.duration,
    requests: data.metrics.http_reqs.values.count,
    rps: Number(data.metrics.http_reqs.values.rate.toFixed(2)),
    failRate: data.metrics.http_req_failed.values.rate,
    latencyMs: {
      avg: Number(d.avg.toFixed(1)),
      med: Number(d.med.toFixed(1)),
      p95: Number(d['p(95)'].toFixed(1)),
      p99: Number(d['p(99)'].toFixed(1)),
      max: Number(d.max.toFixed(1)),
    },
  };
  return {
    [`perf/results/${SCENARIO}-${LABEL}.json`]: JSON.stringify(summary, null, 2),
    stdout: `\n[${SCENARIO} / ${LABEL}] reqs=${summary.requests} rps=${summary.rps} ` +
            `avg=${summary.latencyMs.avg}ms p95=${summary.latencyMs.p95}ms fail=${summary.failRate}\n`,
  };
}
