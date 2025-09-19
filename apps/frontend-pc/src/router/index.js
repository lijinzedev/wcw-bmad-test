import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import LoginView from '../views/LoginView.vue'
import AdminView from '../views/AdminView.vue'
import AdminUsersView from '../views/AdminUsersView.vue'
import AdminRolesView from '../views/AdminRolesView.vue'
import RisksListView from '../views/risk/RisksListView.vue'
import RiskFormView from '../views/risk/RiskFormView.vue'
import HazardsBoardView from '../views/hazard/HazardsBoardView.vue'
import HazardDetailView from '../views/hazard/HazardDetailView.vue'
import BehaviorBoardView from '../views/behavior/BehaviorBoardView.vue'
import BehaviorFormView from '../views/behavior/BehaviorFormView.vue'
import InspectionBoardView from '../views/inspection/InspectionBoardView.vue'
import InspectionFormView from '../views/inspection/InspectionFormView.vue'
import InspectionDetailView from '../views/inspection/InspectionDetailView.vue'
import { auth } from '../services/auth'
import GovernmentLoginView from '../views/government/GovernmentLoginView.vue'
import GovernmentDashboardView from '../views/government/GovernmentDashboardView.vue'
import DisciplineDashboardView from '../views/discipline/DisciplineDashboardView.vue'
import DisciplineRulesView from '../views/discipline/DisciplineRulesView.vue'
import AssessmentDashboardView from '../views/analysis/AssessmentDashboardView.vue'
import AssessmentConfigView from '../views/analysis/AssessmentConfigView.vue'
import AccidentDashboardView from '../views/analysis/AccidentDashboardView.vue'
import AccidentFormView from '../views/analysis/AccidentFormView.vue'
import BenchmarkCompareView from '../views/analysis/BenchmarkCompareView.vue'
import ManagementCockpitView from '../views/analysis/ManagementCockpitView.vue'
import AlertAnalyticsView from '../views/analysis/AlertAnalyticsView.vue'
import HealthManagementView from '../views/health/HealthManagementView.vue'
import MonitoringIntegrationView from '../views/monitoring/MonitoringIntegrationView.vue'
import AlertsBoardView from '../views/monitoring/AlertsBoardView.vue'
import NotificationSettingsView from '../views/notification/NotificationSettingsView.vue'
import KbArticlesView from '../views/kb/KbArticlesView.vue'
import SlaSettingsView from '../views/monitoring/SlaSettingsView.vue'
import SlaReportView from '../views/monitoring/SlaReportView.vue'

const routes = [
  { path: '/', name: 'home', component: HomeView, meta: { requiresAuth: true } },
  { path: '/login', name: 'login', component: LoginView },
  { path: '/gov-login', name: 'gov-login', component: GovernmentLoginView },
  { path: '/gov', name: 'gov-dashboard', component: GovernmentDashboardView, meta: { requiresAuth: true, roles: ['GOVERNMENT'] } },
  { path: '/admin', name: 'admin', component: AdminView, meta: { requiresAuth: true, roles: ['ADMIN'] } },
  { path: '/admin/users', name: 'admin-users', component: AdminUsersView, meta: { requiresAuth: true, roles: ['ADMIN'] } },
  { path: '/admin/roles', name: 'admin-roles', component: AdminRolesView, meta: { requiresAuth: true, roles: ['ADMIN'] } },
  { path: '/risks', name: 'risks', component: RisksListView, meta: { requiresAuth: true } },
  { path: '/risks/new', name: 'risk-create', component: RiskFormView, meta: { requiresAuth: true, roles: ['ADMIN'] } },
  { path: '/risks/:id', name: 'risk-edit', component: RiskFormView, meta: { requiresAuth: true, roles: ['ADMIN'] } },
  { path: '/hazards', name: 'hazards-board', component: HazardsBoardView, meta: { requiresAuth: true, roles: ['ADMIN'] } },
  { path: '/hazards/:id', name: 'hazard-detail', component: HazardDetailView, meta: { requiresAuth: true, roles: ['ADMIN'] } },
  { path: '/behaviors', name: 'behavior-board', component: BehaviorBoardView, meta: { requiresAuth: true, roles: ['ADMIN'] } },
  { path: '/behaviors/new', name: 'behavior-create', component: BehaviorFormView, meta: { requiresAuth: true, roles: ['ADMIN'] } },
  { path: '/behaviors/:id', name: 'behavior-edit', component: BehaviorFormView, meta: { requiresAuth: true, roles: ['ADMIN'] } },
  { path: '/inspections', name: 'inspection-board', component: InspectionBoardView, meta: { requiresAuth: true, roles: ['ADMIN'] } },
  { path: '/inspections/new', name: 'inspection-create', component: InspectionFormView, meta: { requiresAuth: true, roles: ['ADMIN'] } },
  { path: '/inspections/:id', name: 'inspection-detail', component: InspectionDetailView, meta: { requiresAuth: true, roles: ['ADMIN'] } },
  { path: '/inspections/:id/edit', name: 'inspection-edit', component: InspectionFormView, meta: { requiresAuth: true, roles: ['ADMIN'] } },
  { path: '/discipline', name: 'discipline-dashboard', component: DisciplineDashboardView, meta: { requiresAuth: true, roles: ['ADMIN'] } },
  { path: '/discipline/rules', name: 'discipline-rules', component: DisciplineRulesView, meta: { requiresAuth: true, roles: ['ADMIN'] } },
  { path: '/analysis/assessments', name: 'analysis-dashboard', component: AssessmentDashboardView, meta: { requiresAuth: true, roles: ['ADMIN'] } },
  { path: '/analysis/assessments/config', name: 'analysis-config', component: AssessmentConfigView, meta: { requiresAuth: true, roles: ['ADMIN'] } },
  { path: '/analysis/accidents', name: 'accident-dashboard', component: AccidentDashboardView, meta: { requiresAuth: true, roles: ['ADMIN'] } },
  { path: '/analysis/accidents/new', name: 'accident-create', component: AccidentFormView, meta: { requiresAuth: true, roles: ['ADMIN'] } },
  { path: '/analysis/accidents/:id', name: 'accident-edit', component: AccidentFormView, meta: { requiresAuth: true, roles: ['ADMIN'] } },
  { path: '/analysis/benchmarks', name: 'benchmark-compare', component: BenchmarkCompareView, meta: { requiresAuth: true, roles: ['ADMIN'] } },
  { path: '/analysis/alerts', name: 'alert-analytics', component: AlertAnalyticsView, meta: { requiresAuth: true, roles: ['ADMIN'] } },
  { path: '/analysis/dashboard', name: 'management-cockpit', component: ManagementCockpitView, meta: { requiresAuth: true, roles: ['ADMIN'] } },
  { path: '/health', name: 'health-management', component: HealthManagementView, meta: { requiresAuth: true, roles: ['ADMIN'] } },
  { path: '/monitoring', name: 'monitoring-integration', component: MonitoringIntegrationView, meta: { requiresAuth: true, roles: ['ADMIN'] } },
  { path: '/alerts', name: 'alerts-board', component: AlertsBoardView, meta: { requiresAuth: true, roles: ['ADMIN'] } },
  { path: '/admin/notifications', name: 'notification-settings', component: NotificationSettingsView, meta: { requiresAuth: true, roles: ['ADMIN'] } },
  { path: '/admin/kb', name: 'kb-articles', component: KbArticlesView, meta: { requiresAuth: true, roles: ['ADMIN'] } },
  { path: '/admin/sla', name: 'sla-settings', component: SlaSettingsView, meta: { requiresAuth: true, roles: ['ADMIN'] } },
  { path: '/analysis/sla', name: 'sla-report', component: SlaReportView, meta: { requiresAuth: true, roles: ['ADMIN'] } }
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

router.beforeEach((to, from, next) => {
  const isAuthed = auth.isAuthenticated()
  if (to.meta.requiresAuth && !isAuthed) {
    return next({ name: 'login', query: { redirect: to.fullPath } })
  }
  if ((to.name === 'login' || to.name === 'gov-login') && isAuthed) {
    return next({ name: 'home' })
  }
  const requiredRoles = to.meta.roles
  if (requiredRoles && requiredRoles.length > 0) {
    const roles = auth.getRoles()
    const has = roles.some(r => requiredRoles.includes(r) || requiredRoles.includes(r.replace('ROLE_', '')))
    if (!has) {
      return next({ name: 'home' })
    }
  }
  next()
})

export default router
