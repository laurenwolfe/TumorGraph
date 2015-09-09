//Query path and string
var rexsterURL = "http://glados49:8182/graphs/tumorgraph/";
var gremlin = "tp/gremlin?";

//supernode that loads
//var id = '58112';

//supernode that crashes browser
//var id = '2048';

//smaller set
var id = '840704';
var key = "objectID";
var val = "stad:Gene:ADCY8";

var graphJSON;
var vertices = [];
var edges = [];
var ids = [];
var edgeIDs = [];

$(window).load(function() {
    if(id) {
        getStartingVertex(id, buildChart);
    } else if(key && val) {
        getVertexID(key, val, buildChart);
    } else {
        //Choose a random default location to start the chart from.
    }
});

function passedQuery(q) {
    queryGremlin();
}

function queryGremlin(q) {
    $.ajax({
        type: "GET",
        url: rexsterURL + gremlin + q,
        dataType: "json",
        contentType: "application/json",
        success: function (json) {
            console.log(json.results);
//            callback();
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
            ParseJSONData(json);
            callback();
        }
    });
}

function queryVertexID(query, callback) {
    $.ajax({
        type: "GET",
        url: rexsterURL + query,
        dataType: "json",
        contentType: "application/json",
        success: function (json) {
            callback(json.results._id);
        }
    });
}

function ParseJSONData(data) {

    if(Array.isArray(data.results)) {
        $.each(data.results, function (i, item) {
            var dataItem;
            if(item._type === 'vertex' && ids.indexOf(item._id) === -1){
                ids.push(item._id);
                dataItem = createVertex(item);

                vertices.push(dataItem);

            } else if(item._type === 'edge' && edgeIDs.indexOf(item._id) === -1) {
                edgeIDs.push(item.edgeID);
                dataItem = createEdge(item);
                edges.push(dataItem);
            } else {
                console.log("Error 1");
            }
        });
    } else {
        if(data.results._type === 'vertex' && ids.indexOf(data.results._id) === -1) {
            ids.push(data.results._id);
            dataItem = createVertex(data.results);

            vertices.push(dataItem);
        } else if(data.results._type === 'edge' && edgeIDs.indexOf(data.results._id) === -1) {
            edgeIDs.push(data.results.edgeID);
            dataItem = createEdge(data.results);
            edges.push(dataItem);
        } else {
            console.log("Error 2");
        }
    }
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
        version: item.version,
        feature_type: item.feature_type
    };

    return node;
}

function createEdge(item){
    var edge = {
        source: item._inV,
        target: item._outV,
        edgeID: item._id,
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
    queryVertexID("vertices?key=" + key + "&value=" + value, function(id){
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
/*            tumor_type: ["acc", "blca", "brca", "cesc", "chol", "coad", "dlbc", "esca", "gbm", "hnsc",
                "kich", "kirc", "kirp", "laml", "lgg", "lihc", "luad", "lusc", "meso", "ov", "paad", "pcpg",
                "prad", "read", "sarc", "skcm", "stad", "tgct", "thca", "thym", "ucec", "ucs", "uvm"]
*/
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
        },
    };

    alchemy.begin(config);

    //alchemy = new Alchemy(config);
}