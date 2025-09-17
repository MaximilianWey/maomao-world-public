import {apiGet, apiPostTyped} from "@/services/authService.ts";
import {USER_URLS} from "@/config.ts";
import type { UserResponse } from "@/types/user.ts";


export const updateUserProfile = async (username?: string, email?: string, displayName?: string, avatarUrl?: string): Promise<UserResponse> => {
    const data: Record<string, string> = {};

    if (username) data.username = username;
    if (email) data.email = email;
    if (displayName) data.displayName = displayName;
    if (avatarUrl) data.avatarUrl = avatarUrl;

    if (Object.keys(data).length === 0) {
        throw new Error("At least one field is required to update user profile.");
    }
    const response: { userProfile: UserResponse; token: string } = await apiPostTyped(USER_URLS.updateUserProfile(), data);

    if (response.token) {
        localStorage.setItem('Authorization', `${response.token}`);
    }
    return response.userProfile;
}

export const getUserData = async (): Promise<UserResponse> => {
    const response = await apiGet(USER_URLS.me());

    if (response.status !== 200) {
        throw new Error('Failed to fetch user data');
    }

    return response.data;
};