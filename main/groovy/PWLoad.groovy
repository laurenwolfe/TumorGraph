//Yo! I'm Lauren and I wrote this code.
conf = new BaseConfiguration() {
    {
        setProperty("storage.backend", "cassandra")
        setProperty("storage.hostname", "127.0.0.1")
        setProperty("storage.batch-loading", true)
    }
}

g = TitanFactory.open(conf)
mgmt = g.getManagementSystem()


//*************************//
// CREATE DATABASE SCHEMA  //
//                         //
//*************************//

//This will be generated as "feature_type:geneId"
objectID = mgmt.makePropertyKey('objectID').dataType(String.class).make()
//Type of relationship between vertices -- all pairwise for this batch load script
pairwise = mgmt.makeEdgeLabel('pairwise').multiplicity(Multiplicity.MULTI).make()
//Identifies these objects as bioentities, as opposed to drugs or other objects we may add later
bioentity = mgmt.makeVertexLabel('bioentity').make();

//Vertex properties
name = mgmt.makePropertyKey('name').dataType(String.class).make()
chr = mgmt.makePropertyKey('chr').dataType(String.class).make()
start = mgmt.makePropertyKey('start').dataType(Integer.class).make()
end = mgmt.makePropertyKey('end').dataType(Integer.class).make()
strand = mgmt.makePropertyKey('strand').dataType(Character.class).make()
tumor_type = mgmt.makePropertyKey('tumor_type').dataType(String.class).make()
version = mgmt.makePropertyKey('version').dataType(String.class).make()
feature_type = mgmt.makePropertyKey('feature_type').dataType(String.class).make()
annotation = mgmt.makePropertyKey('annotation').dataType(String.class).make()


// See edge-properties.txt for descriptions of these edge properties!

correlation = mgmt.makePropertyKey('correlation').dataType(Decimal.class).make() //3
sample_size = mgmt.makePropertyKey('sample_size').dataType(Decimal.class).make() //4
min_log_p_uncorrected = mgmt.makePropertyKey('min_log_p_uncorrected').dataType(Decimal.class).make() //5
bonferroni = mgmt.makePropertyKey('bonferroni').dataType(Decimal.class).make() //6
min_log_p_corrected = mgmt.makePropertyKey('min_log_p_corrected').dataType(Decimal.class).make() //7
excluded_sample_count_a = mgmt.makePropertyKey('excluded_sample_count_a').dataType(Decimal.class).make() //8
min_log_p_unused_a = mgmt.makePropertyKey('min_log_p_unused_a').dataType(Decimal.class).make() //9
excluded_sample_count_b = mgmt.makePropertyKey('excluded_sample_count_b').dataType(Decimal.class).make() //10
min_log_p_unused_b = mgmt.makePropertyKey('min_log_p_unused_b').dataType(Decimal.class).make() //11
genomic_distance = mgmt.makePropertyKey('genomic_distance').dataType(Decimal.class).make() //12
feature_types = mgmt.makePropertyKey('feature_types').dataType(String.class).make()

//Create index of ObjectId to speed map building
mgmt.buildIndex('byObjectID', Vertex.class).addKey(objectID).unique().buildCompositeIndex()
mgmt.commit()


//******************//
//* DATA PROCESSING //
//*                 //
//******************//

bg = new BatchGraph(g, VertexIDType.STRING, 10000000)

//For testing, output count
idList = []
edgeList = []
def objectID1
def objectID2

//Filename will need to be looped here from another file containing filenames and perhaps tumor
//type (or could just rtrim the tumor type from filenames.)
//Example filename: stad.all.16jan15.TP.pwpv
new File("filenames.tsv").eachLine({ String file_iter ->

    def details = file_iter.split('\\.')

    new File(file_iter).eachLine({ final String line ->
        def id = 0

        tumor_type = details[0]
        version = details[2]

        //Pull in line from the tsv
        def (object1, object2, correlation, sample_size, min_log_p_uncorrected, bonferroni, min_log_p_corrected, excluded_sample_count_a,
             min_log_p_unused_a, excluded_sample_count_b, min_log_p_unused_b, genomic_distance) = line.split('\t')

        //Split bioentity columns into component data
        def (data_type_1, feature_type_1, name1, chr1, start1, end1, strand1, annotation1) = object1.split(':')
        def (data_type_2, feature_type_2, name2, chr2, start2, end2, strand2, annotation2) = object2.split(':')

        //Generate objectIDs by concatenating the tumor type, feature type and gene name
        switch (feature_type_1) {
            case "GEXB":
                objectID1 = tumor_type + ':Gene:' + name1
                break
            case "GNAB":
                objectID1 = tumor_type + ':Gene:' + name1
                break
            case "CNVR":
                objectID1 = tumor_type + ':Gene:' + name1
                break
            case "RPPA":
                objectID1 = tumor_type + ':Protein:' + name1
                break
            case "METH":
                objectID1 = tumor_type + ':Methylation:' + name1
                break
            case "MIRN":
                objectID1 = tumor_type + ':miRNA:' + name1
                break
            default:
                objectID1 = tumor_type + ':' + feature_type_1 + ':' + name1
                break
        }

        switch (feature_type_2) {
            case "GEXB":
                objectID2 = tumor_type + ':Gene:' + name2
                break
            case "GNAB":
                objectID2 = tumor_type + ':Gene:' + name2
                break
            case "CNVR":
                objectID2 = tumor_type + ':Gene:' + name2
                break
            case "RPPA":
                objectID2 = tumor_type + ':Protein:' + name2
                break
            case "METH":
                objectID2 = tumor_type + ':Methylation:' + name2
                break
            case "MIRN":
                objectID2 = tumor_type + ':miRNA:' + name2
                break
            default:
                objectID2 = tumor_type + ':' + feature_type_1 + ':' + name2
                break
        }

        //Does the vertex already exist? If not, create it in the db
        if (!idList.contains(objectID1)) {

            v1 = bg.addVertex(objectID1)
            v1.setProperty("objectID", objectID1)
            v1.setProperty("name", name1)
            v1.setProperty("tumor_type", tumor_type)
            v1.setProperty("version", version)
            v1.setProperty("feature_type", feature_type_1)

            //Some of these may be empty, so let's test for that.
            !chr1 ?: v1.setProperty("chr", chr1)
            !start1 ?: v1.setProperty("start", start1)
            !end1 ?: v1.setProperty("end", end1)
            !strand1 ?: v1.setProperty("strand", strand1)
            !annotation1 ?: v1.setProperty("annotation", annotation1)

            idList.add(objectID1);

            i++
        } else {

            v1 = bg.getVertex(objectID1)
        }

        if (!idList.contains(objectID2)) {

            v2 = bg.addVertex(objectID2)
            v2.setProperty("objectID", objectID2)
            v2.setProperty("name", name2)
            v2.setProperty("tumor_type", tumor_type)
            v2.setProperty("version", version)
            v2.setProperty("feature_type", feature_type_2)

            //Some of these may be empty, so let's test for that.
            !chr2 ?: v2.setProperty("chr", chr2)
            !start2 ?: v2.setProperty("start", start2)
            !end2 ?: v2.setProperty("end", end2)
            !strand2 ?: v2.setProperty("strand", strand2)
            !annotation2 ?: v2.setProperty("annotation", annotation2)

            idList.add(objectID2);
            i++
        } else {
            v2 = bg.getVertex(objectID2)
        }

        if (!edgeList.contains(objectID1 + ":" + objectID2)) {
            edge = bg.addEdge(null, v1, v2, "pairwise")
            !correlation ?: edge.setProperty("correlation", correlation)
            !sample_size ?: edge.setProperty("sample_size", sample_size)
            !min_log_p_corrected ?: edge.setProperty("min_log_p_corrected", min_log_p_corrected)
            !min_log_p_uncorrected ?: edge.setProperty("min_log_p_uncorrected", min_log_p_uncorrected)
            !bonferroni ?: edge.setProperty("bonferroni", bonferroni)
            !excluded_sample_count_a ?: edge.setProperty("excluded_sample_count_a", excluded_sample_count_a)
            !min_log_p_unused_a ?: edge.setProperty("min_log_p_unused_a", min_log_p_unused_a)
            !excluded_sample_count_b ?: edge.setProperty("excluded_sample_count_b", excluded_sample_count_b)
            !min_log_p_unused_b ?: edge.setProperty("min_log_p_unused_b", min_log_p_unused_b)
            !genomic_distance ?: edge.setProperty("genomic_distance", genomic_distance)
            edge.setProperty("feature_types", feature_type_1 + ':' + feature_type_2)

            edgeList.add(objectID1 + ":" + objectID2)
            i++
        }

        if( i % 10000 == 0) { bg.commit() }

    })

})

g.commit()