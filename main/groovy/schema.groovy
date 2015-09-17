
g = TitanFactory.build().set("storage.backend", "cassandra").set("storage.hostname", "127.0.0.1").set("storage.batch-loading", true).open()

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
type = mgmt.makePropertyKey('type').dataType(String.class).make()

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
mgmt.buildIndex('byType', Vertex.class).addKey(type).buildMixedIndex("search")
mgmt.commit()

g.commit()