const csv_info = require('./csv_info.js');
const express = require('express');
const axios = require('axios');
const cors = require('cors');

//server
const port = 4560;
const addrr = "0.0.0.0";

const app = express();
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({
  extended: true
}));

app.post('/', (req, res) => {
  console.log(req.body);

  var separator = csv_info.getSeparator(req.body["separator"]);
  csv_info.getHeadersList("/usr/src/app/user_datasets/", req.body["filename"], separator).then(
    colNames => {
      var data = {
        "doc": {
          datasetColumns: colNames
        }
      }
      updateDataset(data, req.body["id"]).then(data => {
        res.set('Content-Type', "application/json");
        res.status(202).send({ "status": "received" });
      });
    }
  );

  csv_info.getNoOfCSVLines(`/usr/src/app/user_datasets/${req.body["filename"]}`).then(
    noOfLines => {
      var data = {
        "doc": {
          datasetTotalRecords: noOfLines
        }
      }
      updateDataset(data, req.body["id"]);
    }
  );
});

app.listen(port, addrr, () => {
  console.log(`Datatransfer server listening at http://localhost:${port}`)
});

//elastic info

const elstasticUrl = "http://elasticsearch:9200";
const datasetsIndex = "datasets_info";
const reqHeaders = {
  "Content-Type": "application/json",
  "Authorization": "Basic ZWxhc3RpYzpjaGFuZ2VtZQ=="
};


//functions

function updateDataset(data, datasetId) {
  return new Promise((resolve, reject) => {
    var body = JSON.stringify(data);

    var config = {
      method: 'post',
      url: `${elstasticUrl}/${datasetsIndex}/_doc/${datasetId}/_update?refresh=wait_for&retry_on_conflict=6`,
      headers: reqHeaders,
      data: body
    };

    axios(config)
      .then(function (response) {
        resolve();
      })
      .catch(function (error) {
        resolve();
        console.log(error);
      });
  });
}