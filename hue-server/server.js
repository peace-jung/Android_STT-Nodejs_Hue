const config = [
  { command: '파티', sat: 254, bri: 150, hue: 42000 },
  { command: '우울', sat: 60, bri: 120, hue: 10000 },
  { command: '책', sat: 170, bri: 200, hue: 15000 },
  { command: '독서', sat: 170, bri: 200, hue: 15000 },
  { command: '빨간', sat: 254, bri: 254, hue: 65535 },
  { command: '빨강', sat: 254, bri: 254, hue: 65535 },
  { command: '주황', sat: 254, bri: 254, hue: 14000 },
  { command: '노랑', sat: 254, bri: 254, hue: 23000 },
  { command: '노란', sat: 254, bri: 254, hue: 23000 },
  { command: '초록', sat: 254, bri: 254, hue: 26500 },
  { command: '파랑', sat: 254, bri: 254, hue: 47000 },
  { command: '파란', sat: 254, bri: 254, hue: 47000 },
  { command: '보라', sat: 254, bri: 254, hue: 49000 },
  { command: '흐림', sat: 254, bri: 254, hue: 30000 }
];
const express = require('express');
const http = require('http');
const bodyParser = require('body-parser');
const mysql = require('mysql');
const Hue = require('philips-hue');
var hue = new Hue();

hue.bridge = "192.168.0.3";
hue.username = "-GqY0afViorHrXVY2KY1Q4qHg9WUG5vluAd-aOSC";


const app = express();

// Body-parser MiddleWare
app.use(bodyParser.urlencoded({ extended: true }));

app.post("/", (req, res) => {
  req.on('data', (data) => {
    var buf = new Buffer(data);
    var str = buf.toString('utf-8');
    var i;
    console.log('입력됨: ' + str);

    searchLedData(str);
  });
  res.send('ok');
});

// search LED Data
function searchLedData(str) {
  if (str == '메세지') {
    console.log(hue.light(2).lights);
    hue.light(2).setState({ effect: "none", alert: "lselect", sat: 254, bri: 254, hue: 26500 }, function () {
      hue.light(2).setState({alert: "lselect", sat: 254, bri: 254, hue: 1000});
    });
    return;
  }

  for (let i in config) {
    //hue.light(2).setState({effect: "none", alert: "none"});

    // 일치하는 데이터가 있을 시
    if (str.indexOf(config[i].command) >= 0) {
      console.log('matching... ' + config[i].command);
      hue.light(2).setState({ effect: "none", bri: config[i].bri, sat: config[i].sat, hue: config[i].hue });

      if (i == 0) {
        //파티모드
        hue.light(2).setState({ effect: "colorloop", transitiontime: 1 });
        return;
      }
      return;
    }

    //일치하는 데이터가 없을 시
    if (i == config.length - 1) {
      if (str.indexOf('켜') >= 0) {
        // 불켜기
        hue.light(2).on();
        hue.light(2).setState({ effect: "none" });
        console.log('불켜기');
        return;
      } else if (str.indexOf('꺼') >= 0) {
        // 불끄기
        hue.light(2).setState({ effect: "none" });
        hue.light(2).off();
        console.log('불끄기');
        return;
      }
      console.log('not matching...');
      return;
    }
  }
}


// Server On
http.createServer(app).listen(1234, () => {
  console.log('Server running at port 1234...');
});
