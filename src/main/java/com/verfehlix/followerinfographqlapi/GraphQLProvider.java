package com.verfehlix.followerinfographqlapi;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URL;

import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;

@Component
public class GraphQLProvider {

    private GraphQL graphQL;

    @Autowired
    private GraphQLDataFetchers graphQLDataFetchers;

    @Bean
    public GraphQL graphQL() {
        return graphQL;
    }

    /**
     * init method: initializes the graphQL API
     * @throws IOException
     */
    @PostConstruct
    public void init() throws IOException {
        // get schema from file & build SDL-string from it
        URL url = Resources.getResource("schema.graphqls");
        String sdl = Resources.toString(url, Charsets.UTF_8);

        // build schema from SDL-string
        GraphQLSchema graphQLSchema = buildSchema(sdl);

        // init graphql instance based on schema
        this.graphQL = GraphQL.newGraphQL(graphQLSchema).build();
    }

    /**
     * builds a GraphQL Schema from a given SDL (Schema Definition Language)
     * @param sdl
     * @return a graphql schema based on the given SDL-string
     */
   private GraphQLSchema buildSchema(String sdl) {
        // create type registry from sdl
        TypeDefinitionRegistry typeRegistry = new SchemaParser().parse(sdl);

        // build wiring (wires code into type-registry to actually fetch data)
        RuntimeWiring runtimeWiring = buildWiring();

        // generate schema from type registry & wiring and return it
        SchemaGenerator schemaGenerator = new SchemaGenerator();
        return schemaGenerator.makeExecutableSchema(typeRegistry, runtimeWiring);
    }

    /**
     * creates a wiring with data fetchers (to wire the given schema to actual data)
     * @return RunTimeWiring (wiring between a given schema and data fetchers)
     */
    private RuntimeWiring buildWiring() {
        // create new wiring and add data fetchers for follower info
        return RuntimeWiring.newRuntimeWiring()
                .type(newTypeWiring("Query")
                        .dataFetcher("followerInfo", graphQLDataFetchers.getFollowerInfoDataFetcher())
                )
                .type(newTypeWiring("FollowerInfo")

                )
                .build();
    }
}
