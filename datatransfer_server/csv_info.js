const { exec } = require('child_process');
var firstline = require('firstline');

//resolve returns either the number of lines(without counting the header) or undefined
function getNoOfCSVLines(filename) {
    return new Promise((resolve, reject) => {
        exec(`wc -l ${filename}`, (err, stdout, stderr) => {
            if (err) {
                console.log(`stderr: ${stderr.replace('\n', "")}`);
                resolve();
                return;
            }
            console.log(`stdout: ${stdout.replace('\n', "")}`);
            var totalNoOfLines = stdout.split(" ")[0];
            resolve(totalNoOfLines - 1);
        })
    });
}

function getHeadersList(path, filename, separator) {
    return new Promise((resolve, reject) => {
        firstline(path + filename).then(
            function (results) {
                headers = results.split(separator);
                for (let index = 0; index < headers.length; index++) {
                    headers[index] = headers[index].trim();                    
                }
                resolve(headers);
            }
        );
    });
}

function getSeparator(separator) {
    return separator == "tab" ? "\t" : separator;
}

module.exports = {
    getNoOfCSVLines: getNoOfCSVLines,
    getHeadersList: getHeadersList,
    getSeparator: getSeparator
};