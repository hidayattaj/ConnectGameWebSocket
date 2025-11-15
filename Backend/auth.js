const fs = require('fs');
const path = require('path');

const sessionsFile = path.join(__dirname, '/files/sessions.json');


function loadSessions() {
    if (!fs.existsSync(sessionsFile)) return [];
    return JSON.parse(fs.readFileSync(sessionsFile));
}
function saveSessions(sessions) {
    fs.writeFileSync(sessionsFile, JSON.stringify(sessions, null, 2));
}


function authenticate(req, res, next) {
    const authHeader = req.headers['authorization'];
    if (!authHeader) { return res.status(401).send('No token provided'); }

    const parts = authHeader.split(' ');
    if (parts[0] !== 'Bearer' || !parts[1]) {
        return res.status(400).send('Invalid authorization format');
    }

    const token = parts[1];
    const sessions = loadSessions();
    const session = sessions.find(s => s.sessionId === token);

    if (!session) { return res.status(401).send('Unauthorized'); }

    req.user = session.email;
    next();
}


function addSession(sessionId, email) {
    // this code useful if user is logged into multiple sessions phone, pc etc
    // =================================
    //let sessions = loadSessions();
    //sessions.push({ sessionId, email });
    //saveSessions(sessions);
    // =================================

    // only ONE session per user
    let sessions = loadSessions();
    // Remove old sessions for this email
    sessions = sessions.filter(s => s.email !== email);
    // Add new one
    sessions.push({ sessionId, email });
    saveSessions(sessions);
}


function removeSession(sessionId) {
    let sessions = loadSessions();
    sessions = sessions.filter(s => s.sessionId !== sessionId);
    saveSessions(sessions);
}

module.exports = { authenticate, addSession, removeSession, saveSessions, loadSessions };
