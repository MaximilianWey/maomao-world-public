import type { User } from "@/types/user.ts";
import type { Song } from "@/stores/songStore.ts";
import type { Visibility } from "@/types/visibility.ts";

export interface Playlist {
    id: string
    name: string
    creator: User
    visibility: Visibility
    songs: Song[]
    subscribers: User[]
    createdAt: string
}