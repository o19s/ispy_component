demo
---

This demo shows basic utilization with SolrCloud.

## Prereqs
- Docker + Docker Compose
- A shell with bash & curl support
- An existing build, run `gradlew build` from the top level before trying to run the demo.

## Running
- Fire up the docker demo environment with `docker-compose up`.  This will setup an empty SolrCloud environment with two Solr nodes and one Zookeeper.
- Run the setup demo script: `./setup_demo.sh`

The script creates two collections, one is a demo data collection and another is the target logging collection for the iSpy component plugin.  Anytime the `demo_data` collection is queried against `/ispy_demo` with debug enabled, the query results will be logged out to `ispy_log`.  View the setup script to get an idea of customizing the plugin for your deployment.
