//Query path and string
var rexsterURL = "http://glados49:8182/graphs/tumorgraph/";
var gremlin = "tp/gremlin?script=";

function passedQuery(q) {
    var url = rexsterURL + gremlin + q;
    queryGremlin(url);
}

function queryGremlin(q) {
    $.ajax({
        type: "POST",
        url: q,
        dataType: "json",
        crossDomain: true,
        contentType: "application/json",
        success: function (json) {
            console.log(json);
//            callback();
        }
    });
}