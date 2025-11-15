const express = require('express');
const bcrypt = require('bcrypt');
const fs = require('fs');
const path = require('path');
const { authenticate, removeSession, addSession } = require('./auth');


const app = express();
app.use(express.json());

const usersFile = path.join(__dirname, '/files/users.json');


function loadUsers() {
    if (!fs.existsSync(usersFile)) return [];
    return JSON.parse(fs.readFileSync(usersFile));
}
function saveUsers(users) { fs.writeFileSync(usersFile, JSON.stringify(users, null, 2)); }



// Registration
app.post('/register', async (req, res) => {
    const { Email, Password } = req.body;

    if (!Email || !Password) { return res.status(400).send('Email and password required'); }

    const users = loadUsers();
    if (users.find(u => u.email === Email)) { return res.status(400).send('User already exists'); }

    const hashedPassword = await bcrypt.hash(Password, 10);
    users.push({ email: Email, password: hashedPassword });
    saveUsers(users);

    res.json({ message: 'User registered successfully' });
});


// Login
app.post('/login', async (req, res) => {
    const { Email, Password } = req.body;

    const users = loadUsers();

    const user = users.find(u => u.email === Email);

    if (!user) { return res.status(400).send('User not found'); }

    const passwordMatches = await bcrypt.compare(Password, user.password);
    if (!passwordMatches) { return res.status(400).send('Invalid credentials'); }

    const sessionId = Math.random().toString(36).substring(2);
    addSession(sessionId, Email);

    res.json({ message: 'Login successful', sessionId });
});


// Protected Logout route
app.post('/logout', authenticate, (req, res) => {
    const token = req.headers['authorization'].split(' ')[1];
    removeSession(token);
    res.json({ message: 'Logged out successfully' });
});



app.listen(5000, '0.0.0.0', () => {
    console.log('Server running on http://0.0.0.0:5000');
});
