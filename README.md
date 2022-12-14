iSpy Component
---

This is a Solr Component that will log out full Solr responses to a configured Solr collection.  

## Building
- Run `./gradlew build` to get a new jar in the `build/libs` folder.  From here you can run the demo to get an idea of how the plugin works.


## Setup
First, add the Jar to your solr lib folder, next add the new component to your `solrconfig.xml`:

```
<searchComponent class="com.o19s.components.ISpyComponent" name="ispy">
  <str name="ispyCollection">http://quepid-solr.dev.o19s.com:8985/solr/ispy</str>
</searchComponent>
```

Note: You must specify the Solr host including HTTP scheme


Lastly, configure a search handler of your choosing to run the ispy component last in the chain, you must specify the full chain of components as Solr runs debug after `last-components` and we want to include that data.

```
<requestHandler name="standard" class="solr.StandardRequestHandler" default="true">
  <lst name="defaults">
    <str name="df">content</str>
  </lst>
  <arr name="components">
    <str>query</str>
    <str>facet</str>
    <str>mlt</str>
    <str>highlight</str>
    <str>stats</str>
    <str>debug</str>
    <str>ispy</str>
  </arr>
</requestHandler>
```

From here if you run a query with debug the results will be logged to the configured collection if ispy is enabled on the handler.
