//Query path and string
var restURL = "http://glados49:8080/query/index?query=";

function passedQuery(q) {
    $.ajax({
        type: "GET",
        url: restURL + q,
        dataType: "json",
        contentType: "application/json",
        success: function (json) {
            if(json) {
                buildChart(json);
            } else {
                alert("Bad request");
            }
        },
        error: function(jqXHR, textStatus, errorThrown) {
            alert(errorThrown);
        }
    });
}

function buildChart() {
    var config = {
        dataSource: graphJSON,
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
        }
    };

    alchemy.begin(config);
}