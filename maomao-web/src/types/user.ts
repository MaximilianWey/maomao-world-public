export interface User {
    id: string;
    displayName: string;
    username: string;
    email: string;
    theme: 'light' | 'dark';
    providerId: string;
    avatarUrl: string;
    linkedAccounts: LinkedAccount[];
}

export interface UserResponse {
    userId: string;
    user: {
        id: string;
        username: string;
        passwordHash: string;
        email: string;
        createdAt: string;
        lastLoginAt: string;
        provider: string;
    };
    displayName: string;
    avatarUrl: string;
    linkedAccounts: LinkedAccount[];
}

export interface LinkedAccount {
    id: string;
    providerId: string;
    externalId: string;
    preferredName: string;
    avatarUrl?: string | null;
    email?: string | null;
    linkedAt: string;
    extraData: Record<string, unknown>;
}

export interface DiscordUser {
    username: string;
    uniqueName: string;
    avatarUrl: string;
    id: string;
}