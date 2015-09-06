//Query path and string
var rexsterURL = "http://glados49:8182/graphs/tumorgraph/";

var id = '41472';
var key = "objectID";
var val = "stad Gene TG";

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

function queryVertexId(query, callback) {
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
    var result;

    if(Array.isArray(data.results)) {
        $.each(data.results, function (i, item) {
            var dataItem;
            if(item._type === 'vertex') {
                dataItem = createVertex(item);

                result = $.grep(vertices, function(item){return item.id == id; });

                vertices.push(dataItem);

//                if(result.length === 0) { vertices.push(dataItem); }

            } else {
                dataItem = createEdge(item);
                edges.push(dataItem);
            }
        });
    } else {
        if(data.results._type === 'vertex') {
            dataItem = createVertex(data.results);

            //See if vertex is already added to vertices array
            var result = $.grep(vertices, function(item){ return item.id == id; });

            vertices.push(dataItem);

//            if(result.length === 0) { vertices.push(dataItem); }
        } else {
            dataItem = createEdge(data.results);
            edges.push(dataItem);
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
        version: item.version
    };

    return node;
}

function createEdge(item){
    var edge = {
        source: item._inV,
        target: item._outV,
        nodeID: item._id,
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
/*
case "GEXB":
objectID1 =  tumor_type + ' Gene ' + name1
case "GNAB":
objectID1 =  tumor_type + ' Gene ' + name1
case "CNVR":
objectID1 =  tumor_type + ' Gene ' + name1
case "RPPA":
objectID1 =  tumor_type + ' Protein ' + name1
case "METH":
objectID1 =  tumor_type + ' Methylation ' + name1
case "MIRN":
objectID1 =  tumor_type + ' miRNA ' + name1
*/

function buildChart() {
    console.log(JSON.stringify(graphJSON));
    var config = {
        "dataSource": graphJSON //,
        /*
        "nodeTypes": { "feature_type": ["GEXB", "GNAB", "CNVR", "RPPA", "METH", "MIRN"] },
        //"nodeCaption": "name",
        //"edgeCaption": "genomic_distance",
        "nodeMouseOver": function(node) {
            return node.tumor_type;
        },
        "nodeStyle": {
            "all": {
                "radius": 10
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
        */
    }

//    var alchemy = new Alchemy(config);
}