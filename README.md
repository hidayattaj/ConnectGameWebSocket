

# Connect Multiplayer Turn-Based Game (WebSocket)

A custom-built online multiplayer turn-based game with real-time synchronization using WebSockets.  
Players can register, log in, search for opponents, and play a Connect-Four style game where turns are updated live across devices.

## Features
- **User Authentication**
  - Register, login, and logout with secure password hashing (bcrypt).
  - Session management with tokens (single active session per user).
- **Matchmaking**
  - Players can search for a game.
  - If another player is waiting, a match is created and both are notified.
- **Gameplay**
  - Turn-based mechanics with server-side validation.
  - Real-time updates via WebSocket (moves, turns, win/draw state).
  - Client UI updates instantly when the opponent plays.
- **Game State**
  - Server stores game objects in JSON (`games.json`).
  - Tracks players, moves, turn, status, and winner.
- **Frontend**
  - Android app built with Java.
  - OkHttp used for HTTP requests and WebSocket connections.
  - Interactive UI with token placement, win/draw dialogs, and turn indicators.

---

## Tech Stack
- **Backend**
  - Node.js
  - Express.js
  - WebSocket (`ws`)
  - bcrypt (password hashing)
  - File-based JSON storage
- **Frontend**
  - Android (Java)
  - OkHttp (HTTP + WebSocket)
  - Gson (JSON parsing)
