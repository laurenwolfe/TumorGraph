//Yo! I'm Lauren and I wrote this code.

/*
conf = new BaseConfiguration() {
    {
        setProperty("storage.backend", "cassandra")
        setProperty("storage.hostname", "127.0.0.1")
        setProperty("storage.batch-loading", true)
    }
}

g = TitanFactory.open(conf)
*/

g = TitanFactory.build()
    .set("storage.backend", "cassandra")
    .set("storage.hostname", "127.0.0.1")
    .set("storage.batch-loading", true)
    .open()


mgmt = g.getManagementSystem()

//*************************//
// CREATE DATABASE SCHEMA  //
//                         //
//*************************//

//This will be generated as "feature_type:geneId"
objectID = mgmt.makePropertyKey('objectID').dataType(String.class).make()
//Type of relationship between vertices -- all pairwise for this batch load script
pairwise = mgmt.makeEdgeLabel('pairwise').multiplicity(Multiplicity.MULTI).make()
datasetslice = mgmt.makeEdgeLabel('datasetslice').multiplicity(Multiplicity.MULTI).make()
proximal = mgmt.makeEdgeLabel('proximal').multiplicity(Multiplicity.MULTI).make()
codesfor = mgmt.makeEdgeLabel('codesfor').multiplicity(Multiplicity.MULTI).make()
//Identifies these objects as bioentities, as opposed to drugs or other objects we may add later
bioentity = mgmt.makeVertexLabel('bioentity').make()

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

//Ids, Vertices
masterGeneVertices = [:]
masterProteinVertices = [:]
masterProbeVertices = [:]

//******************//
// DATA PROCESSING  //
//                  //
//******************//

bg = new BatchGraph(g, VertexIDType.STRING, 10000000)

//Filename will need to be looped here from another file containing filenames and perhaps tumor
//type (or could just rtrim the tumor type from filenames.)
//Example filename: stad.all.16jan15.TP.pwpv
new File("filenames.tsv").eachLine({ String file_iter ->

    objectID1
    objectID2

    //For testing, output count
    edgeList = []
    vertices  = [:]

    def details = file_iter.split('\\.')

    tumor_type = details[0]
    version = details[2]

    new File(file_iter).eachLine({ final String line ->
        def i = 0

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
                objectID2 = tumor_type + ':' + feature_type_2 + ':' + name2
                break
        }

        //******************************//
        // Create Pairwise Relationship //
        //                              //
        //******************************//

        //Filtering excluded feature types
        if(!(feature_type_1 == "GNAB" && feature_type_2 == "GNAB" &&
                (annotation1 == 'code_potential_somatic' || annotation2 == 'code_potential_somatic'))
                && feature_type_1 != "CLIN" && feature_type_2 != "CLIN"
                && feature_type_1 != "SAMP" && feature_type_2 != "SAMP") {

            //Does the vertex already exist? If not, create it in the db
            if (!vertices.containsKey(objectID1)) {

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

                vertices.put(objectID1, v1)
            } else {
                v1 = vertices[objectID1]
            }

            if (!vertices.containsKey(objectID2)) {

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

                vertices.put(objectID2, v2)
            } else {
                v2 = vertices[objectID2]
            }

            if (!edgeList.contains(objectID1 + ":" + objectID2) && (objectID1 != objectID2)) {
                //outvertex ---> invertex
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
            }

            //************************//
            // Create Datasetslice    //
            // MasterGene --> subGene //
            //************************//

            if(feature_type_1 == "GNAB" || feature_type_1 == "GEXP" || feature_type_1 == "CNVR") {
                geneID1 = "Gene:" + name1
                geneID2 = "Gene:" + name2

                if (!masterGeneVertices.containsKey(geneID1)) {
                    geneV1 = bg.addVertex(geneID1)
                    geneV1.setProperty("objectID", geneID1)
                    geneV1.setProperty("name", name1)

                    !start1 ?: geneV1.setProperty("start", start1)
                    !end1 ?: geneV1.setProperty("end", end1)
                    !chr1 ?: geneV1.setProperty("chr", chr1)
                } else {
                    geneV1 = masterGeneVertices.get(geneID1)
                }

                geneEdge1 = bg.addEdge(null, geneV1, v1, "datasetslice")
            }

            if(feature_type_2 == "GNAB" || feature_type_2 == "GEXP" || feature_type_2 == "CNVR") {
                if (!masterGeneVertices.containsKey(geneID2)) {
                    geneV2 = bg.addVertex(geneID2)
                    geneV2.setProperty("objectID", geneID2)
                    geneV2.setProperty("name", name2)

                    !start2 ?: geneV2.setProperty("start", start2)
                    !end2 ?: geneV2.setProperty("end", end2)
                    !chr2 ?: geneV2.setProperty("chr", chr2)
                } else {
                    geneV2 = masterGeneVertices.get(geneID2)
                }

                geneEdge2 = bg.addEdge(null, geneV2, v2, "datasetslice")
            }

            //*******************************//
            // Create Datasetslice          //
            // MasterProtein --> subProtein //
            //*******************************//

            if(feature_type_1 == "RPPA") {
                proteinID1 = "Protein:" + name1
                geneProteinID1 = "Gene:" + name1

                //First time we've seen this protein? Create the master protein vertex, link it to matching master gene vertex
                if(!masterProteinVertices.containsKey(proteinID1)) {
                    proteinV1 = bg.addVertex(proteinID1)
                    proteinV1.setProperty("objectID", proteinID1)
                    proteinV1.setProperty("name", name1)

                    !start1 ?: proteinV1.setProperty("start", start1)
                    !end1 ?: proteinV1.setProperty("end", end1)
                    !chr1 ?: proteinV1.setProperty("chr", chr1)
                    !strand1 ?: proteinV1.setProperty("strand", strand1)

                    //*******************************//
                    // Create Codesfor               //
                    // MasterGene --> MasterProtein  //
                    //*******************************//

                    //Does this protein's master gene exist yet? If not, create it.
                    if(!masterGeneVertices.containsKey(geneProteinID1)) {
                        geneProteinV1 = bg.addVertex(geneProteinID1)
                        geneProteinV1.setProperty("objectID", geneID1)
                        geneProteinV1.setProperty("name", name1)

                        !start1 ?: geneProteinV1.setProperty("start", start1)
                        !end1 ?: geneProteinV1.setProperty("end", end1)
                        !chr1 ?: geneProteinV1.setProperty("chr", chr1)
                    } else {
                        geneProteinV1 = masterGeneVertices.get(geneProteinID1)
                    }

                    //Link master gene to master protein
                    masterProteinGeneEdge1 = bg.addEdge(null, geneProteinV1, proteinV1, "codesfor")

                } else {
                    //We've already seen this protein, so it's master vertex already exists; grab it.
                    proteinV1 = masterProteinVertices.get(proteinID1)
                }
                proteinEdge1 = bg.addEdge(null, proteinV1, v1, "datasetslice")
            }

            if(feature_type_2 == "RPPA") {
                proteinID2 = "Protein:" + name2
                geneProteinID2 = "Gene:" + name2

                if(!masterProteinVertices.containsKey(proteinID2)) {
                    proteinV2 = bg.addVertex(proteinID2)
                    proteinV2.setProperty("objectID", proteinID2)
                    proteinV2.setProperty("name", name2)

                    !start2 ?: proteinV2.setProperty("start", start2)
                    !end2 ?: proteinV2.setProperty("end", end2)
                    !chr2 ?: proteinV2.setProperty("chr", chr2)
                    !strand2 ?: proteinV2.setProperty("strand", strand2)

                    //*******************************//
                    // Create Codesfor               //
                    // MasterGene --> MasterProtein  //
                    //*******************************//

                    //Does this protein's master gene exist yet? If not, create it.
                    if(!masterGeneVertices.containsKey(geneProteinID2)) {
                        geneProteinV2 = bg.addVertex(geneProteinID2)
                        geneProteinV2.setProperty("objectID", geneID2)
                        geneProteinV2.setProperty("name", name2)

                        !start2 ?: geneProteinV2.setProperty("start", start2)
                        !end2 ?: geneProteinV2.setProperty("end", end2)
                        !chr2 ?: geneProteinV2.setProperty("chr", chr2)
                    } else {
                        geneProteinV2 = masterGeneVertices.get(geneProteinID2)
                    }

                    //Link master gene to master protein
                    masterProteinGeneEdge2 = bg.addEdge(null, geneProteinV2, proteinV2, "codesfor")

                } else {
                    proteinV2 = masterProteinVertices.get(proteinID2)
                }

                proteinEdge2 = bg.addEdge(null, proteinV2, v2, "datasetslice")
            }

            if(feature_type_1 == "METH") {
                annotSplit1 = tokenize(annotation1, "_")
                probeID1 = "Methylation:" + annotSplit1.get(0)
                geneMethID1 = "Gene:" + name1

                //*******************************//
                // Create Proximal               //
                // MasterGene --> MasterProbe    //
                //*******************************//

                //If we've never seen this probe before, we need to create its vertex and link it to its master gene.
                if(!masterProbeVertices.containsKey(probeID1))  {
                    probeV1 = bg.addVertex(probeID1)
                    probeV1.addProperty("objectID", probeID1)
                    probeV1.addProperty("name", name)

                    !start1 ?: probeV1.setProperty("start", start1)
                    !end1 ?: probeV1.setProperty("end", end1)
                    !chr1 ?: probeV1.setProperty("chr", chr1)

                    if(!masterGeneVertices.containsKey(geneMethID1)) {
                        geneMethV1 = bg.addVertex(geneMethID1)
                        geneMethV1.setProperty("objectID", geneID1)
                        geneMethV1.setProperty("name", name1)

                        !start1 ?: geneMethV1.setProperty("start", start1)
                        !end1 ?: geneMethV1.setProperty("end", end1)
                        !chr1 ?: geneMethV1.setProperty("chr", chr1)
                    } else {
                        geneMethV1 = masterGeneVertices.get(geneMethID1)
                    }

                    geneMethEdge1 = bg.addEdge(null, geneMethV1, probeV1, "proximal")

                } else {
                    probeV1 = masterProbeVertices.get(probeID1)
                }

                probeEdge1 = bg.addEdge(null, probeV1, v1, "datasetslice")
            }

            if(feature_type_2 == "METH") {
                annotSplit2 = tokenize(annotation2, "_")
                probeID2 = "Methylation:" + annotSplit2.get(0)
                geneMethID2 = "Gene:" + name2


                if(!masterProbeVertices.containsKey(probeID2))  {
                    probeV2 = bg.addVertex(probeID2)
                    probeV2.addProperty("objectID", probeID2)
                    probeV2.addProperty("name", name)

                    !start2 ?: probeV2.setProperty("start", start2)
                    !end2 ?: probeV2.setProperty("end", end2)
                    !chr2 ?: probeV2.setProperty("chr", chr2)

                    //*******************************//
                    // Create Proximal               //
                    // MasterGene --> MasterProbe    //
                    //*******************************//

                    if(!masterGeneVertices.containsKey(geneMethID2)) {
                        geneMethV2 = bg.addVertex(geneMethID2)
                        geneMethV2.setProperty("objectID", geneID2)
                        geneMethV2.setProperty("name", name2)

                        !start2 ?: geneMethV2.setProperty("start", start2)
                        !end2 ?: geneMethV2.setProperty("end", end2)
                        !chr2 ?: geneMethV2.setProperty("chr", chr2)
                    } else {
                        geneMethV2 = masterGeneVertices.get(geneMethID2)
                    }

                    geneMethEdge2 = bg.addEdge(null, geneMethV2, probeV2, "proximal")

                } else {
                    probeV2 = masterProbeVertices.get(probeID2)
                }

                probeEdge2 = bg.addEdge(null, probeV2, v2, "datasetslice")
            }

            i++
            if (i % 100 == 0) {
                bg.commit()
            }
        }
    })
})

g.commit()