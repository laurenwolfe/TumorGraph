//Query path and string
//var restURL = "http://192.168.99.100:8182/graphs/tumorgraph/tp/gremlin?script=";
//var restURL = "http://192.168.99.100:8080/query/index?query=";

var restURL = "http://192.168.99.100:8080/query/index?query=";

function passedQuery(q) {

    $("#alchemy").empty();
    $("#popup").empty();
    $("#edgeinfo").empty();

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
        //showControlDash: true,
        //showStats: true,
        //nodeStats: true,
        forceLocked: false,
        //nodeFilters: true,
        //showFilters: true,
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
        }, nodeMouseOver: function(node) {
            var circle = $( "#circle-" + node.id);
            var position = circle.offset();
            var toppos = position.top + 10;
            var leftpos = position.left + 10;

            var tablehtml = "<tr><td>Name: </td><td>"+ node._properties.name +"</td></tr>" +
                "<tr><td>Chr: </td><td>"+ node._properties.chr +"</td></tr>" +
                "<tr><td>Start: </td><td>"+ node._properties.start +"</td></tr>" +
                "<tr><td>End: </td><td>"+ node._properties.end +"</td></tr>" +
                "<tr><td>Strand: </td><td>"+ node._properties.strand +"</td></tr>" +
                "<tr><td>Tumor Type: </td><td>"+ node._properties.tumor_type +"</td></tr>" +
                "<tr><td>Version: </td><td>"+ node._properties.version +"</td></tr>" +
                "<tr><td>Feature Type: </td><td>"+ node._properties.feature_type +"</td></tr>" +
                "<tr><td>Annotation: </td><td>"+ node._properties.annotation +"</td></tr>" +
                "<tr><td>ID: </td><td>"+ node._properties.id +"</td></tr>";

            $("#popup").append(tablehtml);
            $("#popup").css({top: toppos + "px", left: leftpos + "px", 'z-index': 999, position:'absolute'});

        }, nodeMouseOut: function(node) {
            $("#popup").empty();
        }, edgeClick: function(edge) {
            var edgeXY = $( "#path-" + edge.id);
            var position = edgeXY.offset();
            var toppos = position.top + 10;
            var leftpos = position.left + 10;

            var edgehtml = "<div id='closebtn'><img id='close' src='/css/images/close-button.png' width='20' height='20'></div>" +
                "<table>" +
                "<tr><td>Nodes connecting:</td><td>" + edge._properties.source + " - "+ edge._properties.target + "</td></tr>" +
                "<tr><td>Edge ID:</td><td>"+ edge._properties.edgeID +"</td></tr>" +
                "<tr><td>Correlation: </td><td>" + edge._properties.correlation + "</td></tr>" +
                "<tr><td>Sample size: </td><td>" + edge._properties.sample_size + "</td></tr>" +
                "<tr><td>-LogP (uncorrected): </td><td>" + edge._properties.min_log_p_uncorrected + "</td></tr>" +
                "<tr><td>-LogP (corrected): </td><td>" + edge._properties.min_log_p_corrected + "</td></tr>" +
                "<tr><td>Bonferroni: </td><td>" + edge._properties.bonferroni + "</td></tr>" +
                "<tr><td>Excl. sample count A: </td><td>" + edge._properties.excluded_sample_count_a + "</td></tr>" +
                "<tr><td>Unused -LogP A: </td><td>" + edge._properties.min_log_p_unused_a + "</td></tr>" +
                "<tr><td>Excl. sample count B: </td><td>" + edge._properties.excluded_sample_count_b + "</td></tr>" +
                "<tr><td>Unused -LogP B: </td><td>" + edge._properties.min_log_p_unused_b + "</td></tr>" +
                "<tr><td>Genomic distance: </td><td>" + edge._properties.genomic_distance + "</td></tr>" +
                "<tr><td>Feature types: </td><td>" + edge._properties.feature_types + "</td></tr>" +
                "</table>";

            $("#edgeinfo").append(edgehtml);
            $("#edgeinfo").css({top: toppos + "px", left: leftpos + "px", 'z-index': 1000, position:'absolute'});

            $(function () {
                $("#close").click(function () {
                    $("#edgeinfo").empty();
                });
            });
        }, nodeClick: function(node) {
            var q = "g.v(" + node.id + ")";
            passedQuery(q);
        }
    };

    alchemy.begin(config);
}