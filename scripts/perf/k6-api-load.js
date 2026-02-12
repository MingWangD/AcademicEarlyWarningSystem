import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: 20,
  duration: '30s',
  thresholds: {
    http_req_duration: ['p(95)<800'],
    http_req_failed: ['rate<0.05'],
  },
};

export default function () {
  const loginPayload = JSON.stringify({ username: 'teacher01', password: '123456', role: 'TEACHER' });
  const loginRes = http.post('http://localhost:9090/api/v1/auth/login', loginPayload, {
    headers: { 'Content-Type': 'application/json' },
  });

  check(loginRes, {
    'login status is 200': (r) => r.status === 200,
    'login code is 0': (r) => r.json('code') === 0,
  });

  const token = loginRes.json('data.token');
  if (token) {
    const boardRes = http.get('http://localhost:9090/api/v1/teacher/dashboard', {
      headers: { Authorization: `Bearer ${token}` },
    });
    check(boardRes, {
      'dashboard status is 200': (r) => r.status === 200,
      'dashboard payload ok': (r) => r.json('code') === 0,
    });
  }

  sleep(1);
}
