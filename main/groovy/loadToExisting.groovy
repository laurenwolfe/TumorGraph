conf = new BaseConfiguration() {
    {
        setProperty("storage.backend", "cassandra")
        setProperty("storage.hostname", "127.0.0.1")
        setProperty("storage.batch-loading", true)
    }
}

g = TitanFactory.open(conf)

masterGeneVertices = [:]
masterProteinVertices = [:]
masterProbeVertices = [:]

masterGenes = g.query().has("type", CONTAINS, "gene").vertices()

if(masterGenes.size() > 0) {
    for (i = 0; i < masterGenes.size(); i++) {
        v = masterGenes.get(i)

        masterGeneVertices.put(v.objectID, v)
    }
}

masterProteins = g.query().has("type", CONTAINS, "protein").vertices()

if(masterProteins.size() > 0) {
    for (i = 0; i < masterProteins.size(); i++) {
        v = masterProteins.get(i)

        masterProteinVertices.put(v.objectID, v)
    }
}

masterProbes = g.query().has("type", CONTAINS, "probe").vertices()

if(masterProbes.size() > 0) {
    for (i = 0; i < masterProbes.size(); i++) {
        v = masterProbes.get(i)

        masterProbeVertices.put(v.objectID, v)
    }
}