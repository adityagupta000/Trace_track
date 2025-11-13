// src/config/api.js
export const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

export const API_ENDPOINTS = {
  // Auth endpoints
  LOGIN: '/api/auth/login',
  REGISTER: '/api/auth/register',
  LOGOUT: '/api/auth/logout',
  REFRESH: '/api/auth/refresh',
  VALIDATE: '/api/auth/validate',
  ME: '/api/auth/me',

  // Items endpoints
  ITEMS: '/items',
  ITEM_BY_ID: (id) => `/items/${id}`,

  // Claims endpoints
  CLAIMS: '/claims',
  CLAIM_ITEM: (id) => `/claims/item/${id}`,
  CLAIM_BY_ID: (id) => `/claims/${id}`,

  // Messages endpoints
  MESSAGES: '/messages',
  MESSAGES_SENT: '/messages/sent',
  REPLY: '/messages/reply',
  MESSAGE_BY_ID: (id) => `/messages/${id}`,

  // Feedback endpoint
  FEEDBACK: '/feedback',

  // Dashboard endpoint
  DASHBOARD: '/dashboard',

  // Admin endpoints
  ADMIN_DASHBOARD: '/admin/dashboard',
  ADMIN_ITEMS: '/admin/items',
  ADMIN_CLAIMS: '/admin/claims',
  ADMIN_USERS: '/admin/users',
  ADMIN_FEEDBACK: '/admin/feedback',
  ADMIN_DELETE_ITEM: (id) => `/admin/items/${id}`,
  ADMIN_DELETE_CLAIM: (id) => `/admin/claims/${id}`,
  ADMIN_DELETE_USER: (id) => `/admin/users/${id}`,
  ADMIN_DELETE_FEEDBACK: (id) => `/admin/feedback/${id}`,
};

// Helper function to make API calls with proper error handling
export const apiCall = async (endpoint, options = {}) => {
  const defaultOptions = {
    credentials: 'include', // Always include cookies
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
  };

  // Merge options
  const finalOptions = { ...defaultOptions, ...options };

  // Remove Content-Type for FormData
  if (options.body instanceof FormData) {
    delete finalOptions.headers['Content-Type'];
  }

  try {
    const response = await fetch(`${API_BASE_URL}${endpoint}`, finalOptions);

    // Handle token refresh if needed (401 Unauthorized)
    // Only attempt refresh for non-auth endpoints
    if (response.status === 401 && 
        !endpoint.includes('/api/auth/') && 
        endpoint !== API_ENDPOINTS.REFRESH) {
      
      // Try to refresh token
      try {
        const refreshResponse = await fetch(`${API_BASE_URL}${API_ENDPOINTS.REFRESH}`, {
          method: 'POST',
          credentials: 'include',
        });

        if (refreshResponse.ok) {
          // Retry original request
          return fetch(`${API_BASE_URL}${endpoint}`, finalOptions);
        } else {
          // Refresh failed, redirect to login
          window.location.href = '/';
          throw new Error('Session expired');
        }
      } catch (refreshError) {
        console.error('Token refresh failed:', refreshError);
        window.location.href = '/';
        throw new Error('Session expired');
      }
    }

    return response;
  } catch (error) {
    console.error('API call error:', error);
    throw error;
  }
};