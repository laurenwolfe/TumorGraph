class PWLoader {
    def g
    def bg
    def masterGeneVertices
    def masterProteinVertices
    def masterProbeVertices
    int i = 0
    def vertices
    def edgeList

    def load() {
        g = openGraph()

        bg = new BatchGraph(g, VertexIDType.STRING, 10000)
        bg.setVertexIdKey("objectID")
        bg.setLoadingFromScratch(false)

        //Lists of master vertices
        masterGeneVertices = setMasterVertices("gene")
        masterProteinVertices = setMasterVertices("protein")
        masterProbeVertices = setMasterVertices("probe")

        processFiles()

        bg.commit()
        g.commit()
    }

    def processFiles() {
        new File("filenames.tsv").eachLine({ String file_iter ->
            edgeList = []
            vertices = [:]
            def details = file_iter.split('\\.')
            def v1, v2, edge

            println "Beginning processing on " + file_iter

            def tumor_type = details[0]
            def version = details[2]

            new File(file_iter).eachLine({ final String line ->
                def (object1, object2, correlation, sample_size, min_log_p_uncorrected, bonferroni,
                     min_log_p_corrected, excluded_sample_count_a, min_log_p_unused_a,
                     excluded_sample_count_b, min_log_p_unused_b, genomic_distance) = line.split('\t')

                def objArr1 = object1.split(':')
                def objArr2 = object2.split(':')
                def feature1 = objArr1[1]
                def feature2 = objArr2[1]
                def name1 = objArr1[2]
                def name2 = objArr2[2]
                def anno1 = "nothing"
                def anno2 = "nothing"

                if(objArr1.size() == 8) {
                    anno1 = objArr1[7]
                }

                if(objArr2.size() == 8) {
                    anno2 = objArr2[7]
                }

                if(feature1 != "CLIN" && feature2 != "CLIN"
                        && feature1 != "SAMP" && feature2 != "SAMP" &&
                        !(feature1 == "GNAB" && feature2 == "GNAB" &&
                                (anno1 == 'code_potential_somatic' || anno2 == 'code_potential_somatic'))) {

                    def objectID1 = makeObjID(feature1, tumor_type, name1)
                    def objectID2 = makeObjID(feature2, tumor_type, name2)

                    v1 = makeOrGetVertex(objArr1, objectID1, tumor_type, version)
                    v2 = makeOrGetVertex(objArr2, objectID2, tumor_type, version)

                    if ((!edgeList.contains(objectID1 + ":" + objectID2)) && (objectID1 != objectID2)) {
                        makeEdge(v1, v2, correlation, sample_size, min_log_p_uncorrected, bonferroni,
                                min_log_p_corrected, excluded_sample_count_a, min_log_p_unused_a,
                                excluded_sample_count_b, min_log_p_unused_b, genomic_distance, feature1,
                                feature2, objectID1, objectID2)
                    }
                }

                i++

                println "round: " + i.toString()

                if(i % 100 == 0) {
                    bg.commit()
                    println "round number: " + i.toString()
                }
            })
        })
    }

    def makeMasterGene(geneID, v, label) {
        def geneV

        if (!masterGeneVertices.containsKey(geneID)) {
            geneV = bg.addVertex(geneID)
            geneV.setProperty("objectID", geneID)
            geneV.setProperty("name", v.name)
            geneV.setProperty("type", "gene")

            !v.startPos ?: geneV.setProperty("startPos", v.startPos)
            !v.endPos ?: geneV.setProperty("endPos", v.endPos)
            !v.chr ?: geneV.setProperty("chr", v.chr)

            masterGeneVertices.put(geneID, geneV)
        } else {
            geneV = masterGeneVertices.get(geneID)
        }

        bg.addEdge(null, geneV, v, label)
    }

    def makeMasterProtein(proteinID, v) {
        def proteinV

        if(!masterProteinVertices.containsKey(proteinID)) {
            proteinV = bg.addVertex(proteinID)
            proteinV.setProperty("objectID", proteinID)
            proteinV.setProperty("name", v.name)
            proteinV.setProperty("type", "protein")

            !v.startPos ?: proteinV.setProperty("startPos", v.startPos)
            !v.endPos ?: proteinV.setProperty("endPos", v.endPos)
            !v.chr ?: proteinV.setProperty("chr", v.chr)
            !v.strand ?: proteinV.setProperty("strand", v.strand)

            masterProteinVertices.put(proteinID, proteinV)

            def geneID = "Gene:" + v.name

            //Make connection between master protein and master gene
            makeMasterGene(geneID, proteinV, "codesfor")

        } else {
            //We've already seen this protein, so it's master vertex already exists; grab it.
            proteinV = masterProteinVertices.get(proteinID)
        }

        bg.addEdge(null, proteinV, v, "datasetslice")

    }

    def makeMasterProbe(probeName, v) {
        def annotSplit = v.annotation.split('_')
        def probeID = "Methylation:" + annotSplit[0]
        def probeV

        if(!masterProbeVertices.containsKey(probeID))  {
            probeV = bg.addVertex(probeID)
            probeV.setProperty("objectID", probeID)
            probeV.setProperty("name", v.name)
            probeV.setProperty("type", "probe")

            !v.startPos ?: probeV.setProperty("startPos", v.startPos)
            !v.endPos ?: probeV.setProperty("endPos", v.endPos)
            !v.chr ?: probeV.setProperty("chr", v.chr)

            masterProbeVertices.put(probeID, probeV)

            def geneID = "Gene:" + v.name

            makeMasterGene(geneID, probeV, "proximal")

        } else {
            probeV = masterProbeVertices.get(probeID)
        }

        bg.addEdge(null, probeV, v, "datasetslice")
    }

    def makeEdge(v1, v2, correlation, sample_size, min_log_p_uncorrected, bonferroni, min_log_p_corrected,
                 excluded_sample_count_a, min_log_p_unused_a, excluded_sample_count_b, min_log_p_unused_b,
                 genomic_distance, feature_type_1, feature_type_2, objectID1, objectID2) {

        //outvertex ---> invertex
        def edge = bg.addEdge(null, v1, v2, "pairwise")
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

    def makeOrGetVertex(object, objectID, tumor_type, version) {
        def data_type, feature_type, name, chr, startPos, endPos, strand, annotation, v

        data_type = object[0]
        feature_type = object[1]
        name = object[2]

        if (object.size() > 3) {
            chr = object[3]
        }

        if(object.size() > 4) {
            startPos = object[4]
        }

        if(object.size() > 5) {
            endPos = object[5]
        }

        if(object.size() > 6) {
            strand = object[6]
        }

        if(object.size() > 7) {
            annotation = object[7]
        }

        //Does the vertex already exist? If not, create it in the db
        if (!vertices.containsKey(objectID)) {

            v = bg.addVertex(objectID)
            v.setProperty("objectID", objectID)
            v.setProperty("name", name)
            v.setProperty("tumor_type", tumor_type)
            v.setProperty("version", version)
            v.setProperty("feature_type", feature_type)

            //Some of these may be empty, so let's test for that.
            !chr ?: v.setProperty("chr", chr)
            !startPos ?: v.setProperty("startPos", startPos)
            !endPos ?: v.setProperty("endPos", endPos)
            !strand ?: v.setProperty("strand", strand)
            !annotation ?: v.setProperty("annotation", annotation)

            vertices.put(objectID, v)

            //Connect the vertex to its master node
            if(feature_type == "GNAB" || feature_type == "GEXP" || feature_type == "CNVR") {
                println "Master Gene"
                makeMasterGene("Gene:" + name, v, "datasetslice")
            } else if(feature_type == "RPPA") {
                println "Master Protein"
                makeMasterProtein("Protein:" + name, v)
            } else if(feature_type == "METH") {
                println "Master Probe"
                makeMasterProbe("Probe:" + name, v)
            }

        } else {
            v = vertices[objectID]
        }

        return v
    }

    def makeObjID(feature_type, tumor_type, name) {
        def objectID

        switch (feature_type) {
            case "GEXB":
                objectID = tumor_type + ':Gene:' + name
                break
            case "GNAB":
                objectID = tumor_type + ':Gene:' + name
                break
            case "CNVR":
                objectID = tumor_type + ':Gene:' + name
                break
            case "RPPA":
                objectID = tumor_type + ':Protein:' + name
                break
            case "METH":
                objectID = tumor_type + ':Methylation:' + name
                break
            case "MIRN":
                objectID = tumor_type + ':miRNA:' + name
                break
            default:
                objectID = tumor_type + ':' + feature_type + ':' + name
                break
        }

        return objectID
    }

    def setMasterVertices(type) {
        def masterMap = [:]
        def masterVertices = g.query().has("type", CONTAINS, type).vertices()
        def v

        if (masterVertices.size() > 0) {
            for (i = 0; i < masterVertices.size(); i++) {
                v = masterVertices.get(i)
                masterMap.put(v.objectID, v)
            }
        }

        return masterMap
    }

    def openGraph() {
        def conf = new BaseConfiguration() {
            {
                setProperty("storage.backend", "cassandra")
                setProperty("storage.hostname", "localhost")
                setProperty("storage.batch-loading", true)
            }
        }

        return TitanFactory.open(conf)
    }
}