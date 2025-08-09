import { createServer } from 'https';
import { parse } from 'url';
import fs from 'fs';
import next from 'next';

const dev = process.env.NODE_ENV !== 'production';
const app = next({ dev });
const handle = app.getRequestHandler();

const httpsOptions = {
    key: fs.readFileSync('C:/certs/localhost+2-key.pem'),
    cert: fs.readFileSync('C:/certs/localhost+2.pem'),
};

app.prepare().then(() => {
    createServer(httpsOptions, (req, res) => {
        const parsedUrl = parse(req.url || '', true);
        void handle(req, res, parsedUrl)
    }).listen(3000, () => {
        console.log('> HTTPS Ready on https://localhost:3000');
    });
});
