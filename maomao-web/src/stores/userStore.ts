import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import router from '@/router.ts';
import { getUserData, updateUserProfile } from '@/services/userService.ts';
import { useAuthStore } from "@/stores/authStore.ts";
import type { User, UserResponse, LinkedAccount } from '@/types/user.ts';
import placeholderAvatar from '@/assets/placeholderAvatar.svg';
import { disconnectLinkedAccount } from "@/services/authProviderService.ts";

export const useUserStore = defineStore('user', () => {
    const authStore = useAuthStore();
    const currentUser = ref<User | null>(null);
    const isLoading = ref(false);
    const error = ref<string | null>(null);

    const isAuthenticated = computed(() => !!currentUser.value);
    const isAdmin = computed(() => false); // TODO: replace with actual permission check
    const userTheme = computed(() => currentUser.value?.theme || 'light');
    const username = computed(() => currentUser.value?.username || '');
    const displayName = computed(() => currentUser.value?.displayName || '');
    const avatarUrl = computed(() => currentUser.value?.avatarUrl || placeholderAvatar);
    const linkedAccounts = computed(() => currentUser.value?.linkedAccounts || []);
    const discordAccount = computed<LinkedAccount | undefined>(() => {
        const discordProvider = authStore.getProviderByName("discord");
        if (!discordProvider) return undefined;
        return linkedAccounts.value.find(account =>
            account.providerId === discordProvider.id
        );
    });
    const hasDiscordLinked = computed(() => !!discordAccount.value);

    const fetchUserData = async () => {
        isLoading.value = true;
        error.value = null;

        try {
            const userData = await getUserData();
            currentUser.value = mapUserResponseToUser(userData);
        } catch (err: any) {
            if (err.message?.toLowerCase().includes('unauthorized')) {
                error.value = 'Unauthorized access. Logging out.';
                clearUser();
                localStorage.removeItem('Authorization');
                await router.push('/login');
            } else {
                console.error('Failed to fetch user data:', err);
                error.value = 'Failed to load user data';
            }
        } finally {
            isLoading.value = false;
        }
    };

    const updateProfile = async (username?: string, email?: string, displayName?: string, avatarUrl?: string) => {
        if (!currentUser.value) throw new Error('No user is currently logged in.');

        try {
            const updatedUser = await updateUserProfile(username, email, displayName, avatarUrl);
            currentUser.value = mapUserResponseToUser(updatedUser);
        } catch (err: any) {
            console.error('Failed to update user profile:', err);
            throw new Error('Failed to update user profile');
        }
    };

    const clearUser = () => {
        currentUser.value = null;
        error.value = null;
    };

    const disconnectAccount = async (providerId: string) => {
        try {
            const profile = await disconnectLinkedAccount(providerId);
            if (profile) {
                currentUser.value = mapUserResponseToUser(profile);
            } else {
                console.warn('No profile returned after disconnecting account');
            }
        } catch (err: any) {
            console.error('Failed to disconnect linked account:', err);
            throw new Error('Failed to disconnect linked account');
        }
    }

    return {
        currentUser,
        isLoading,
        error,
        isAuthenticated,
        isAdmin,
        userTheme,
        username,
        displayName,
        avatarUrl,
        linkedAccounts,
        discordAccount,
        hasDiscordLinked,
        fetchUserData,
        updateProfile,
        clearUser,
        disconnectAccount
    };
}, {
    persist: true,
});

function mapUserResponseToUser(response: UserResponse): User {
    console.log('Mapping user response:', response);
    return {
        id: response.userId,
        displayName: response.displayName,
        username: response.user.username,
        email: response.user.email,
        avatarUrl: response.avatarUrl,
        providerId: response.user.provider,
        linkedAccounts: response.linkedAccounts,
        theme: (localStorage.getItem('userTheme') as 'light' | 'dark') || 'light'
    };
}