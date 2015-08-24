
//If you are using NodeJS as your server, just run this file 'node server.js'

//It depends on the express module which can be installed as a global module (once node is installed) by running
// >  npm -g install express

// If you can't run Node or express just use a simple python server
//    
//   > python -m SimpleHTTPServer 8080
//

// Helper to get the major version of ExpressJS
function getVersion(){
  // Since Express 3.3 version is not exposed any longer
  var version = (express.version || '4.').match(/^(\d)+\./)[1];
  return Number(version);
}

var express = require('express');

var app;

var version = getVersion();

// Depending on the version number there are several ways to create an app
if(version === 2){
  app = express.createServer();
} else if(version > 2){
  app = express();
}

app.use('/', express["static"].apply(null, [__dirname + '/']));

app.listen(8080);

console.log('Server running. Browse to http://localhost:8080/index.htm');
