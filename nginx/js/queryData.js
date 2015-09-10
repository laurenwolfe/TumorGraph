//Query path and string
var rexsterURL = "http://glados49:8182/graphs/tumorgraph/";
var gremlin = "tp/gremlin?script=";

var rootNodeID;
var vertices = [];
var edges = [];
var ids = [];
var edgeIDs = [];
var unparsedEdges = [];

function passedQuery(q) {
    var url = rexsterURL + gremlin + q;
    firstQuery(url, buildChart);
}

function firstQuery(q, callback) {
    $.ajax({
        type: "GET",
        url: q,
        dataType: "json",
        contentType: "application/json",
        success: function (json) {
            if(json) {
                resultHandler(json.results, callback);
            } else {
                alert("Bad request");
            }
        },
        error: function(jqXHR, textStatus, errorThrown) {
            alert(errorThrown);
        }
    });
}

function queryRexster(query, callback) {
    $.ajax({
        type: "GET",
        url: rexsterURL + query,
        dataType: "json",
        contentType: "application/json",
        success: function (json) {
            parseData(json.results, callback);
            callback();
        },
        error: function(jqXHR, textStatus, errorThrown) {
            alert(errorThrown);
        }
    });

}

function resultHandler(json, callback) {
    var count = 0;

    for(entry in json) count++;

    if(count > 0) {

        var type = json[0]._type;

        if (count === 1 && type === "vertex") {
            rootNodeID = json[0]._id;
            parseData(json);
            getNodes(callback);
        } else if (type === "vertex") {
            parseData(json);
            checkForEdges();
            //parseData
            //buildChart
        } else if (type === "edge") {
            //parseData
            //createNodeIDSet
            //getNodes
            //parseData
            //buildChart
        }

    } else {
        alert("Query returned no results");
    }

}

function parseData(data) {
    $.each(data, function (i, item) {
        var dataItem;

        if (item._type === 'vertex' && ids.indexOf(item._id) === -1) {
            ids.push(item._id);
            dataItem = createVertex(item);
            vertices.push(dataItem);
        } else if (item._type === 'edge' && edgeIDs.indexOf(item._id) === -1) {
            edgeIDs.push(item.edgeID);
            dataItem = createEdge(item);
            edges.push(dataItem);
        } else {
            console.log("Error 1");
        }
    });
}

function createVertex(item){
    var node = {
        id: item._id,
        objectID: item.objectID,
        name: item.name,
        chr: item.chr,
        start: item.start,
        end: item.end,
        strand: item.strand,
        tumor_type: item.tumor_type,
        version: item.version,
        feature_type: item.feature_type,
        annotation: item.annotation
    };

    return node;
}

function createEdge(item){
    var edge = {
        source: item._inV,
        target: item._outV,
        edgeID: item._id,
        correlation: item.correlation,
        sample_size: item.sample_size,
        min_log_p_uncorrected: item.min_log_p_uncorrected,
        bonferroni: item.bonferroni,
        min_log_p_corrected: item.min_log_p_corrected,
        excluded_sample_count_a: item.excluded_sample_count_a,
        min_log_p_unused_a: item.min_log_p_unused_a,
        excluded_sample_count_b: item.excluded_sample_count_b,
        min_log_p_unused_b: item.min_log_p_unused_b,
        genomic_distance: item.genomic_distance,
        feature_types: item.feature_types
    };
    return edge;
}

function checkForEdges() {

    var ids2 = ids;
    var query;
    var length;

    var currId = ids2.shift();
    var bool;


    while(ids2.length > 1) {
        length = ids2.length;

        for (var i = 0; i < length; i++) {
            query = "g.v(" + currId + ").both.retain([g.v(" + ids2[i] + ")]).hasNext()";

            if(edgeQueries(rexsterURL + gremlin + query)) {
                console.log("true!");
                query = "g.v(" + id + ").bothE.as('x').bothV.retain([g.v(" + id2 + ")]).back('x')";

                edge = edgeQueries(rexsterURL + gremlin + query);
                unparsedEdges.push(edge);
            } else {
                console.log("false!");
            }

        }
        currId = ids2.shift();
    }
}

function edgeQueries(q) {
    $.ajax({
        type: "GET",
        url: q,
        dataType: "json",
        contentType: "application/json",
        success: function (json) {
            return json;
        }
    });

}

/*
 This is the path for a single vertex query.
 */
function getNodes(callback) {
    var query = "vertices/" + rootNodeID + "/both";

    return queryRexster(query, function() {
        getEdges(callback);
    });
}

function getEdges(callback) {
    var query = "vertices/" + rootNodeID + "/bothE";

    return queryRexster(query, function() {
        buildGraphJSON(callback);
    });
}




function buildGraphJSON(callback) {
    graphJSON = {
        "nodes": vertices,
        "edges": edges
    };

    callback();
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