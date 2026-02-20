/**
 * Main App Component
 * @author Moon Myung-seop
 */

import { useEffect } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ThemeProvider } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import { useAuthStore } from './stores/authStore';
import { useThemeStore } from './stores/themeStore';
import { createMesTheme } from './themes/themeConfig';

// Import i18n configuration (must be imported before components that use translations)
import './i18n/config';

// Common Components
import PWAInstallPrompt from './components/common/PWAInstallPrompt';

// Pages
import LoginPage from './pages/LoginPage';
import DashboardLayout from './components/layout/DashboardLayout';
import Dashboard from './pages/Dashboard';
import OverviewDashboard from './pages/OverviewDashboard';
import UsersPage from './pages/UsersPage';
import RolesPage from './pages/RolesPage';
import PermissionsPage from './pages/PermissionsPage';
import AuditLogsPage from './pages/AuditLogsPage';
import ThemesPage from './pages/ThemesPage';

// Production Management Pages
import ProductsPage from './pages/admin/ProductsPage';
import ProcessesPage from './pages/admin/ProcessesPage';
import WorkOrdersPage from './pages/production/WorkOrdersPage';
import WorkResultsPage from './pages/production/WorkResultsPage';

// Quality Management Pages
import QualityStandardsPage from './pages/quality/QualityStandardsPage';
import QualityInspectionsPage from './pages/quality/QualityInspectionsPage';

// Inventory Management Pages
import WarehousesPage from './pages/inventory/WarehousesPage';
import LotsPage from './pages/inventory/LotsPage';
import InventoryPage from './pages/inventory/InventoryPage';
import InventoryTransactionsPage from './pages/inventory/InventoryTransactionsPage';

// BOM Management Pages
import BomsPage from './pages/bom/BomsPage';

// Process Routing Management Pages
import ProcessRoutingsPage from './pages/routing/ProcessRoutingsPage';

// Production Schedule Pages
import ProductionSchedulePage from './pages/schedule/ProductionSchedulePage';

// Business Management Pages
import CustomersPage from './pages/business/CustomersPage';
import SuppliersPage from './pages/business/SuppliersPage';

// Material Management Pages
import MaterialsPage from './pages/material/MaterialsPage';

// Purchase Management Pages
import PurchaseOrdersPage from './pages/purchase/PurchaseOrdersPage';

// Sales Management Pages
import SalesOrdersPage from './pages/sales/SalesOrdersPage';
import DeliveriesPage from './pages/sales/DeliveriesPage';

// Common Management Pages
import SitesPage from './pages/common/SitesPage';
import DepartmentsPage from './pages/common/DepartmentsPage';
import EmployeesPage from './pages/common/EmployeesPage';

// Warehouse Operations Pages
import ReceivingPage from './pages/warehouse/ReceivingPage';
import ShippingPage from './pages/warehouse/ShippingPage';
import IQCRequestsPage from './pages/warehouse/IQCRequestsPage';
import OQCRequestsPage from './pages/warehouse/OQCRequestsPage';
import MaterialRequestsPage from './pages/warehouse/MaterialRequestsPage';
import MaterialHandoversPage from './pages/warehouse/MaterialHandoversPage';
import ReturnsPage from './pages/warehouse/ReturnsPage';
import DisposalsPage from './pages/warehouse/DisposalsPage';

// WMS Pages
import WeighingsPage from './pages/wms/WeighingsPage';

// Defect Management Pages
import DefectsPage from './pages/defect/DefectsPage';
import AfterSalesPage from './pages/defect/AfterSalesPage';
import ClaimsPage from './pages/defect/ClaimsPage';

// Equipment Management Pages
import EquipmentsPage from './pages/equipment/EquipmentsPage';
import EquipmentOperationsPage from './pages/equipment/EquipmentOperationsPage';
import EquipmentInspectionsPage from './pages/equipment/EquipmentInspectionsPage';

// FMS Extension Pages
import InspectionFormsPage from './pages/equipment/InspectionFormsPage';
import InspectionPlansPage from './pages/equipment/InspectionPlansPage';
import GaugesPage from './pages/equipment/GaugesPage';
import ConsumablesPage from './pages/equipment/ConsumablesPage';
import EquipmentPartsPage from './pages/equipment/EquipmentPartsPage';
import BreakdownsPage from './pages/equipment/BreakdownsPage';
import BreakdownStatisticsPage from './pages/equipment/BreakdownStatisticsPage';
import DeviationsPage from './pages/equipment/DeviationsPage';
import ExternalCalibrationsPage from './pages/equipment/ExternalCalibrationsPage';

// Downtime Management Pages
import DowntimesPage from './pages/downtime/DowntimesPage';

// Mold Management Pages
import MoldsPage from './pages/mold/MoldsPage';
import MoldMaintenancesPage from './pages/mold/MoldMaintenancesPage';
import MoldProductionHistoriesPage from './pages/mold/MoldProductionHistoriesPage';

// HR Management Pages
import SkillMatrixPage from './pages/hr/SkillMatrixPage';
import EmployeeSkillsPage from './pages/hr/EmployeeSkillsPage';

// Analytics Pages
import AnalyticsDashboardPage from './pages/analytics/AnalyticsDashboardPage';
import StatisticalReportsPage from './pages/analytics/StatisticalReportsPage';

// Admin Pages
import CommonCodesPage from './pages/admin/CommonCodesPage';

// Common Management Extension Pages
import HolidaysPage from './pages/common/HolidaysPage';
import ApprovalPage from './pages/common/ApprovalPage';
import AlarmPage from './pages/common/AlarmPage';
import SOPsPage from './pages/common/SOPsPage';

// POP (Point of Production) Pages
import POPLayout from './components/pop/POPLayout';
import POPHomePage from './pages/pop/POPHomePage';
import POPWorkOrderPage from './pages/pop/POPWorkOrderPage';
import POPScannerPage from './pages/pop/POPScannerPage';
import POPSOPPage from './pages/pop/POPSOPPage';
import POPPerformancePage from './pages/pop/POPPerformancePage';
import POPWorkProgressPage from './pages/pop/POPWorkProgressPage';

// Mobile Pages
import MobileInventoryListPage from './pages/mobile/MobileInventoryListPage';
import MobileInventoryCheckPage from './pages/mobile/MobileInventoryCheckPage';

// Protected Route Component
const ProtectedRoute = ({ children }: { children: React.ReactNode }) => {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return <>{children}</>;
};

function App() {
  const initialize = useAuthStore((state) => state.initialize);
  const currentTheme = useThemeStore((state) => state.currentTheme);
  const mode = useThemeStore((state) => state.mode);

  // Initialize auth state on app load
  useEffect(() => {
    initialize();
  }, [initialize]);

  // Create MUI theme based on current theme and mode
  const theme = createMesTheme(currentTheme, mode);

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <PWAInstallPrompt />
      <BrowserRouter>
        <Routes>
          {/* Public Routes */}
          <Route path="/login" element={<LoginPage />} />

          {/* Protected Routes */}
          <Route
            path="/"
            element={
              <ProtectedRoute>
                <DashboardLayout />
              </ProtectedRoute>
            }
          >
            <Route index element={<Dashboard />} />
            <Route path="overview" element={<OverviewDashboard />} />
            <Route path="users" element={<UsersPage />} />
            <Route path="roles" element={<RolesPage />} />
            <Route path="permissions" element={<PermissionsPage />} />
            <Route path="audit-logs" element={<AuditLogsPage />} />
            <Route path="themes" element={<ThemesPage />} />

            {/* Production Management Routes */}
            <Route path="production/products" element={<ProductsPage />} />
            <Route path="production/processes" element={<ProcessesPage />} />
            <Route path="production/work-orders" element={<WorkOrdersPage />} />
            <Route path="production/work-results" element={<WorkResultsPage />} />
            <Route path="production/schedules" element={<ProductionSchedulePage />} />

            {/* Quality Management Routes */}
            <Route path="quality/standards" element={<QualityStandardsPage />} />
            <Route path="quality/inspections" element={<QualityInspectionsPage />} />

            {/* Inventory Management Routes */}
            <Route path="inventory/warehouses" element={<WarehousesPage />} />
            <Route path="inventory/lots" element={<LotsPage />} />
            <Route path="inventory/status" element={<InventoryPage />} />
            <Route path="inventory/transactions" element={<InventoryTransactionsPage />} />

            {/* BOM Management Routes */}
            <Route path="bom/boms" element={<BomsPage />} />

            {/* Process Routing Management Routes */}
            <Route path="routing/routings" element={<ProcessRoutingsPage />} />

            {/* Business Management Routes */}
            <Route path="business/customers" element={<CustomersPage />} />
            <Route path="business/suppliers" element={<SuppliersPage />} />

            {/* Material Management Routes */}
            <Route path="material/materials" element={<MaterialsPage />} />

            {/* Purchase Management Routes */}
            <Route path="purchase/orders" element={<PurchaseOrdersPage />} />

            {/* Sales Management Routes */}
            <Route path="sales/orders" element={<SalesOrdersPage />} />
            <Route path="sales/deliveries" element={<DeliveriesPage />} />

            {/* Common Management Routes */}
            <Route path="common/sites" element={<SitesPage />} />
            <Route path="common/departments" element={<DepartmentsPage />} />
            <Route path="common/employees" element={<EmployeesPage />} />
            <Route path="common/common-codes" element={<CommonCodesPage />} />
            <Route path="common/holidays" element={<HolidaysPage />} />
            <Route path="common/approvals" element={<ApprovalPage />} />
            <Route path="common/alarms" element={<AlarmPage />} />
            <Route path="common/sops" element={<SOPsPage />} />

            {/* Warehouse Operations Routes */}
            <Route path="warehouse/receiving" element={<ReceivingPage />} />
            <Route path="warehouse/shipping" element={<ShippingPage />} />
            <Route path="warehouse/iqc-requests" element={<IQCRequestsPage />} />
            <Route path="warehouse/oqc-requests" element={<OQCRequestsPage />} />
            <Route path="warehouse/material-requests" element={<MaterialRequestsPage />} />
            <Route path="warehouse/material-handovers" element={<MaterialHandoversPage />} />
            <Route path="warehouse/returns" element={<ReturnsPage />} />
            <Route path="warehouse/disposals" element={<DisposalsPage />} />
            <Route path="warehouse/weighings" element={<WeighingsPage />} />

            {/* Defect Management Routes */}
            <Route path="defect/defects" element={<DefectsPage />} />
            <Route path="defect/after-sales" element={<AfterSalesPage />} />
            <Route path="defect/claims" element={<ClaimsPage />} />

            {/* Equipment Management Routes */}
            <Route path="equipment/equipments" element={<EquipmentsPage />} />
            <Route path="equipment/operations" element={<EquipmentOperationsPage />} />
            <Route path="equipment/inspections" element={<EquipmentInspectionsPage />} />
            <Route path="equipment/inspection-forms" element={<InspectionFormsPage />} />
            <Route path="equipment/inspection-plans" element={<InspectionPlansPage />} />
            <Route path="equipment/gauges" element={<GaugesPage />} />
            <Route path="equipment/consumables" element={<ConsumablesPage />} />
            <Route path="equipment/parts" element={<EquipmentPartsPage />} />
            <Route path="equipment/breakdowns" element={<BreakdownsPage />} />
            <Route path="equipment/breakdown-statistics" element={<BreakdownStatisticsPage />} />
            <Route path="equipment/deviations" element={<DeviationsPage />} />
            <Route path="equipment/external-calibrations" element={<ExternalCalibrationsPage />} />

            {/* Downtime Management Routes */}
            <Route path="downtime/downtimes" element={<DowntimesPage />} />

            {/* Mold Management Routes */}
            <Route path="mold/molds" element={<MoldsPage />} />
            <Route path="mold/maintenances" element={<MoldMaintenancesPage />} />
            <Route path="mold/production-histories" element={<MoldProductionHistoriesPage />} />

            {/* HR Management Routes */}
            <Route path="hr/skill-matrix" element={<SkillMatrixPage />} />
            <Route path="hr/employee-skills" element={<EmployeeSkillsPage />} />

            {/* Analytics Routes */}
            <Route path="analytics/dashboard" element={<AnalyticsDashboardPage />} />
            <Route path="analytics/reports" element={<StatisticalReportsPage />} />
          </Route>

          {/* POP (Point of Production) Routes - Separate layout for field workers */}
          <Route
            path="/pop"
            element={
              <ProtectedRoute>
                <POPLayout />
              </ProtectedRoute>
            }
          >
            <Route index element={<POPHomePage />} />
            <Route path="work-orders" element={<POPWorkOrderPage />} />
            <Route path="scanner" element={<POPScannerPage />} />
            <Route path="sop" element={<POPSOPPage />} />
            <Route path="performance" element={<POPPerformancePage />} />
            <Route path="work-progress" element={<POPWorkProgressPage />} />
          </Route>

          {/* Mobile Routes */}
          <Route
            path="/mobile"
            element={
              <ProtectedRoute>
                <MobileInventoryListPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/mobile/inventory-check"
            element={
              <ProtectedRoute>
                <MobileInventoryCheckPage />
              </ProtectedRoute>
            }
          />

          {/* Catch all - redirect to home */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </ThemeProvider>
  );
}

export default App;
