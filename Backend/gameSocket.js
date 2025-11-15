const WebSocket = require('ws');
const { loadSessions } = require('./auth');
const fs = require('fs');
const path = require('path');


const gamesFile = path.join(__dirname, '/files/games.json');


function loadGames() {
  if (!fs.existsSync(gamesFile)) return { Games: {} };
  return JSON.parse(fs.readFileSync(gamesFile));
}
function saveGames(games) { fs.writeFileSync(gamesFile, JSON.stringify(games, null, 2)); }


const wss = new WebSocket.Server({ port: 8080 });


// Attach user identity from token
wss.on('connection', (ws, req) => {
  const params = new URLSearchParams(req.url.replace('/?', ''));
  const token = params.get('token');

  const sessions = loadSessions();
  const session = sessions.find(s => s.sessionId === token);

  if (!session) {
    ws.close(1008, 'Unauthorized');
    return;
  }

  ws.user = session.email;
  

  ws.on("message", (message) => {
    const data = JSON.parse(message);

    if (data.message === "search-game") { handleSearchGame(ws); }


    if (data.message === "get-gameObject") {
      const gameId = data.gameId;
      handleGetGameObject(ws, gameId);
    }


    if (data.message === "update-moveAndTurn") {
      const gameId = data.gameId;
      const previousPlayerMove = data.setPreviousMove;
      const currentPlayerTurn = data.setCurrentTurn;
      handleMoveAndTurn(ws, gameId, previousPlayerMove, currentPlayerTurn);
    }


    if (data.message === "game-drawn") { handleGameDrawn(ws, data.gameId) }


    if (data.message === "game-won") { handleGameWon(ws, data.gameId, data.winnerIs, data.lastMove); }
  });


  ws.on("close", () => {
    console.log(`Connection closed for user: ${ws.user}`);
    handleDisconnect(ws.user);
  });

});



function notifyPlayers(game, gameId) {
  wss.clients.forEach(client => {
    if (client.readyState === WebSocket.OPEN && (client.user === game.player1ID || client.user === game.player2ID)) {
      client.send(JSON.stringify({ message: "proceed-to-game", gameId }));
    }
  });
}
function handleSearchGame(ws) {

  const playerId = ws.user;
  let games = loadGames();

  const waitingGameId = Object.keys(games.Games).find(id => games.Games[id].status === "waiting for other player");

  if (waitingGameId) {

    const game = games.Games[waitingGameId];
    game.player2ID = playerId;
    game.status = "game ready";
    saveGames(games);

    // Notify both players
    notifyPlayers(game, waitingGameId);

  } else {

    const gameId = require('uuid').v4();
    games.Games[gameId] = {
      player1ID: playerId,
      player1Move: "none",
      player2ID: "none",
      player2Move: "none",
      status: "waiting for other player",
      turn: playerId,
      winner: "none"
    };
    saveGames(games);

  }

}



function handleGetGameObject(ws, gameId) {
  // Load all games from file
  const games = loadGames();

  // Find the specific game by ID
  const game = games.Games[gameId];

  if (game) {
    saveGames(games);

    ws.send(JSON.stringify({
          message: "done-gameObject",
          player1ID: game.player1ID,
          player1Move: game.player1Move,
          player2ID: game.player2ID,
          player2Move: game.player2Move,
          status: game.status,
          turn: game.turn,
          winner: game.winner
        }));

  } else {
    // Handle case where gameId is not found
    ws.send(JSON.stringify({
      type: "error",
      message: `Game with ID ${gameId} not found`
    }));
  }
}



function handleMoveAndTurn(ws, gameId, previousPlayerMove, currentPlayerTurn) {

  // Load all games from file
  const games = loadGames();

  // Find the specific game by ID
  const game = games.Games[gameId];

  if (game) {

    if (currentPlayerTurn === game.player1ID) {
      game.player2Move = previousPlayerMove;
      game.turn = currentPlayerTurn;
      game.status = "game in progress";

      game.player1Move = "not played his turn yet";
      saveGames(games);
    }

    if (currentPlayerTurn === game.player2ID) {
      game.player1Move = previousPlayerMove;
      game.turn = currentPlayerTurn;
      game.status = "game in progress";

      game.player2Move = "not played his turn yet";
      saveGames(games);
    }

    wss.clients.forEach(client => {
        if (client.readyState === WebSocket.OPEN && (client.user === game.player1ID || client.user === game.player2ID)) {

          client.send(JSON.stringify({
            message: "done-moveAndTurn",
            player1ID: game.player1ID,
            player1Move: game.player1Move,
            player2ID: game.player2ID,
            player2Move: game.player2Move,
            status: game.status,
            turn: game.turn,
            winner: game.winner
          }));

        }
      });

  } else {
    // Handle case where gameId is not found
    ws.send(JSON.stringify({
      type: "error",
      message: `Game with ID ${gameId} not found`
    }));
  }

}



function handleGameWon(ws, gameId, winnerIs, lastMove) {

  // Load all games from file
  const games = loadGames();

  // Find the specific game by ID
  const game = games.Games[gameId];

  if (game) {

    if (winnerIs === game.player1ID) {
      game.winner = game.player1ID;
      game.player1Move = lastMove;
    }

    if (winnerIs == game.player2ID) {
      game.winner = game.player2ID;
      game.player2Move = lastMove;
    }

    game.status = "game ended";
    saveGames(games);


    wss.clients.forEach(client => {
        if (client.readyState === WebSocket.OPEN && (client.user === game.player1ID || client.user === game.player2ID)) {

          client.send(JSON.stringify({
            message: "done-game-won",
            player1ID: game.player1ID,
            player1Move: game.player1Move,
            player2ID: game.player2ID,
            player2Move: game.player2Move,
            status: game.status,
            turn: game.turn,
            winner: game.winner
          }));

        }
      });


  } else {
    ws.send(JSON.stringify({
      type: "error",
      message: `Game with ID ${gameId} not found`
    }));
  }
}



function handleGameDrawn(ws, gameId) {
  // Load all games from file
  const games = loadGames();

  // Find the specific game by ID
  const game = games.Games[gameId];

  if (game) {
    game.status = "drawn";
    game.winner = "none";
    saveGames(games);


    wss.clients.forEach(client => {
        if (client.readyState === WebSocket.OPEN && (client.user === game.player1ID || client.user === game.player2ID)) {

          client.send(JSON.stringify({
            message: "done-game-drawn",
            player1ID: game.player1ID,
            player1Move: game.player1Move,
            player2ID: game.player2ID,
            player2Move: game.player2Move,
            status: game.status,
            turn: game.turn,
            winner: game.winner
          }));

        }
    });

  } else {
    ws.send(JSON.stringify({
      type: "error",
      message: `Game with ID ${gameId} not found`
    }));
  }
}



// Handle disconnect cleanup
function handleDisconnect(playerId) {
  
  let games = loadGames();

  // Find any game where this player is involved AND status is "in progress"
  const gameId = Object.keys(games.Games).find(id => {
    const g = games.Games[id];
    return (g.status === "game in progress") && (g.player1ID === playerId || g.player2ID === playerId);
  });

  if (gameId) {
    const game = games.Games[gameId];
    game.status = "abandoned";
    game.winner = (game.player1ID === playerId) ? game.player2ID : game.player1ID;
    saveGames(games);

     wss.clients.forEach(client => {
        if (client.readyState === WebSocket.OPEN && (client.user === game.player1ID || client.user === game.player2ID)) {

          client.send(JSON.stringify({
            message: "opponent-abandoned-game",
            player1ID: game.player1ID,
            player1Move: game.player1Move,
            player2ID: game.player2ID,
            player2Move: game.player2Move,
            status: game.status,
            turn: game.turn,
            winner: game.winner
          }));

        }
    });

  }

}