
# Note

The following is a snapshot from a private microservice based project I started working on early 2025.
It is mostly abandoned and planned to be replaced by a new microservice-based full stack application where I want to practice React and integrating Services like AWS and Azure.


---

# 🐾 MaoMao World

A personal digital playground and project hub for experimenting with web tech, games, finance tools, and Discord integrations — all themed around the maomaocloud universe.

---

## 🚧 Todo

- [x] Implement CI/CD pipelines for all services

### 🛂 Auth Service (`maomaoworld-auth`)
- [x] Integrate OIDC (Authentik)
- [x] Discord account linking
- [ ] Overhaul of existing user management
- [ ] User roles and permissions
- [ ] User settings management
- [x] Implement Authentication methods:
  - [x] OAuth2 (Discord)
  - [x] OIDC (Authentik)
- [x] Add Authentication provider capability levels
  - [x] User source
  - [x] Login only
  - [x] Link only
- [ ] Implement Authentication provider management:
  - [ ] Add new providers
  - [ ] Remove existing providers
  - [ ] Configure provider settings
- [ ] Implement user management:
  - [ ] Create new users
  - [ ] Update existing users
  - [ ] Delete users
  - [ ] View user details

### 🗃️ Database (`maomaoworld-db`)
- [x] PostgreSQL setup and schema

### 💬 Forums (`maomaoworld-forum`)
- [ ] Implement forum service - _possiby using Ruby on Rails_
- [ ] Add bug tracker
- [ ] Add feature requests
- [ ] Add discussion boards
- [ ] Add search functionality
- [ ] Add user roles and permissions
- [ ] Add user settings management
- [ ] Add user authentication and authorization
- [ ] Add user notifications
- [ ] Add user activity feed
- [ ] Add user mentions and tagging
- [ ] Add user polls and surveys

### 💸 Finance Tracker (`maomaoworld-finance-service`)
- [ ] Expense tracking API
- [ ] Monthly summaries + graphs

### 🎮 Games (`maomao-games`)
- [ ] Trivia (`maomao-trivia`) – _possibly Ruby on Rails_
- [ ] Cards Against Humanity (`maomao-cah`) – _TBD_

### ⏱️ Time Tracker (`maomaoworld-time-service`)
- [ ] REST API for managing tracked sessions - _possibly Ruby on Rails_
- [ ] Discord bot interface - _feature to be added to the Music Bot_
- [ ] Simple web frontend for overview and edits
- [ ] Export to:
    - [ ] Markdown (weekly format, similar to existing text docs)
    - [ ] ICS calendar files (for calendar imports)
- [ ] Weekly/ Monthly summaries and total hours

### 🎵 Music Bot (`maomaoworld-music`)
- [x] Java Discord bot for music playback
- [x] Connect to Lavalink server for audio streaming
- [x] Commands for:
    - [x] Play/Skip/Stop
    - [x] Loop [song/queue]
    - [x] Queue management
- [ ] Web frontend:
    - [x] Show currently playing track
    - [x] Display saved playlists
    - [x] Add songs to queue
    - [x] Skip songs
    - [x] Remove songs from queue
    - [ ] Save current queue as a playlist
    - [x] Stats overview
    - [ ] Join me button which checks whether user has permission to do so (e.g Admin, Mod, bot not in use)
- [x] Docker setup for Lavalink server hosting

### 📦 Anonymous Downloader (`maomao-downloader`)
- [ ] Basic downloader backend (via Tor SOCKS5 at `192.168.178.2:9150`)
- [ ] Accept paste-in links via web dashboard
- [ ] Save downloads to temporary local storage
- [ ] Generate ephemeral QR code per downloaded file
- [ ] Show QR to requesting user only
- [ ] Ephemeral file cleanup (e.g., delete after 24h)
- [ ] Optional: Upload to user’s Nextcloud if requested
- [ ] Admin-only dashboard:
  - [ ] View all active QR files
  - [ ] Manage active QR Files
  - [ ] Set file to permanent
  - [ ] Sort by user / download date
- [ ] JWT bearer token validation via Auth server
- [ ] Restrict access to downloader route (requires working permission system)
- [ ] Docker Compose setup for downloader backend
- [ ] Hide or disable download file listing for non-admins

### 🌐 Web Frontend (`maomaoworld-web`)
- [x] Login redirect/handler page
- [x] Dashboard for logged-in users
- [ ] Public home/landing page
- [ ] Public project showcase
- [x] Light/Dark theme toggle
- [ ] Responsive design for mobile
- [x] Users settings page
