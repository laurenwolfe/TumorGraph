    conf = new BaseConfiguration() {{
            setProperty("storage.backend", "cassandra")
            setProperty("storage.hostname", "127.0.0.1")
            setProperty("storage.batch-loading", true)

    }}

    g = TitanFactory.open(conf)
    mgmt = g.getManagementSystem()

    //This will be generated as "feature_type:geneId"
    objectId = mgmt.makePropertyKey('objectID').dataType(String.class).make()
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

    /*
    Edge properties -- inline comment corresponds to column #:
    # 1 feature A
    # 2 feature B (order is alphabetical, and has no effect on result)
    # 3 Spearman correlation coefficient (range is [-1,+1], also can be "NA" if cannot be calculated or is not appropriate to the data)
    # 4 number of samples used for pairwise test (non-NA overlap of feature A and feature B)
    # 5 -log10(p-value)  (uncorrected)
    # 6 log10(Bonferroni correction factor)
    # 7 -log10(corrected p-value)   [ col #7 = min ( (col #5 - col #6), 0 ) ]
    # 8 # of non-NA samples in feature A that were not used in pairwise test
    # 9 -log(p-value) that the samples from A that were not used are "different" from those that were
    #10 (same as col #8 but for feature B)
    #11 (same as col #9 but for feature B)
    #12 genomic distance between features A and B (if not on same chromosome or one or both do not have coordinates, then this value is set to 500000000)
    */
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
    //mgmt.buildIndex('byObjectId', Vertex.class).addKey(objectId).unique().buildCompositeIndex()
    mgmt.commit()

    bg = new BatchGraph(g, VertexIDType.STRING, 10000000)

    //For testing, output count
    x = 0
    y = 0
    idList = []


    //Filename will need to be looped here from another file containing filenames and perhaps tumor 
    //type (or could just rtrim the tumor type from filenames.)
    //Example filename: stad.all.16jan15.TP.pwpv
    new File("filenames.tsv").eachLine( { String file_iter ->

        def details = file_iter.split('\\.')

        new File(file_iter).eachLine( { final String line ->

            tumor_type = details[0]
            version = details[2]

            //Pull in line from the tsv
            def (object1,
                object2,
                correlation1,
                sample_size1,
                min_log_p_uncorrected1,
                bonferroni1,
                excluded_sample_count_a1,
                min_log_p_unused_a1,
                excluded_sample_count_b1,
                min_log_p_unused_b1,
                genomic_distance1) = line.split('\t')   
            
            //Split bioentity column into component data
            def (dataType1,
                 featureType1, 
                 name1,
                 chr1,
                 start1,
                 end1,
                 strand1,
                 annotation1) = object1.split(':')
            
            //Split bioentity column into component data
            def (dataType2,
                 featureType2,
                 name2,
                 chr2,
                 start2,
                 end2,
                 strand2,
                 annotation2) = object2.split(':')
            
            def objectID1
            def objectID2

            //This is for filtering by annotation type, currently both bioentities need to be code_potential_somatic for the 
            //code block to execute.

            //Generate objectIDs by concatenating the tumor type, feature type and gene name
            switch(featureType1) {
                case "GEXB":
                    objectID1 =  tumor_type + ':Gene:' + name1
                    break
                case "GNAB":
                    objectID1 =  tumor_type + ':Gene:' + name1
                    break
                case "CNVR":
                    objectID1 =  tumor_type + ':Gene:' + name1
                    break
                case "RPPA":
                    objectID1 =  tumor_type + ':Protein:' + name1
                    break
                case "METH":
                    objectID1 =  tumor_type + ':Methylation:' + name1
                    break
                case "MIRN":
                    objectID1 =  tumor_type + ':miRNA:' + name1
                    break
                default:
                    objectID1 = tumor_type + ':' + featureType1 + ':' + name1
                    break
            }

            switch(featureType2) {
                case "GEXB":
                    objectID2 =  tumor_type + ':Gene:' + name2
                    break
                case "GNAB":
                    objectID2 =  tumor_type + ':Gene:' + name2
                    break
                case "CNVR":
                    objectID2 =  tumor_type + ':Gene:' + name2
                    break
                case "RPPA":
                    objectID2 =  tumor_type + ':Protein:' + name2
                    break
                case "METH":
                    objectID2 =  tumor_type + ':Methylation:' + name2
                    break
                case "MIRN":
                    objectID2 =  tumor_type + ':miRNA:' + name2
                    break
                default:
                    objectID2 = tumor_type + ':' + featureType1 + ':' + name2
                    break
            }

                //Does the vertex already exist? If not, create it in the db
                if(!bg.getVertex(objectID1)) {
                    v1 = bg.addVertex(objectID1)
                    v1.setProperty("objectID", objectID1)
                    v1.setProperty("name", name1)
                    v1.setProperty("tumor_type", tumor_type)
                    v1.setProperty("version", version)
                    v1.setProperty("feature_type", feature_type)

                    //Some of these may be empty, so let's test for that.
                    !chr1 ?: v1.setProperty("chr", chr1)
                    !start1 ?: v1.setProperty("start", start1)
                    !end1 ?: v1.setProperty("end", end1)
                    !strand1 ?: v1.setProperty("strand", strand1)

                    idList.add(v1.getId());
                    y++

                } else {
                    v1 = bg.getVertex(objectID1)
                }

                if(!bg.getVertex(objectID2)) {
                    v2 = bg.addVertex(objectID2)
                    v2.setProperty("objectID", objectID2)
                    v2.setProperty("name", name2)
                    v2.setProperty("tumor_type", tumor_type)
                    v2.setProperty("version", version)
                    v2.setProperty("feature_type", feature_type)

                    //Some of these may be empty, so let's test for that.
                    !chr2 ?: v2.setProperty("chr", chr2)
                    !start2 ?: v2.setProperty("start", start2)
                    !end2 ?: v2.setProperty("end", end2)
                    !strand2 ?: v2.setProperty("strand", strand2)

                    idList.add(v2.getId());
                    y++

                } else {
                    v2 = bg.getVertex(objectID2)
                }
                
                //create edge and set its properties 
                //Some of these may be empty, so let's test for that
                edge = bg.addEdge(null, v1, v2, "pairwise")
                edge.setProperty("sample_size", sample_size1)
                edge.setProperty("min_log_p_uncorrected", min_log_p_uncorrected1)
                edge.setProperty("bonferroni", bonferroni1)
                edge.setProperty("excluded_sample_count_a", excluded_sample_count_a1)
                edge.setProperty("min_log_p_unused_a", min_log_p_unused_a1)
                edge.setProperty("excluded_sample_count_b", excluded_sample_count_b1)
                edge.setProperty("min_log_p_unused_b", min_log_p_unused_b1)
                edge.setProperty("genomic_distance", genomic_distance1)
                edge.setProperty("feature_types", featureType1 + ':' + featureType2)

                x++
        })
        println x + " edges generated"
        println y + " vertices generated"
        println "vertex count: " + idList.size()
        uniqueList = idList.unique()
        println "unique values: " + uniqueList.size()
    })

    g.commit()
