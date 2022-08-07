echo "Cleaning up any previous versions..."
curl -s -H 'Content-Type: application/json' -d '{"delete": {"query": "id:*"}}' "http://localhost:8981/solr/.system/update?commit=true" > /dev/null
curl -s "http://localhost:8981/solr/admin/collections?action=DELETE&name=ispy_log" > /dev/null
curl -s "http://localhost:8981/solr/admin/collections?action=DELETE&name=demo_data" > /dev/null

echo "POSTing plugin to SolrCloud..."
curl -s -X POST -H 'Content-Type: application/octet-stream' --data-binary @"../build/libs/ispy_component-0.1.jar" "http://localhost:8981/solr/.system/blob/ispy_component" > /dev/null

#echo "Verify plugin upload:"
#curl http://localhost:8981/solr/.system/blob?omitHeader=true

echo "Setting up logging collection..."
curl -s "http://localhost:8981/solr/admin/collections?action=CREATE&name=ispy_log&numShards=2&replicationFactor=1" > /dev/null

echo "Set up demo data collection..."
curl -s "http://localhost:8981/solr/admin/collections?action=CREATE&name=demo_data&numShards=2&replicationFactor=1" > /dev/null
curl -s 'http://localhost:8981/solr/demo_data/update/json?commit=true' --data-binary @books.json -H 'Content-type:application/json' > /dev/null


echo "Configure the plugin for the demo data collection..."
# Add blob as runtime lib
curl -s -X POST -H 'Content-type:application/json' -d '
{
  "add-runtimelib": {"name": "ispy_component", "version": 1}
}' "http://localhost:8981/solr/demo_data/config" > /dev/null

# Add a get handler
curl -s -X POST -H 'Content-type:application/json' -d '{
  "add-requesthandler": {
    "name": "/get",
    "class": "solr.RealTimeGetHandler",
    "defaults": {"omitHeader": "true", "wt": "json", "indent", "true"}
  }
}' "http://localhost:8981/solr/demo_data/config" > /dev/null


# Add search component to collection
curl -s -X POST -H 'Content-type:application/json' -d '
{
  "add-searchcomponent":{
    "name": "ispy",
    "runtimeLib": "true",
    "version": "1",
    "class": "com.o19s.components.ISpyComponent",
    "ispyCollection": "http://solr1:8983/solr/ispy_log"
  }
}' "http://localhost:8981/solr/demo_data/config" > /dev/null

# Setup demo request handler for collection
curl -s -X POST -H 'Content-type:application/json' -d '{
  "add-requesthandler": {
    "name": "/ispy_demo",
    "class": "solr.SearchHandler",
    "defaults": {"df": "title", "defType": "edismax"},
    "components": ["query", "facet", "mlt", "highlight", "stats", "debug", "ispy"]
  }
}' "http://localhost:8981/solr/demo_data/config" > /dev/null


echo "Query demo collection (Trigger log)"
curl -s "http://localhost:8981/solr/demo_data/ispy_demo?q=pride&debug=true" > /dev/null

echo "Query log collection (Proof of life, you should see a logged explain here:)"
curl -s "http://localhost:8981/solr/ispy_log/get?id=pride"
