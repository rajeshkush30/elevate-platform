import { Routes, Route, Navigate } from 'react-router-dom';
import { Box, CircularProgress, Typography } from '@mui/material';
import { useAuth } from '../context/AuthContext';
import Login from '../pages/Login';
import Register from '../pages/Register';
import Dashboard from '../pages/Dashboard';
import ProtectedRoute from './ProtectedRoute';
import PublicRoute from './PublicRoute';
import ForgotPassword from '../pages/ForgotPassword';
import ResetPassword from '../pages/ResetPassword';
import VerifyEmail from '../pages/VerifyEmail';
import Questionnaire from '../pages/Questionnaire';
import Training from '../pages/Training';
import Strategy from '../pages/Strategy';
import Consultation from '../pages/Consultation';
import AdminDashboard from '../pages/AdminDashboard';
import AdminSegments from '../pages/AdminSegments';
import AdminQuestions from '../pages/AdminQuestions';
import AdminClients from '../pages/AdminClients';
import AdminLayout from '../layouts/AdminLayout';
import AdminSubmissions from '../pages/AdminSubmissions';
import AdminSettings from '../pages/AdminSettings';
import AdminStageRules from '../pages/AdminStageRules';
import AssessmentHistory from '../pages/AssessmentHistory';
import AdminAssignmentsCreate from '../pages/AdminAssignmentsCreate';
import AdminAssignmentsList from '../pages/AdminAssignmentsList';
import AdminCatalog from '../pages/AdminCatalog';
import AdminAssessmentWizard from '../pages/AdminAssessmentWizard';
import AdminQuestionnaires from '../pages/AdminQuestionnaires';
import AdminQuestionsManager from '../pages/AdminQuestionsManager';
import MyAssessments from '../pages/MyAssessments';
import ClientAssessmentFill from '../pages/ClientAssessmentFill';
import AssessmentResult from '../pages/AssessmentResult';
import AdminAIPrompts from '../pages/AdminAIPrompts';
import Chat from '../pages/Chat';

const AppContent = () => {
  const { loading } = useAuth();

  if (loading) {
    return (
      <Box
        display="flex"
        flexDirection="column"
        justifyContent="center"
        alignItems="center"
        minHeight="100vh"
        gap={2}
        sx={{ backgroundColor: '#f5f5f5', padding: '20px', textAlign: 'center' }}
      >
        <CircularProgress />
        <Typography variant="body1" color="textSecondary">
          Loading application...
        </Typography>
      </Box>
    );
  }

  return (
    <Routes>
      <Route path="/" element={<HomeRouter />} />

      <Route path="/login" element={<PublicRoute><Login /></PublicRoute>} />
      <Route path="/register" element={<PublicRoute><Register /></PublicRoute>} />
      <Route path="/forgot-password" element={<PublicRoute><ForgotPassword /></PublicRoute>} />
      <Route path="/reset-password" element={<ResetPassword />} />
      <Route path="/verify-email" element={<VerifyEmail />} />
      {/* Chat should be accessible to everyone; do not wrap in PublicRoute */}
      <Route path="/chat" element={<Chat />} />

      <Route path="/dashboard/*" element={<ProtectedRoute><Dashboard /></ProtectedRoute>} />
      <Route path="/my-assessments" element={<ProtectedRoute><MyAssessments /></ProtectedRoute>} />
      <Route path="/client/assessments/:clientAssessmentId" element={<ProtectedRoute><ClientAssessmentFill /></ProtectedRoute>} />

      <Route path="/admin/*" element={<ProtectedRoute roles={["ADMIN"]}><AdminLayout /></ProtectedRoute>}>
        <Route index element={<AdminDashboard />} />
        <Route path="clients" element={<AdminClients />} />
        <Route path="segments" element={<AdminSegments />} />
        <Route path="questions" element={<AdminQuestions />} />
        <Route path="stage-rules" element={<AdminStageRules />} />
        <Route path="ai-prompts" element={<AdminAIPrompts />} />
        <Route path="assignments" element={<AdminAssignmentsList />} />
        <Route path="assignments/create" element={<AdminAssignmentsCreate />} />
        <Route path="catalog" element={<AdminCatalog />} />
        <Route path="questionnaires" element={<AdminQuestionnaires />} />
        <Route path="questionnaires/:questionnaireId/questions" element={<AdminQuestionsManager />} />
        <Route path="assessment-wizard" element={<AdminAssessmentWizard />} />
        <Route path="submissions" element={<AdminSubmissions />} />
        <Route path="settings" element={<AdminSettings />} />
      </Route>

      <Route path="/questionnaire" element={<ProtectedRoute><Questionnaire /></ProtectedRoute>} />
      <Route path="/assessment/result/:clientAssessmentId" element={<ProtectedRoute><AssessmentResult /></ProtectedRoute>} />
      <Route path="/assessment/history" element={<ProtectedRoute><AssessmentHistory /></ProtectedRoute>} />
      <Route path="/training" element={<ProtectedRoute><Training /></ProtectedRoute>} />
      <Route path="/strategy" element={<ProtectedRoute><Strategy /></ProtectedRoute>} />
      <Route path="/consult" element={<ProtectedRoute><Consultation /></ProtectedRoute>} />
    </Routes>
  );
};

// Decides landing route based on current user's role
const HomeRouter = () => {
  const { user } = useAuth();
  if (!user) return <Navigate to="/chat" replace />;
  return <Navigate to={user.role === 'ADMIN' ? '/admin' : '/dashboard'} replace />;
};

export default AppContent;
