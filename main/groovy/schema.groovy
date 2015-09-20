class Schema {

    //Open gremlin.sh
    //Load schema: \. schema.groovy
    //create schema object: schema = new Schema()
    //call build: g = schema.make()
    //returns graph object

    def load() {
        //g = TitanFactory.set("storage.backend", "cassandra").set("storage.hostname", "localhost").open()

        def g = TitanFactory.build().set("storage.backend", "cassandra").set("storage.hostname", "localhost").set("storage.batch-loading", true).open()
        def mgmt = g.getManagementSystem()
        def vProp = makeVertexProperties(mgmt)
        makeEdgeProperties(mgmt)
        makeLabels(mgmt)
        buildIndices(mgmt, vProp['objectID'], vProp['type'])
        commitSchema(mgmt, g)
        return g
    }

    def makeVertexProperties(mgmt) {
        //Vertex properties
        //This will be generated as "tumor_type:feature_type:geneId"
        def objectID = mgmt.makePropertyKey('objectID').dataType(String.class).make()
        def type = mgmt.makePropertyKey('type').dataType(String.class).make()
        def name = mgmt.makePropertyKey('name').dataType(String.class).make()
        def chr = mgmt.makePropertyKey('chr').dataType(String.class).make()
        def start = mgmt.makePropertyKey('startPos').dataType(Integer.class).make()
        def end = mgmt.makePropertyKey('endPos').dataType(Integer.class).make()
        def strand = mgmt.makePropertyKey('strand').dataType(Character.class).make()
        def tumor_type = mgmt.makePropertyKey('tumor_type').dataType(String.class).make()
        def version = mgmt.makePropertyKey('version').dataType(String.class).make()
        def feature_type = mgmt.makePropertyKey('feature_type').dataType(String.class).make()
        def annotation = mgmt.makePropertyKey('annotation').dataType(String.class).make()

        return ['objectID': objectID, 'type': type]
    }

    def makeEdgeProperties(mgmt) {
        // See edge-properties.txt for descriptions of these edge properties!
        def correlation = mgmt.makePropertyKey('correlation').dataType(Decimal.class).make() //3
        def sample_size = mgmt.makePropertyKey('sample_size').dataType(Decimal.class).make() //4
        def min_log_p_uncorrected = mgmt.makePropertyKey('min_log_p_uncorrected').dataType(Decimal.class).make() //5
        def bonferroni = mgmt.makePropertyKey('bonferroni').dataType(Decimal.class).make() //6
        def min_log_p_corrected = mgmt.makePropertyKey('min_log_p_corrected').dataType(Decimal.class).make() //7
        def excluded_sample_count_a = mgmt.makePropertyKey('excluded_sample_count_a').dataType(Decimal.class).make() //8
        def min_log_p_unused_a = mgmt.makePropertyKey('min_log_p_unused_a').dataType(Decimal.class).make() //9
        def excluded_sample_count_b = mgmt.makePropertyKey('excluded_sample_count_b').dataType(Decimal.class).make() //10
        def min_log_p_unused_b = mgmt.makePropertyKey('min_log_p_unused_b').dataType(Decimal.class).make() //11
        def genomic_distance = mgmt.makePropertyKey('genomic_distance').dataType(Decimal.class).make() //12
        def feature_types = mgmt.makePropertyKey('feature_types').dataType(String.class).make()
    }

    def makeLabels(mgmt) {
        //Type of relationship between vertices -- all pairwise for this batch load script
        def pairwise = mgmt.makeEdgeLabel('pairwise').multiplicity(Multiplicity.MULTI).make()
        def datasetslice = mgmt.makeEdgeLabel('datasetslice').multiplicity(Multiplicity.MULTI).make()
        def proximal = mgmt.makeEdgeLabel('proximal').multiplicity(Multiplicity.MULTI).make()
        def codesfor = mgmt.makeEdgeLabel('codesfor').multiplicity(Multiplicity.MULTI).make()
        //Identifies these objects as bioentities, as opposed to drugs or other objects we may add later
        def bioentity = mgmt.makeVertexLabel('bioentity').make()
    }

    def buildIndices(mgmt, objectID, type) {
        mgmt.buildIndex('byObjectID', Vertex.class).addKey(objectID).unique().buildCompositeIndex()
        mgmt.buildIndex('byType', Vertex.class).addKey(type).buildMixedIndex("search")
    }

    def commitSchema(mgmt, g) {
        mgmt.commit()
        g.commit()
    }
}