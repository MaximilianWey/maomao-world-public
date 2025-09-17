import {apiGet, apiGetTyped} from './authService'
import {AUTH_URLS} from '@/config'
import type {UserResponse} from "@/types/user.ts";

export const fetchAuthProviders = async () => {
    return await apiGet(AUTH_URLS.providers())
}

export const loginWithOidcProvider = async (providerId: string) => {
    const redirectUri = getRedirectUri();
    window.location.href = AUTH_URLS.oidcLogin(providerId, redirectUri);
}

export const linkOidcProvider = async (providerId: string) => {
    const redirectUri = getRedirectUri();
    const res = await apiGet(AUTH_URLS.oidcLink(providerId, redirectUri));
    if (res && res.data && res.data.redirectUrl) {
        window.location.href = res.data.redirectUrl;
    } else {
        console.error('No redirect URL received from API');
    }
};

export const loginWithOauth2Provider = async (providerId: string) => {
    const redirectUri = getRedirectUri();
    window.location.href = AUTH_URLS.oauth2Login(providerId, redirectUri);
}

export const linkOauth2Provider = async (providerId: string) => {
    const redirectUri = getRedirectUri();
    const res = await apiGet(AUTH_URLS.oauth2Link(providerId, redirectUri));
    if (res && res.data && res.data.redirectUrl) {
        window.location.href = res.data.redirectUrl;
    } else {
        console.error('No redirect URL received from API');
    }
};

export const loginWithSamlProvider = async (providerId: string) => {
    const redirectUri = getRedirectUri();
    window.location.href = AUTH_URLS.samlLogin(providerId, redirectUri);
}

export const disconnectLinkedAccount = async (providerId: string): Promise<UserResponse> => {
    const res = await apiGetTyped<{ token: string; userProfile: UserResponse }>(
        AUTH_URLS.disconnectLinkedAccount(providerId)
    );

    const { token, userProfile } = res;

    if (token) {
        localStorage.setItem("Authorization", token);
        console.log('Successfully disconnected account, new token set:', token);
    } else {
        console.error('No token received after disconnecting account. Logging out...');
        localStorage.removeItem("Authorization");
    }

    return userProfile;
};

function getRedirectUri(): string {
    const origin = window.location.origin;
    const path = window.location.pathname;
    const redirectUri = `${origin}${path}`;
    console.log('Setting redirect_uri to:', redirectUri);
    return redirectUri;
}

export function handleTokenRedirect(): string | null {
    const url = new URL(window.location.href);
    const token = url.searchParams.get("token");

    if (token) {
        localStorage.setItem("Authorization", `${token}`);
        url.searchParams.delete("token");
        const cleanUrl = url.pathname + url.search + url.hash;
        window.history.replaceState({}, document.title, cleanUrl);
        return token;
    }
    return null;
}