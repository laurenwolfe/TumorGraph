//Query path and string
var rexsterURL = "http://glados49:8182/graphs/tumorgraph/";

var id = '322560';
var key = "objectID";
var val = "stad:Gene:TG";

var graphJSON;
var vertices = [];
var edges = [];

$(window).load(function() {
    if(id) {
        getStartingVertex(id, buildChart);
    } else if(key && val) {
        getVertexID(key, val, buildChart);
    } else {
        //Choose a random default location to start the chart from.
    }
});

function queryRexster(query, cb) {
    $.ajax({
        type: "GET",
        url: rexsterURL + query,
        dataType: "json",
        contentType: "application/json",
        success: function (json) {
            ParseJSONData(json.results);
            cb();
        }
    });
}

function queryVertexId(query, cb) {
    $.ajax({
        type: "GET",
        url: rexsterURL + query,
        dataType: "json",
        contentType: "application/json",
        success: function (json) {
            cb(json.results._id);
        }
    });
}

function ParseJSONData(items) {
    $.each(items, function (i, item) {
        var dataItem;

        if(item._type === 'vertex') {
            dataItem = createVertex(item);
            vertices.push(dataItem);
        } else {
            dataItem = createEdge(item);
            edges.push(dataItem);
        }
    });
}

function createVertex(item){
    var node = {
        id: item._id,
        objectID: item.objectID,
        start: item.start,
        name: item.name,
        strand: item.strand,
        end: item.end,
        chr: item.chr,
        tumor_type: item.tumor_type,
        version: item.version
    };

    return node;
}

function createEdge(item){
    var edge = {
        source: item._inV,
        target: item._outV,
        bonferroni: item.bonferroni,
        sample_size: item.sample_size,
        min_log_p_uncorrected: item.min_log_p_uncorrected,
        excluded_sample_count_a: item.excluded_sample_count_a,
        min_log_p_unused_a: item.min_log_p_unused_a,
        excluded_sample_count_b: item.excluded_sample_count_b,
        min_log_p_unused_b: item.min_log_p_unused_b,
        genomic_distance: item.genomic_distance,
        feature_types: item.feature_types
    };

    return edge;
}

function getVertexID(key, value, callback) {
    queryVertexId("vertices?key=" + key + "&value=" + value, function(id){
        getStartingVertex(id, callback);
    });
}

function getStartingVertex(id, callback) {
    var query = "vertices/" + id;

    queryRexster(query, function(){
        getVertices(id, callback);
    });
}

function getVertices(id, callback) {
    var query = "vertices/" + id + "/both";

    queryRexster(query, function(){
        getEdges(id, callback);
    });
}

function getEdges(id, callback) {
    var query = "vertices/" + id + "/bothE";

    queryRexster(query, function() {
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
    console.log(graphJSON);
    alchemy.begin({"dataSource": graphJSON})
}