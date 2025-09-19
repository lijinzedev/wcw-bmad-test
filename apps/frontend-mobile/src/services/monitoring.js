import { request } from './api.js'

export function fetchMyAlerts(params = {}) {
  return request({ url: '/api/v1/monitoring/my-alerts', method: 'GET', data: params })
}

export function countMyAlerts() {
  return request({ url: '/api/v1/monitoring/my-alerts/count', method: 'GET' })
}

export function ackMyAlert(id, acknowledged) {
  return request({ url: `/api/v1/monitoring/my-alerts/${id}/ack`, method: 'PATCH', data: { acknowledged }, headers: { 'Content-Type': 'application/json' } })
}
