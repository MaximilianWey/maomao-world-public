import axios, {type AxiosResponse} from 'axios';

const api = axios.create();

api.interceptors.request.use(config => {
    const token = getAuthToken();
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

api.interceptors.response.use(
    response => response,
        error => {
            const isPublic = error.config?.url?.includes('login') || error.config?.url?.includes('/api/public');
            const isAuthEndpoint = error.config?.url?.includes('/api/authentication/') || error.config?.url?.includes('/api/user');
            if (!isPublic && isAuthEndpoint && error.response.status === 401) {
                clearAuthToken();
                window.location.href = '/login';
            }
            return Promise.reject(error);
})

// --- SIMPLE API CALL FUNCTIONS ---
export const apiGet = (url: string) => api.get(url);
export const apiPost = (url: string, data: any) => api.post(url, data);
export const apiDelete = (url: string) => api.delete(url);

// --- GENERIC API CALL FUNCTIONS ---
export const apiGetTyped = async <T = any>(url: string): Promise<T> => {
    const response: AxiosResponse<T> = await api.get<T>(url);
    return response.data;
};

export const apiPostTyped = async <T = any, D = any>(url: string, data: D): Promise<T> => {
    const response: AxiosResponse<T> = await api.post<T>(url, data);
    return response.data;
};

export const apiDeleteTyped = async <T = any>(url: string): Promise<T> => {
    const response: AxiosResponse<T> = await api.delete<T>(url);
    return response.data;
};

// --- HELPER FUNCTIONS ---
export function clearAuthToken() { localStorage.removeItem('Authorization'); }
export function getAuthToken(): string | null {
    return localStorage.getItem('Authorization');
}
export function getCookie(name: string): string | null {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) {
        return parts.pop()!.split(';').shift()!;
    }
    return null;
}