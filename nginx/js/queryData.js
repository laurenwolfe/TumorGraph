//Query path and string
//var restURL = "http://glados49:8080/query/index?query=";
//var restURL = "http://192.168.99.100:8182/graphs/tumorgraph/tp/gremlin?script=";
//var restURL = "http://192.168.99.100:8080/query/index?query=";
var restURL = "http://glados49:8080/query/index?query=";

function passedQuery(q) {

    var http_request = new XMLHttpRequest();


    http_request.onreadystatechange = function(){

        if (http_request.readyState == 4  ){
            // Javascript function JSON.parse to parse JSON data

            var json = JSON.parse(http_request.responseText);

            buildChart(json);
        }
    };

    http_request.open("GET", restURL + q, true);
    http_request.send();
}

function buildChart(json) {

    var config = {
        dataSource: json,
        linkDistance: function(){ return 10; },
//        graphHeight: function(){ return 600; },
//        graphWidth: function(){ return 800; },
        nodeCaption: "objectID",
        nodeCaptionsOnByDefault: true,
        nodeTypes: {
            // gene, gene, gene, protein, methylation, miRNA
            feature_type: ["GEXB", "GNAB", "CNVR", "RPPA", "METH", "MIRN"]
        },
        showControlDash: true,
        showStats: true,
        nodeStats: true,
        forceLocked: false,
        zoomControls: true,
        nodeFilters: true,
        showFilters: true,
        nodeStyle: {
            "all": {
                "borderColor": "#B5B5B5"
            },
            "GEXB": {
                "color": "#004953"
            },
            "GNAB": {
                "color": "#004953"
            },
            "CNVR": {
                "color": "#004953"
            },
            "RPPA": {
                "color": "#800080"
            },
            "METH": {
                "color": "#32b835"
            },
            "MIRN": {
                "color": "#f08080"
            }
        }/*,
        nodeMouseOver: function(node) {
            return node.name + node.annotation;
        }*/
    };

    alchemy.begin(config);
}